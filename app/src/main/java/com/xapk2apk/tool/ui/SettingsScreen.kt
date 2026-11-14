package com.xapk2apk.tool.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun SwitchRow(title: String, sub: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(sub, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val ksPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importKeystore(uri)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        SwitchRow("Sign APK", "Required to install the merged APK", vm.sign) { vm.sign = it }
        SwitchRow("Save to Downloads", "Copy result to Download/OLIVER", vm.saveToDownloads) { vm.saveToDownloads = it }
        SwitchRow("Include OBB files", "Export OBB expansion files too", vm.includeObb) { vm.includeObb = it }

        HorizontalDivider()
        Text("Keystore (PKCS12 .p12)", style = MaterialTheme.typography.titleMedium)
        Text(if (vm.hasCustomKeystore) "Custom keystore imported" else "Using built-in debug keystore",
            style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(vm.ksAlias, { vm.ksAlias = it }, label = { Text("Alias") }, singleLine = true,
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vm.ksPass, { vm.ksPass = it }, label = { Text("Password") }, singleLine = true,
            modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { ksPicker.launch(arrayOf("*/*")) }) { Text("Import keystore") }
            if (vm.hasCustomKeystore) TextButton(onClick = vm::removeKeystore) { Text("Remove") }
        }

        HorizontalDivider()
        Text("About", style = MaterialTheme.typography.titleMedium)
        Text("OLIVER 1.0.0 • merging by APKEditor, signing by apksig",
            style = MaterialTheme.typography.bodySmall)
    }
}
