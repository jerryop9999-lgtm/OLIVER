package com.xapk2apk.tool.core

import java.io.File

/**
 * Merges split APKs with the APKEditor jar placed in app/libs.
 * Called reflectively so the project still compiles when the jar is absent.
 */
object SplitMerger {
    fun merge(splitsDir: File, out: File) {
        val cls = try {
            Class.forName("com.reandroid.apkeditor.Main")
        } catch (e: ClassNotFoundException) {
            throw ConvertException("APKEditor jar missing: put APKEditor-x.y.z.jar into app/libs and rebuild", e)
        }
        val args = arrayOf("m", "-i", splitsDir.absolutePath, "-o", out.absolutePath, "-f")
        try {
            val method = runCatching { cls.getMethod("execute", Array<String>::class.java) }
                .getOrElse { cls.getMethod("main", Array<String>::class.java) }
            method.invoke(null, args as Any)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw ConvertException("Merge failed: ${e.targetException?.message}", e.targetException)
        } catch (e: Exception) {
            throw ConvertException("Merge failed: ${e.message}", e)
        }
        if (!out.exists() || out.length() == 0L) throw ConvertException("Merge produced no output")
    }
}
