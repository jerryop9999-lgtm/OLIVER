package com.xapk2apk.tool.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(
    primary = Color(0xFF6EA2FF), background = Color(0xFF0F1115), surface = Color(0xFF171A21),
    surfaceVariant = Color(0xFF222733), onBackground = Color(0xFFE6E8EE), onSurface = Color(0xFFE6E8EE)
)
private val Light = lightColorScheme(primary = Color(0xFF2D6CDF))

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
