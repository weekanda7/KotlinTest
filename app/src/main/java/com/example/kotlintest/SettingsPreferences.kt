package com.example.kotlintest

import android.content.Context

object SettingsPreferences {

    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_AUTO_REFRESH_ENABLED = "auto_refresh_enabled"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isNotificationsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isAutoRefreshEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_REFRESH_ENABLED, false)

    fun setAutoRefreshEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_REFRESH_ENABLED, enabled).apply()
    }
}
