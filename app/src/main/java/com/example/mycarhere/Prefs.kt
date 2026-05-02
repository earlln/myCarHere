package com.example.mycarhere

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val NAME = "mycarhere_prefs"
    private const val KEY_RECORD_SECONDS = "record_seconds"
    private const val KEY_MULTI_LOCATION = "multi_location"
    private const val KEY_MULTI_PHOTO = "multi_photo"
    private const val KEY_MULTI_VOICE = "multi_voice"

    private fun sp(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var Context.recordSeconds: Int
        get() = sp(this).getInt(KEY_RECORD_SECONDS, 5)
        set(v) = sp(this).edit().putInt(KEY_RECORD_SECONDS, v).apply()

    var Context.allowMultiLocation: Boolean
        get() = sp(this).getBoolean(KEY_MULTI_LOCATION, false)
        set(v) = sp(this).edit().putBoolean(KEY_MULTI_LOCATION, v).apply()

    var Context.allowMultiPhoto: Boolean
        get() = sp(this).getBoolean(KEY_MULTI_PHOTO, false)
        set(v) = sp(this).edit().putBoolean(KEY_MULTI_PHOTO, v).apply()

    var Context.allowMultiVoice: Boolean
        get() = sp(this).getBoolean(KEY_MULTI_VOICE, false)
        set(v) = sp(this).edit().putBoolean(KEY_MULTI_VOICE, v).apply()
}
