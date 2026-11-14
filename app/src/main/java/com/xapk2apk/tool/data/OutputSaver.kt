package com.xapk2apk.tool.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

object OutputSaver {
    /** Copies the APK (and OBBs) to Downloads/XAPK2APK. Returns a human-readable location. */
    fun saveToDownloads(ctx: Context, apk: File, obbs: List<File>, pkg: String?): String {
        if (Build.VERSION.SDK_INT >= 29) {
            insert(ctx, apk, "application/vnd.android.package-archive", "Download/OLIVER")
            obbs.forEach { insert(ctx, it, "application/octet-stream", "Download/OLIVER/obb/${pkg ?: "app"}") }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "XAPK2APK")
            dir.mkdirs()
            apk.copyTo(File(dir, apk.name), overwrite = true)
            obbs.forEach { it.copyTo(File(dir, "obb/${pkg ?: "app"}/${it.name}").also { f -> f.parentFile?.mkdirs() }, true) }
        }
        return "Download/OLIVER/${apk.name}"
    }

    private fun insert(ctx: Context, file: File, mime: String, relPath: String) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
        }
        val uri = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
        ctx.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
    }
}
