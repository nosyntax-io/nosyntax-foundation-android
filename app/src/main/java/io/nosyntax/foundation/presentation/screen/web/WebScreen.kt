package io.nosyntax.foundation.presentation.screen.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.nosyntax.foundation.R
import io.nosyntax.foundation.core.component.ScreenOrientationController
import io.nosyntax.foundation.presentation.component.NoConnectionView
import io.nosyntax.foundation.core.component.SystemUIController
import io.nosyntax.foundation.core.component.SystemUIState
import io.nosyntax.foundation.core.extension.findActivity
import io.nosyntax.foundation.core.util.Connectivity
import io.nosyntax.foundation.presentation.screen.web.util.Downloader
import io.nosyntax.foundation.presentation.screen.web.util.WebKitChromeClient
import io.nosyntax.foundation.presentation.screen.web.util.WebKitClient
import io.nosyntax.foundation.presentation.screen.web.util.WebView
import io.nosyntax.foundation.core.extension.handleIntent
import io.nosyntax.foundation.core.util.monetize.BannerAd
import io.nosyntax.foundation.core.extension.openContent
import io.nosyntax.foundation.presentation.screen.web.util.rememberSaveableWebViewState
import io.nosyntax.foundation.presentation.screen.web.util.rememberWebViewNavigator
import io.nosyntax.foundation.domain.model.app_config.AppConfig
import io.nosyntax.foundation.domain.model.app_config.WebViewSettings
import io.nosyntax.foundation.presentation.MainActivity
import io.nosyntax.foundation.presentation.screen.web.component.JsAlertDialog
import io.nosyntax.foundation.presentation.screen.web.component.JsConfirmDialog
import io.nosyntax.foundation.presentation.screen.web.component.JsPromptDialog
import io.nosyntax.foundation.presentation.component.LoadingIndicatorView
import io.nosyntax.foundation.presentation.component.PermissionDialog
import io.nosyntax.foundation.presentation.screen.web.util.FileChooserDelegate
import io.nosyntax.foundation.presentation.screen.web.util.JsDialog
import io.nosyntax.foundation.presentation.screen.web.util.WebViewPermissions
import io.nosyntax.foundation.presentation.screen.web.util.rememberWebViewPermissions
import kotlinx.coroutines.CoroutineScope

