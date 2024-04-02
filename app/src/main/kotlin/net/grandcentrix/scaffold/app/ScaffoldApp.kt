package net.grandcentrix.scaffold.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import net.grandcentrix.scaffold.app.ui.components.AppBottomNavigation
import net.grandcentrix.scaffold.app.ui.components.AppTopBar
import net.grandcentrix.scaffold.app.ui.theme.AppTheme

/*
    Main entry point for the app.
    Todo rename the composable function to apply for the customer
 */
@Composable
fun ScaffoldApp() {
    AppTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { AppBottomNavigation(navController = navController) },
            topBar = { AppTopBar(navController = navController) }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(it)
            ) {
                addHomeGraph()
                addThemingGraph()
                addProfileGraph()
            }
        }
    }
}
