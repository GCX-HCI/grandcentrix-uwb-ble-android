package net.grandcentrix.scaffold.app.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import net.grandcentrix.scaffold.app.R
import net.grandcentrix.scaffold.app.Screen

@Composable
fun AppBottomNavigation(navController: NavController) {
    val currentBaseScreen by navController.currentBaseScreenAsState()
    currentBaseScreen?.run {
        AppBottomNavigation(currentScreen = this) {
            navController.navigate(it.route) {
                launchSingleTop = true
                restoreState = true

                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        }
    }
}

@Composable
internal fun AppBottomNavigation(currentScreen: Screen, onClick: (Screen) -> Unit) {
    BottomNavigation {
        BottomNavigationItem(
            selected = currentScreen == Screen.Home,
            onClick = { onClick(Screen.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = { Text(text = stringResource(id = R.string.title_home)) }
        )
        BottomNavigationItem(
            selected = currentScreen == Screen.Theming,
            onClick = { onClick(Screen.Theming) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dashboard_black_24dp),
                    contentDescription = null
                )
            },
            label = { Text(text = stringResource(id = R.string.title_theming)) }
        )
        BottomNavigationItem(
            selected = currentScreen == Screen.Profile,
            onClick = { onClick(Screen.Profile) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile_black_24dp),
                    contentDescription = null
                )
            },
            label = { Text(text = stringResource(id = R.string.title_profile)) }
        )
    }
}

/**
 * Gets the current base [Screen] as a MutableState. When the given navController changes
 * the back stack due to a [NavController.navigate] or [NavController.popBackStack] this will
 * trigger a recompose and return the base screen of the hierarchy.
 *
 * Usecase: Find the base screen to identify which [BottomNavigationItem] is selected.
 *
 * @return: a mutable state of the current base screen
 */
@Composable
internal fun NavController.currentBaseScreenAsState(): State<Screen?> {
    val selectedItem = remember { mutableStateOf<Screen?>(null) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            selectedItem.value = when {
                destination.hierarchy.any { it.route == Screen.Home.route } -> Screen.Home
                destination.hierarchy.any { it.route == Screen.Theming.route } -> Screen.Theming
                destination.hierarchy.any { it.route == Screen.Profile.route } -> Screen.Profile
                else -> null
            }
        }

        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

/**
 * Navigation in compose supports both global app bar and local app bar for each screen. If you want
 * to have the same look and feel for every screen it makes sense to use the composable in
 * [net.grandcentrix.scaffold.app.App]. If you want a custom app bar for each screen consider using
 * this composable in each screen.
 *
 *
 * Reference:
 * https://kotlinlang.slack.com/archives/CJLTWPH7S/p1620959319009300?thread_ts=1620952017.004700&cid=CJLTWPH7S
 */
@Composable
fun AppTopBar(
    navController: NavController
) {
    val currentScreen by navController.currentScreenAsState()
    TopAppBar(
        title = currentScreen.title,
        actions = { currentScreen.menuItems(this, navController) },
        navigationIcon =
        if (currentScreen.enableUpNavigation) {
            {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.navigation_back)
                    )
                }
            }
        } else null
    )
}

/**
 * Gets the current [Screen] as a MutableState. When the given navController changes the back stack
 * due to a [NavController.navigate] or [NavController.popBackStack] this will trigger a recompose
 * and return the top screen of the hierarchy.
 *
 * Usecase: Find the current screen to adjust the [AppTopBar].
 *
 * @return: a mutable state of the current screen
 */
@Composable
internal fun NavController.currentScreenAsState(): State<Screen> {
    val selectedScreen = remember { mutableStateOf<Screen>(Screen.Home) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            Screen.getOrNull(destination.route)?.let {
                selectedScreen.value = it
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose { removeOnDestinationChangedListener(listener) }
    }

    return selectedScreen
}
