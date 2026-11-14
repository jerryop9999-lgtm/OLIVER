package com.xapk2apk.tool.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AppRoot(vm: MainViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Convert" to "🔄", "History" to "🕘", "Settings" to "⚙️")
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i },
                        icon = { Text(icon) }, label = { Text(label) })
                }
            }
        }
    ) { pad ->
        Surface(Modifier.padding(pad)) {
            when (tab) {
                0 -> ConvertScreen(vm)
                1 -> HistoryScreen(vm)
                else -> SettingsScreen(vm)
            }
        }
    }
}
