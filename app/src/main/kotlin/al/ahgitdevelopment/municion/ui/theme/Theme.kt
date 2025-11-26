package al.ahgitdevelopment.municion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = Primary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Secondary.copy(alpha = 0.12f),
    onSecondaryContainer = Secondary,
    tertiary = Primary,
    onTertiary = OnPrimary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Background,
    onSurfaceVariant = OnSurface.copy(alpha = 0.7f),
    error = Error,
    onError = OnError,
    errorContainer = Error.copy(alpha = 0.12f),
    onErrorContainer = Error,
    outline = OnSurface.copy(alpha = 0.3f),
    outlineVariant = OnSurface.copy(alpha = 0.12f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.24f),
    onPrimaryContainer = Primary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Secondary.copy(alpha = 0.24f),
    onSecondaryContainer = Secondary,
    tertiary = Primary,
    onTertiary = OnPrimary,
    background = PrimaryDark,
    onBackground = OnPrimary,
    surface = PrimaryDark,
    onSurface = OnPrimary,
    surfaceVariant = PrimaryDark,
    onSurfaceVariant = OnPrimary.copy(alpha = 0.7f),
    error = Error,
    onError = OnError,
    errorContainer = Error.copy(alpha = 0.24f),
    onErrorContainer = Error,
    outline = OnPrimary.copy(alpha = 0.3f),
    outlineVariant = OnPrimary.copy(alpha = 0.12f)
)

@Composable
fun MunicionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Edge-to-edge: System bars are transparent
    // Status bar color comes from TopBar background (PrimaryDark)
    // Navigation bar color comes from BottomBar background (PrimaryDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
