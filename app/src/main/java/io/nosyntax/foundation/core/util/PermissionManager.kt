package io.nosyntax.foundation.core.util

import android.content.Context
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.nosyntax.foundation.core.extension.openAppSettings

@OptIn(ExperimentalPermissionsApi::class)
class PermissionManager(private val context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    fun requestPermission(permissionState: PermissionState) {
        when {
            isFirstRequest(permissionState.permission) -> {
                setFirstRequest(permissionState.permission)
                permissionState.launchPermissionRequest()
            }
            permissionState.status.shouldShowRationale -> {
                permissionState.launchPermissionRequest()
            }
            else -> {
                context.openAppSettings()
            }
        }
    }

    fun requestMultiplePermissions(permissionsState: MultiplePermissionsState) {
        val permissionsToRequest = permissionsState.permissions.filter {
            isFirstRequest(it.permission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionsToRequest.forEach { permissionState ->
                setFirstRequest(permissionState.permission)
            }
            permissionsState.launchMultiplePermissionRequest()
        } else if (permissionsState.shouldShowRationale) {
            permissionsState.launchMultiplePermissionRequest()
        } else {
            context.openAppSettings()
        }
    }

    private fun isFirstRequest(permission: String): Boolean =
        preferences.getBoolean(permission, true)

    private fun setFirstRequest(permission: String) {
        preferences.edit().putBoolean(permission, false).apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "permissions"
    }
}