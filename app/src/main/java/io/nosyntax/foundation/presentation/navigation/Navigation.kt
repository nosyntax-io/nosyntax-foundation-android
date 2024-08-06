package io.nosyntax.foundation.presentation.navigation

import android.content.Context
import androidx.navigation.NavHostController
import io.nosyntax.foundation.core.utility.Intents.openDial
import io.nosyntax.foundation.core.utility.Intents.openEmail
import io.nosyntax.foundation.core.utility.Intents.openSMS
import io.nosyntax.foundation.core.utility.Intents.openUrl
import io.nosyntax.foundation.domain.model.NavigationItem

class Navigator(private val context: Context, private val navController: NavHostController) {
    fun handleItemClick(item: NavigationItem) {
        if (item.type in setOf("browser", "mail", "dial", "sms")) {
            performAction(item.type, item.action!!)
        } else {
            navigate(item.route!!, item.type)
        }
    }

    private fun performAction(type: String, action: String) {
        when (type) {
            "browser" -> context.openUrl(action)
            "mail" -> context.openEmail(action)
            "dial" -> context.openDial(action)
            "sms" -> context.openSMS(action)
        }
    }

    private fun navigate(route: String, type: String) {
        navController.navigate(route = route) {
            if (type in setOf("settings", "about")) {
                popUpTo(navController.currentBackStackEntry?.destination?.route ?: route) {
                    inclusive = false
                }
            } else {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) {
                        saveState = true
                    }
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}