package io.nosyntax.foundation.data.mapper

import io.nosyntax.foundation.data.source.remote.dto.app_config.ThemeDto
import io.nosyntax.foundation.data.source.remote.dto.app_config.ComponentsDto
import io.nosyntax.foundation.data.source.remote.dto.app_config.AppConfigDto
import io.nosyntax.foundation.data.source.remote.dto.app_config.MonetizationDto
import io.nosyntax.foundation.data.source.remote.dto.app_config.SettingsDto
import io.nosyntax.foundation.domain.model.NavigationItem
import io.nosyntax.foundation.domain.model.app_config.Theme
import io.nosyntax.foundation.domain.model.app_config.Components
import io.nosyntax.foundation.domain.model.app_config.AppConfig
import io.nosyntax.foundation.domain.model.app_config.Monetization
import io.nosyntax.foundation.domain.model.app_config.Settings

fun AppConfigDto.toAppConfig(): AppConfig {
    return AppConfig(
        id = this.id,
        name = this.name,
        version = this.version,
        description = this.description,
        settings = this.settings.toSettings(),
        theme = this.theme.toTheme(),
        components = this.components.toComponents(),
        monetization = this.monetization.toMonetization()
    )
}

fun SettingsDto.toSettings(): Settings {
    return Settings(
        entryPage = this.entryPage,
        email = this.email,
        privacyPolicy = this.privacyPolicy,
        termsOfService = this.termsOfService
    )
}

fun ThemeDto.toTheme(): Theme {
    return Theme(
        colorScheme = Theme.ColorScheme(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            secondary = colorScheme.secondary,
            onSecondary = colorScheme.onSecondary,
            backgroundLight = colorScheme.backgroundLight,
            onBackgroundLight = colorScheme.onBackgroundLight,
            surfaceLight = colorScheme.surfaceLight,
            onSurfaceLight = colorScheme.onSurfaceLight,
            surfaceVariantLight = colorScheme.surfaceVariantLight,
            onSurfaceVariantLight = colorScheme.onSurfaceVariantLight,
            outlineLight = colorScheme.outlineLight,
            outlineVariantLight = colorScheme.outlineVariantLight,
            backgroundDark = colorScheme.backgroundDark,
            onBackgroundDark = colorScheme.onBackgroundDark,
            surfaceDark = colorScheme.surfaceDark,
            onSurfaceDark = colorScheme.onSurfaceDark,
            surfaceVariantDark = colorScheme.surfaceVariantDark,
            onSurfaceVariantDark = colorScheme.onSurfaceVariantDark,
            outlineDark = colorScheme.outlineDark,
            outlineVariantDark = colorScheme.outlineVariantDark
        ),
        typography = Theme.Typography(
            primaryFontFamily = typography.primaryFontFamily,
            secondaryFontFamily = typography.secondaryFontFamily
        ),
        darkMode = darkMode
    )
}

fun ComponentsDto.toComponents(): Components {
    return Components(
        appBar = Components.AppBar(
            visible = appBar.visible,
            background = appBar.background,
            title = Components.AppBar.Title(
                visible = appBar.title.visible,
                alignment = appBar.title.alignment
            )
        ),
        navigationDrawer = Components.NavigationDrawer(
            visible = navigationDrawer.visible,
            background = navigationDrawer.background,
            header = Components.NavigationDrawer.Header(
                visible = navigationDrawer.header.visible,
                image = navigationDrawer.header.image
            ),
            items = this.navigationDrawer.items.map { item ->
                NavigationItem(
                    route = item.route,
                    type = item.type,
                    label = item.label,
                    icon = item.icon,
                    action = item.action
                )
            }
        ),
        navigationBar = Components.NavigationBar(
            visible = navigationBar.visible,
            background = navigationBar.background,
            label = navigationBar.label,
            items = navigationBar.items.map { item ->
                NavigationItem(
                    route = item.route,
                    type = item.type,
                    label = item.label,
                    icon = item.icon,
                    action = item.action
                )
            }
        ),
        loadingIndicator = Components.LoadingIndicator(
            visible = loadingIndicator.visible,
            animation = loadingIndicator.animation,
            background = loadingIndicator.background,
            color = loadingIndicator.color
        )
    )
}

fun MonetizationDto.toMonetization(): Monetization {
    return Monetization(
        ads = Monetization.Ads(
            enabled = this.ads.enabled,
            bannerDisplay = this.ads.bannerDisplay,
            interstitialDisplay = this.ads.interstitialDisplay
        )
    )
}