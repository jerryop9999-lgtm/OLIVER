package com.xapk2apk.tool.core

import android.content.Context
import android.net.Uri
import java.io.File

class ConversionEngine(private val ctx: Context) {

    fun convert(
        uri: Uri,
        displayName: String,
        sign: Boolean,
        signCfg: SignConfig,
        onProgress: (Stage, Float) -> Unit
    ): ConversionResult {
        val work = File(ctx.cacheDir, "work").apply { deleteRecursively(); mkdirs() }
        try {
            onProgress(Stage.COPY, 0f)
            val input = File(work, "input.bin")
            ctx.contentResolver.openInputStream(uri)?.use { i -> input.outputStream().use { i.copyTo(it) } }
                ?: throw ConvertException("Cannot open the selected file")

            onProgress(Stage.DETECT, 0f)
            val kind = BundleDetector.detect(input)
            if (kind == BundleKind.APK) throw ConvertException("This is already a single APK")

            onProgress(Stage.EXTRACT, 0f)
            val bundle = BundleExtractor.extract(input, kind, work) { onProgress(Stage.EXTRACT, it) }
            val splitCount = bundle.splitsDir.listFiles()?.size ?: 0

            val merged: File = bundle.universal ?: File(work, "merged.apk").also {
                onProgress(Stage.MERGE, 0f)
                SplitMerger.merge(bundle.splitsDir, it)
            }

            val outDir = File(ctx.getExternalFilesDir(null), "output").apply { mkdirs() }
            val base = displayName.substringBeforeLast('.').ifBlank { "converted" }
            val final = File(outDir, "$base.apk")

            if (sign) {
                onProgress(Stage.SIGN, 0f)
                ApkSignerService.sign(ctx, merged, final, signCfg)
            } else {
                merged.copyTo(final, overwrite = true)
            }

            val obbCopies = bundle.obbs.map { o ->
                File(outDir, "obb/${bundle.packageName ?: base}/${o.name}").also {
                    it.parentFile?.mkdirs(); o.copyTo(it, overwrite = true)
                }
            }
            return ConversionResult(final, bundle.packageName, obbCopies, splitCount, kind)
        } finally {
            work.deleteRecursively()
        }
    }
}
