package com.xapk2apk.tool.core

import org.json.JSONObject
import java.io.File
import java.util.zip.ZipFile

class ExtractedBundle(
    val packageName: String?,
    val splitsDir: File,
    val obbs: List<File>,
    val universal: File?
)

object BundleExtractor {

    fun extract(src: File, kind: BundleKind, work: File, onProgress: (Float) -> Unit): ExtractedBundle {
        val raw = File(work, "raw").apply { mkdirs() }
        val splits = File(work, "splits").apply { mkdirs() }

        ZipFile(src).use { z ->
            val entries = z.entries().asSequence().filter { !it.isDirectory }.toList()
            entries.forEachIndexed { i, e ->
                val out = File(raw, e.name).canonicalFile
                if (!out.path.startsWith(raw.canonicalPath + File.separator)) {
                    throw ConvertException("Unsafe path in archive: ${e.name}")
                }
                out.parentFile?.mkdirs()
                z.getInputStream(e).use { input -> out.outputStream().use { input.copyTo(it) } }
                onProgress((i + 1f) / entries.size)
            }
        }

        var pkg: String? = null
        var universal: File? = null
        val apks: List<File> = when (kind) {
            BundleKind.XAPK -> {
                val json = JSONObject(File(raw, "manifest.json").readText())
                pkg = json.optString("package_name").ifBlank { null }
                val arr = json.optJSONArray("split_apks")
                val listed = buildList {
                    if (arr != null) for (i in 0 until arr.length()) {
                        arr.getJSONObject(i).optString("file").takeIf { it.isNotBlank() }?.let { add(File(raw, it)) }
                    }
                }
                if (listed.isNotEmpty()) listed else raw.listFiles { f -> f.extension == "apk" }.orEmpty().sorted()
            }
            BundleKind.APKM -> raw.listFiles { f -> f.extension == "apk" }.orEmpty().sorted()
            else -> {
                universal = raw.walkTopDown().firstOrNull { it.name == "universal.apk" }
                raw.walkTopDown().filter {
                    it.isFile && it.extension == "apk" && it.name != "universal.apk" &&
                        !it.path.contains("${File.separator}standalones${File.separator}")
                }.toList()
            }
        }.filter { it.exists() }

        if (apks.isEmpty() && universal == null) throw ConvertException("No APK files found in bundle")
        apks.forEach { it.copyTo(File(splits, it.name), overwrite = true) }

        val obbs = raw.walkTopDown().filter { it.isFile && it.extension == "obb" }.toList()
        return ExtractedBundle(pkg, splits, obbs, universal)
    }
}
