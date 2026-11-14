package com.xapk2apk.tool.data

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var sign: Boolean
        get() = sp.getBoolean("sign", true)
        set(v) = sp.edit().putBoolean("sign", v).apply()
    var saveToDownloads: Boolean
        get() = sp.getBoolean("downloads", true)
        set(v) = sp.edit().putBoolean("downloads", v).apply()
    var includeObb: Boolean
        get() = sp.getBoolean("obb", true)
        set(v) = sp.edit().putBoolean("obb", v).apply()
    var ksAlias: String
        get() = sp.getString("ks_alias", "debug") ?: "debug"
        set(v) = sp.edit().putString("ks_alias", v).apply()
    var ksPass: String
        get() = sp.getString("ks_pass", "android") ?: "android"
        set(v) = sp.edit().putString("ks_pass", v).apply()
}
