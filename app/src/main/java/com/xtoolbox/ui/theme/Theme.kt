package com.xtoolbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixThemeOption

@Composable
fun XToolboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MiuixTheme(
        themeOption = if (darkTheme) MiuixThemeOption.Dark else MiuixThemeOption.Light,
        content = content
    )
}
