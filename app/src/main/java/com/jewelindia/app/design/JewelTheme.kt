// Port of: wholesaler ios/JewelIndia/Sources/Design/Theming
package com.jewelindia.app.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val JewelColorScheme = lightColorScheme(
    background = Palette.background,
    surface = Palette.light,
    onBackground = Palette.foreground,
    onSurface = Palette.foreground,
    primary = Palette.dark,
    onPrimary = Palette.light,
    outline = Palette.border,
    error = Palette.statusRejected
)

@Composable
fun JewelTheme(
    roleTheme: RoleTheme = RoleTheme.Wholesaler,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalRoleTheme provides roleTheme) {
        MaterialTheme(
            colorScheme = JewelColorScheme,
            content = content
        )
    }
}