@Composable
fun WebScreen(
    appConfig: AppConfig,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    url: String,
    captureBackPresses: Boolean
) {
    val context = LocalContext.current

    val webViewState = rememberSaveableWebViewState()
    val navigator = rememberWebViewNavigator()
    val webViewPermissions = rememberWebViewPermissions()

    val systemUiState = remember { mutableStateOf(SystemUIState.SYSTEM_UI_VISIBLE) }
    var requestedOrientation by remember { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }
    var jsDialogInfo by rememberSaveable { mutableStateOf<Pair<JsDialog?, JsResult?>?>(null) }
    var totalLoadedPages by remember { mutableIntStateOf(0) }

    SystemUIController(systemUiState = systemUiState)
    ScreenOrientationController(orientation = requestedOrientation)

    LaunchedEffect(navigator) {
        if (webViewState.viewState == null) {
            navigator.loadUrl(url)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WebView(
            modifier = Modifier.fillMaxSize(),
            state = webViewState,
            navigator = navigator,
            captureBackPresses = captureBackPresses,
            onCreated = { webView ->
                initWebView(
                    context = context,
                    webView = webView,
                    settings = appConfig.webViewSettings,
                    permissions = webViewPermissions
                )
            },
            client = webClient(
                context = context,
                onPageLoaded = {
                    if (totalLoadedPages < 7) {
                        totalLoadedPages++
                    } else {
                        (context.findActivity() as MainActivity).showInterstitial()
                        totalLoadedPages = 1
                    }
                }
            ),
            chromeClient = chromeClient(
                context = context,
                settings = appConfig.webViewSettings,
                permissions = webViewPermissions,
                onJsDialogEvent = { d, r -> jsDialogInfo = d to r },
            )
        )

        val ads = appConfig.monetization.ads
        BannerAd(
            enabled = ads.enabled && ads.bannerDisplay, modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }

    appConfig.components.loadingIndicator.takeIf {
        webViewState.isLoading && it.visible
    }?.let {
        LoadingIndicatorView(indicatorConfig = it)
    }

    if (webViewState.capturedErrors.isNotEmpty()) {
        NoConnectionView(onRetry = {
            if (Connectivity.getInstance().isOnline()) {
                navigator.reload()
            }
        })
    }

    jsDialogInfo?.let { (dialog, result) ->
        val cancelDialog: () -> Unit = {
            jsDialogInfo = null
            result?.cancel()
        }

        val confirmDialog: () -> Unit = {
            jsDialogInfo = null
            result?.confirm()
        }

        when (dialog) {
            is JsDialog.Alert -> JsAlertDialog(
                message = dialog.message,
                onConfirm = confirmDialog
            )
            is JsDialog.Confirm -> JsConfirmDialog(
                message = dialog.message,
                onCancel = cancelDialog,
                onConfirm = confirmDialog
            )
            is JsDialog.Prompt -> JsPromptDialog(
                message = dialog.message,
                defaultValue = dialog.defaultValue,
                onCancel = cancelDialog,
                onConfirm = { promptResult ->
                    (result as? JsPromptResult)?.confirm(promptResult)
                    cancelDialog()
                }
            )
            else -> {}
        }
    }

    WebViewPermissionsHandler(
        webViewPermissions = webViewPermissions
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun initWebView(
    context: Context,
    webView: WebView,
    settings: WebViewSettings,
    permissions: WebViewPermissions
) {
    webView.apply {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false

        this.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            domStorageEnabled = true
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = WebSettings.LOAD_DEFAULT

            supportMultipleWindows()
            setGeolocationEnabled(settings.geolocationEnabled)
        }

        setDownloadListener { url, userAgent, disposition, mimeType, _ ->
            val fileName = URLUtil.guessFileName(url, disposition, mimeType)

            if (Build.VERSION.SDK_INT in 24..29) {
                if (permissions.isStoragePermissionGranted()) {
                    Downloader(context).download(fileName, url, userAgent, mimeType)
                } else {
                    permissions.showStoragePermissionRationale()
                }
            } else {
                Downloader(context).download(fileName, url, userAgent, mimeType)
            }
        }
    }
}

@Composable
private fun webClient(
    context: Context,
    onPageLoaded: () -> Unit
): WebKitClient {
    val knownDomains = remember {
        context.assets.open(
            "known-domains.txt"
        ).bufferedReader().readLines()
    }

    val webClient = remember {
        object : WebKitClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                onPageLoaded()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString().orEmpty()

                return when {
                    url == "about:blank" -> true

                    !URLUtil.isValidUrl(url) -> {
                        context.handleIntent(url)
                        true
                    }

                    knownDomains.any { url.contains(it) } -> {
                        context.openContent(url).let { true }
                    }

                    else -> false
                }
            }
        }
    }

    return webClient
}

@Composable
private fun chromeClient(
    context: Context,
    settings: WebViewSettings,
    permissions: WebViewPermissions,
    onJsDialogEvent: (JsDialog, JsResult) -> Unit,
): WebKitChromeClient {
    var filePath by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserDelegate = remember {
        FileChooserDelegate(context = context, onReceiveResult = { uris ->
            filePath?.onReceiveValue(uris)
            filePath = null
        })
    }

    val fileChooser = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            fileChooserDelegate.onActivityResult(result.data)
        }
    )

    val chromeClient = remember {
        object : WebKitChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (message != null && result != null) {
                    onJsDialogEvent(JsDialog.Alert(message), result)
                    return true
                }
                result?.cancel()
                return false
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (message != null && result != null) {
                    onJsDialogEvent(JsDialog.Confirm(message), result)
                    return true
                }
                result?.cancel()
                return false
            }

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                if (message != null && result != null) {
                    onJsDialogEvent(JsDialog.Prompt(message, defaultValue.orEmpty()), result)
                    return true
                }
                result?.cancel()
                return false
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (!settings.allowFileUploads) return false

                filePathCallback?.let { callback ->
                    val isCaptureEnabled = fileChooserParams?.acceptTypes?.any { type ->
                        setOf(
                            "image", "jpg", "jpeg", "gif", "webp", "tiff",
                            "bmp", "ico", "pjp", "svg", "tif", "png"
                        ).any { type.contains(it, ignoreCase = true) }
                    } == true

                    if (isCaptureEnabled && !permissions.isCameraPermissionGranted()) {
                        permissions.showCameraPermissionRationale()
                        return false
                    }

                    filePath?.onReceiveValue(null)
                    filePath = callback

                    val fileChooserParamsOverride = fileChooserParams?.let {
                        object : FileChooserParams() {
                            override fun getMode() = it.mode
                            override fun getAcceptTypes() = it.acceptTypes
                            override fun isCaptureEnabled() = isCaptureEnabled
                            override fun getTitle() = it.title
                            override fun getFilenameHint() = it.filenameHint
                            override fun createIntent() = it.createIntent()
                        }
                    }

                    return fileChooserParamsOverride?.let {
                        fileChooserDelegate.onShowFileChooser(
                            params = fileChooserParamsOverride,
                            launcher = fileChooser
                        )
                    } ?: false
                }

                return false
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                if (permissions.isLocationPermissionsGranted()) {
                    callback?.invoke(origin, true, false)
                } else {
                    callback?.invoke(origin, false, false)
                    permissions.showLocationPermissionsRationale()
                }
            }
        }
    }

    return chromeClient
}

@Composable
private fun WebViewPermissionsHandler(
    webViewPermissions: WebViewPermissions
) {
    webViewPermissions.rationaleState.value?.let { state ->
        when (state) {
            WebViewPermissions.RationaleType.StorageRequired -> {
                PermissionDialog(
                    title = stringResource(R.string.storage_required),
                    description = stringResource(R.string.storage_required_description),
                    icon = painterResource(R.drawable.icon_folder_outline),
                    onDismiss = { webViewPermissions.dismissRationale() },
                    onConfirm = {
                        webViewPermissions.apply {
                            dismissRationale()
                            requestStoragePermission()
                        }
                    }
                )
            }

            is WebViewPermissions.RationaleType.CameraRequired -> {
                PermissionDialog(
                    title = stringResource(R.string.camera_required),
                    description = stringResource(R.string.camera_required_description),
                    icon = painterResource(R.drawable.icon_camera_outline),
                    onDismiss = { webViewPermissions.dismissRationale() },
                    onConfirm = {
                        webViewPermissions.apply {
                            dismissRationale()
                            requestCameraPermission()
                        }
                    }
                )
            }

            is WebViewPermissions.RationaleType.LocationRequired -> {
                PermissionDialog(
                    title = stringResource(R.string.location_required),
                    description = stringResource(R.string.location_required_description),
                    icon = painterResource(R.drawable.icon_location_outline),
                    onDismiss = { webViewPermissions.dismissRationale() },
                    onConfirm = {
                        webViewPermissions.apply {
                            dismissRationale()
                            requestLocationPermissions()
                        }
                    }
                )
            }
        }
    }
}