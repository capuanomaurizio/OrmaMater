package it.unibo.orma.ui.theme

import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import it.unibo.orma.data.repositories.ThemeMode

/**
 * Palette dell'app: verde bosco come colore principale (complementare del rosso UNIBO) e color terracotta come accento.
 *
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF2E6B4F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB4F1CE),
    onPrimaryContainer = Color(0xFF00210F),

    secondary = Color(0xFF4E6355),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E8D6),
    onSecondaryContainer = Color(0xFF0C1F14),

    tertiary = Color(0xFFB5502F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBD0),
    onTertiaryContainer = Color(0xFF3B0A00),

    background = Color(0xFFF7FBF7),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF7FBF7),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DC),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717972),
    outlineVariant = Color(0xFFC0C9C0),

    // Le Card e le altre superfici elevate usano questi ruoli: senza, restano
    // sul grigio tendente al viola del tema di base.
    surfaceDim = Color(0xFFD7DBD5),
    surfaceBright = Color(0xFFF7FBF7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5F0),
    surfaceContainer = Color(0xFFEBEFEA),
    surfaceContainerHigh = Color(0xFFE5E9E4),
    surfaceContainerHighest = Color(0xFFDFE4DE)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD3B0),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF10512F),
    onPrimaryContainer = Color(0xFFABF0CB),

    secondary = Color(0xFFB4CCB9),
    onSecondary = Color(0xFF203527),
    secondaryContainer = Color(0xFF364B3D),
    onSecondaryContainer = Color(0xFFD0E8D6),

    tertiary = Color(0xFFFFB59B),
    onTertiary = Color(0xFF5F1600),
    tertiaryContainer = Color(0xFF8C2D0F),
    onTertiaryContainer = Color(0xFFFFDBD0),

    background = Color(0xFF101410),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF101410),
    onSurface = Color(0xFFE1E3DE),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC0C9C0),
    outline = Color(0xFF8B938B),
    outlineVariant = Color(0xFF414942),

    surfaceDim = Color(0xFF101410),
    surfaceBright = Color(0xFF353A35),
    surfaceContainerLowest = Color(0xFF0B0F0B),
    surfaceContainerLow = Color(0xFF191C19),
    surfaceContainer = Color(0xFF1D211D),
    surfaceContainerHigh = Color(0xFF272B27),
    surfaceContainerHighest = Color(0xFF323632)
)

/**
 * dynamicColor è disattivato: Material You sostituirebbe la palette con una derivata dallo sfondo del dispositivo, e l'app perderebbe la propria
 * identità — il verde diventa il colore di sistema, diverso su ogni telefono. Per riattivare: dynamicColor = true.
 */
@Composable
fun OrmaTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val activity = LocalActivity.current
    val view = LocalView.current
    if (activity != null && !view.isInEditMode) {
        val sfondo = colorScheme.background.toArgb()
        SideEffect {
            activity.window.setBackgroundDrawable(ColorDrawable(sfondo))
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
