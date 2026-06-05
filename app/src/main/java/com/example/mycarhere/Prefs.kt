package com.example.mycarhere

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val NAME = "mycarhere_prefs"
    private const val KEY_RECORD_SECONDS    = "record_seconds"
    private const val KEY_MULTI_LOCATION    = "multi_location"
    private const val KEY_MULTI_PHOTO       = "multi_photo"
    private const val KEY_MULTI_VOICE       = "multi_voice"
    private const val KEY_AUTO_RECORD       = "auto_record_on_launch"
    private const val KEY_AUTO_PLAY         = "auto_play_on_launch"

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

    /** 앱 실행 시 자동 녹음 여부 (기본 OFF) */
    var Context.autoRecordOnLaunch: Boolean
        get() = sp(this).getBoolean(KEY_AUTO_RECORD, false)
        set(v) = sp(this).edit().putBoolean(KEY_AUTO_RECORD, v).apply()

    /** 앱 실행 시 기존 음성 자동 재생 여부 (기본 OFF) */
    var Context.autoPlayOnLaunch: Boolean
        get() = sp(this).getBoolean(KEY_AUTO_PLAY, false)
        set(v) = sp(this).edit().putBoolean(KEY_AUTO_PLAY, v).apply()
}
