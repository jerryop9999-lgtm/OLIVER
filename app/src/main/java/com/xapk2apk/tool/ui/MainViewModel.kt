package com.xapk2apk.tool.ui

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xapk2apk.tool.core.*
import com.xapk2apk.tool.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

data class Picked(val uri: Uri, val name: String, val size: Long)

class MainViewModel(private val app: Application) : AndroidViewModel(app) {
    private val engine = ConversionEngine(app)
    val prefs = Prefs(app)
    private val store = HistoryStore(app)

    var picked by mutableStateOf<Picked?>(null); private set
    var stage by mutableStateOf(Stage.IDLE); private set
    var progress by mutableStateOf(0f); private set
    var busy by mutableStateOf(false); private set
    var result by mutableStateOf<ConversionResult?>(null); private set
    var savedTo by mutableStateOf<String?>(null); private set
    var error by mutableStateOf<String?>(null); private set
    var history by mutableStateOf(store.load()); private set

    // settings mirrors (Compose-observable)
    var sign by mutableStateOf(prefs.sign)
    var saveToDownloads by mutableStateOf(prefs.saveToDownloads)
    var includeObb by mutableStateOf(prefs.includeObb)
    var ksAlias by mutableStateOf(prefs.ksAlias)
    var ksPass by mutableStateOf(prefs.ksPass)
    var hasCustomKeystore by mutableStateOf(customKeystoreFile().exists())

    private fun customKeystoreFile() = File(app.filesDir, "user.p12")

    fun pick(uri: Uri) {
        var name = "bundle"; var size = 0L
        app.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = c.getString(it) }
                c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = c.getLong(it) }
            }
        }
        picked = Picked(uri, name, size)
        result = null; error = null; savedTo = null; stage = Stage.IDLE; progress = 0f
    }

    fun convert() {
        val p = picked ?: return
        if (busy) return
        busy = true; error = null; result = null; savedTo = null
        prefs.sign = sign; prefs.saveToDownloads = saveToDownloads; prefs.includeObb = includeObb
        prefs.ksAlias = ksAlias; prefs.ksPass = ksPass
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ks = customKeystoreFile().takeIf { it.exists() }
                val r = engine.convert(p.uri, p.name, sign, SignConfig(ks, ksAlias, ksPass)) { s, pr ->
                    stage = s; progress = pr
                }
                stage = Stage.SAVE
                val obbs = if (includeObb) r.obbs else emptyList()
                if (saveToDownloads) savedTo = OutputSaver.saveToDownloads(app, r.apk, obbs, r.packageName)
                store.add(HistoryItem(r.apk.name, r.packageName, r.apk.absolutePath, r.apk.length(), System.currentTimeMillis()))
                history = store.load()
                result = r; stage = Stage.DONE; progress = 1f
            } catch (t: Throwable) {
                error = t.message ?: t.javaClass.simpleName
                stage = Stage.IDLE
            } finally {
                busy = false
            }
        }
    }

    fun importKeystore(uri: Uri) {
        app.contentResolver.openInputStream(uri)?.use { i -> customKeystoreFile().outputStream().use { i.copyTo(it) } }
        hasCustomKeystore = true
    }

    fun removeKeystore() { customKeystoreFile().delete(); hasCustomKeystore = false }
    fun deleteHistory(item: HistoryItem) { store.remove(item.path); history = store.load() }
    fun clearHistory() { history.forEach { File(it.path).delete() }; store.clear(); history = emptyList() }
}
