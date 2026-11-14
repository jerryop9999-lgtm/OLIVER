package com.xapk2apk.tool.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xapk2apk.tool.data.Installer

fun formatSize(b: Long): String = when {
    b >= 1L shl 30 -> "%.2f GB".format(b / (1L shl 30).toDouble())
    b >= 1L shl 20 -> "%.1f MB".format(b / (1L shl 20).toDouble())
    b >= 1L shl 10 -> "%.0f KB".format(b / 1024.0)
    else -> "$b B"
}

@Composable
fun ConvertScreen(vm: MainViewModel) {
    val ctx = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.pick(uri)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("OLIVER", style = MaterialTheme.typography.headlineMedium)
        Text("XAPK / APKS / APKM → one installable APK",
            style = MaterialTheme.typography.bodyMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val p = vm.picked
                if (p == null) Text("No file selected")
                else {
                    Text(p.name, style = MaterialTheme.typography.titleMedium)
                    if (p.size > 0) Text(formatSize(p.size))
                }
                OutlinedButton(onClick = { picker.launch(arrayOf("*/*")) }, enabled = !vm.busy) {
                    Text(if (p == null) "Choose file" else "Change file")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::convert, enabled = vm.picked != null && !vm.busy,
                modifier = Modifier.weight(1f)) { Text("Convert") }
        }

        if (vm.busy || vm.stage != Stage0) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(vm.stage.label, style = MaterialTheme.typography.titleSmall)
                    if (vm.busy) LinearProgressIndicator(progress = { vm.progress }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        vm.error?.let {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(it, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        vm.result?.let { r ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Success", style = MaterialTheme.typography.titleMedium)
                    Text("${r.apk.name} • ${formatSize(r.apk.length())}")
                    Text("Format: ${r.kind}  •  Splits merged: ${r.splitCount}")
                    r.packageName?.let { Text("Package: $it") }
                    vm.savedTo?.let { Text("Saved to: $it") }
                    if (r.obbs.isNotEmpty()) {
                        Text("OBB files: ${r.obbs.size}. Copy them to /sdcard/Android/obb/${r.packageName ?: "<package>"}/",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { Installer.install(ctx, r.apk) }) { Text("Install") }
                        OutlinedButton(onClick = { Installer.share(ctx, r.apk) }) { Text("Share") }
                    }
                }
            }
        }
    }
}

private val Stage0 = com.xapk2apk.tool.core.Stage.IDLE
