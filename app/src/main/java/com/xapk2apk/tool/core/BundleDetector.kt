package com.xapk2apk.tool.core

import java.io.File
import java.util.zip.ZipFile

object BundleDetector {
    fun detect(file: File): BundleKind {
        val names = try {
            ZipFile(file).use { z -> z.entries().asSequence().map { it.name }.toList() }
        } catch (e: Exception) {
            throw ConvertException("Not a valid zip/bundle file", e)
        }
        val set = names.toHashSet()
        return when {
            "AndroidManifest.xml" in set && "classes.dex" in set -> BundleKind.APK
            "manifest.json" in set -> BundleKind.XAPK
            "info.json" in set -> BundleKind.APKM
            "toc.pb" in set || names.any { it.startsWith("splits/") } -> BundleKind.APKS
            names.any { it.endsWith(".apk") } -> BundleKind.APKS
            else -> throw ConvertException("Unknown bundle layout: no APK files inside")
        }
    }
}
