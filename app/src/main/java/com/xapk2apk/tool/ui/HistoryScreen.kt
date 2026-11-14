package com.xapk2apk.tool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xapk2apk.tool.data.Installer
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(vm: MainViewModel) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("History", style = MaterialTheme.typography.headlineMedium)
            if (vm.history.isNotEmpty()) TextButton(onClick = vm::clearHistory) { Text("Clear all") }
        }
        if (vm.history.isEmpty()) {
            Text("Nothing converted yet.", Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.history, key = { it.path }) { h ->
                    val exists = File(h.path).exists()
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(h.name, style = MaterialTheme.typography.titleSmall)
                            Text("${formatSize(h.sizeBytes)} • " +
                                DateFormat.getDateTimeInstance().format(Date(h.time)),
                                style = MaterialTheme.typography.bodySmall)
                            h.packageName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { Installer.install(ctx, File(h.path)) }, enabled = exists) { Text("Install") }
                                TextButton(onClick = { Installer.share(ctx, File(h.path)) }, enabled = exists) { Text("Share") }
                                TextButton(onClick = { vm.deleteHistory(h) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}
