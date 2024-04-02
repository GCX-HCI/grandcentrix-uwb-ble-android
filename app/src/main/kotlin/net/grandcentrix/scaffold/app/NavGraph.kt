package net.grandcentrix.scaffold.app

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import net.grandcentrix.scaffold.app.ui.home.HomeScreen
import net.grandcentrix.scaffold.app.ui.profile.ProfileScreen
import net.grandcentrix.scaffold.app.ui.theming.ThemingScreen

sealed class Screen(
    val route: String,
    val title: @Composable () -> Unit,
    val menuItems: @Composable RowScope.(navController: NavController) -> Unit = {},
    val enableUpNavigation: Boolean = false,
    val content: @Composable () -> Unit
) {
    object Home : Screen(
        route = Destination.Home.route,
        title = { Text(text = stringResource(id = R.string.title_home)) },
        content = { HomeScreen() }
    )

    object Theming : Screen(
        route = Destination.Theming.route,
        title = { Text(text = stringResource(id = R.string.title_theming)) },
        content = { ThemingScreen() }
    )

    object Profile : Screen(
        route = Destination.Profile.route,
        title = { Text(text = stringResource(id = R.string.title_profile)) },
        menuItems = { navController ->
            IconButton(onClick = { navController.navigate(Destination.Settings.route) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.title_settings)
                )
            }
        },
        content = { ProfileScreen() }
    )

    object Settings : Screen(
        route = Destination.Settings.route,
        title = { Text(text = stringResource(id = R.string.title_settings)) },
        enableUpNavigation = true,
        content = { Scaffold { Text(stringResource(id = R.string.title_settings)) } }
    )

    companion object {
        fun getOrNull(route: String?): Screen? {
            return when (route) {
                Home.route -> Home
                Theming.route -> Theming
                Profile.route -> Profile
                Settings.route -> Settings
                else -> null
            }
        }
    }
}

sealed class Destination(val route: String) {
    object Home : Destination("home")
    object Theming : Destination("theming")
    object Profile : Destination("profile")
    object Settings : Destination("settings")
}

fun NavGraphBuilder.addHomeGraph() {
    composable(route = Screen.Home.route) { Screen.Home.content() }
}

fun NavGraphBuilder.addThemingGraph() {
    composable(route = Screen.Theming.route) { Screen.Theming.content() }
}

fun NavGraphBuilder.addProfileGraph() {
    composable(route = Screen.Profile.route) { Screen.Profile.content() }
    composable(route = Screen.Settings.route) { Screen.Settings.content() }
}
