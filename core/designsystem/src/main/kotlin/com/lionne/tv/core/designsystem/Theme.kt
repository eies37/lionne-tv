package com.lionne.tv.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * Identité visuelle de l'application. Changer de marque (couleurs, nom) se fait ici,
 * sans toucher aux écrans : ils lisent tous [LocalBrand] et le thème Material.
 */
@Immutable
data class BrandConfig(
    val appName: String,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
) {
    companion object {
        /** Palette provisoire « Lionne » : or fauve sur fond nuit. À valider (tâche T-012). */
        val Lionne = BrandConfig(
            appName = "Lionne TV",
            primary = Color(0xFFE0A526),
            onPrimary = Color(0xFF1A1200),
            accent = Color(0xFFFFC94D),
            background = Color(0xFF0E0F12),
            surface = Color(0xFF1B1D22),
            onSurface = Color(0xFFF2EFE8),
            onSurfaceMuted = Color(0xFFA8A49B),
        )
    }
}

val LocalBrand = staticCompositionLocalOf { BrandConfig.Lionne }

@Composable
fun LionneTheme(
    brand: BrandConfig = BrandConfig.Lionne,
    content: @Composable () -> Unit,
) {
    val colors = darkColorScheme(
        primary = brand.primary,
        onPrimary = brand.onPrimary,
        background = brand.background,
        onBackground = brand.onSurface,
        surface = brand.surface,
        onSurface = brand.onSurface,
        onSurfaceVariant = brand.onSurfaceMuted,
    )
    CompositionLocalProvider(LocalBrand provides brand) {
        MaterialTheme(colorScheme = colors) {
            CompositionLocalProvider(LocalContentColor provides brand.onSurface, content = content)
        }
    }
}
