package net.grandcentrix.scaffold.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Define your color values here, which ARE included in the [AppTheme]. Make them private to use
 * them only by the theme.
 */
private val purple200 = Color(0xFFBB86FC)
private val purple500 = Color(0xFF6200EE)
private val purple700 = Color(0xFF3700B3)
private val teal200 = Color(0xFF03DAC5)

private val LightThemeColors = lightColors(
    primary = purple500,
    primaryVariant = purple700,
    secondary = teal200
)

private val DarkThemeColors = darkColors(
    primary = purple200,
    primaryVariant = purple700,
    secondary = teal200,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkThemeColors else LightThemeColors,
        typography = AppTypography,
        content = content
    )
}
