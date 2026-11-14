package com.xapk2apk.tool.core

import java.io.File

enum class BundleKind { XAPK, APKS, APKM, APK }

enum class Stage(val label: String) {
    IDLE("Ready"),
    COPY("Reading file"),
    DETECT("Detecting format"),
    EXTRACT("Extracting"),
    MERGE("Merging splits"),
    SIGN("Signing"),
    SAVE("Saving"),
    DONE("Done")
}

class ConvertException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class ConversionResult(
    val apk: File,
    val packageName: String?,
    val obbs: List<File>,
    val splitCount: Int,
    val kind: BundleKind
)
