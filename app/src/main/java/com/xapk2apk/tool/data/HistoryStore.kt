package com.xapk2apk.tool.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class HistoryItem(
    val name: String,
    val packageName: String?,
    val path: String,
    val sizeBytes: Long,
    val time: Long
)

class HistoryStore(ctx: Context) {
    private val file = File(ctx.filesDir, "history.json")

    fun load(): List<HistoryItem> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                HistoryItem(
                    o.getString("name"), o.optString("pkg").ifBlank { null },
                    o.getString("path"), o.getLong("size"), o.getLong("time")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun add(item: HistoryItem) {
        val list = (listOf(item) + load()).take(100)
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("name", it.name).put("pkg", it.packageName ?: "")
                .put("path", it.path).put("size", it.sizeBytes).put("time", it.time))
        }
        file.writeText(arr.toString())
    }

    fun remove(path: String) {
        val list = load().filterNot { it.path == path }
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("name", it.name).put("pkg", it.packageName ?: "")
                .put("path", it.path).put("size", it.sizeBytes).put("time", it.time))
        }
        file.writeText(arr.toString())
        File(path).delete()
    }

    fun clear() { file.delete() }
}
