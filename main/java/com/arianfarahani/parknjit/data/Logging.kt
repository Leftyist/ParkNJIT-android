package com.arianfarahani.parknjit.data

import android.content.Context
import android.content.SharedPreferences
import com.arianfarahani.parknjit.R
import com.google.firebase.analytics.FirebaseAnalytics

fun SharedPreferences.logProperties(context: Context, analytics: FirebaseAnalytics) {
    logProperty(context, analytics, context.getString(R.string.pref_geofencing_key))
    logProperty(context, analytics, context.getString(R.string.pref_geofencing_tts_key))
}

fun SharedPreferences.logProperty(context: Context, analytics: FirebaseAnalytics, prefKey: String) {
    val key: String
    val value: String
    if (prefKey == context.getString(R.string.pref_geofencing_key)) {
        key = context.getString(R.string.firebase_geofencing_property)
        val enabled = this.getBoolean(
            prefKey,
            context.getString(R.string.pref_geofencing_default).toBoolean()
        )
        if (enabled)
            value = "Yes"
        else
            value = "No"

    } else if (prefKey == context.getString(R.string.pref_geofencing_tts_key)) {
        key = context.getString(R.string.firebase_geofencing_tts_property)
        val enabled = this.getBoolean(
            prefKey,
            context.getString(R.string.pref_geofencing_default).toBoolean()
        )
        if (enabled)
            value = "Yes"
        else
            value = "No"

    } else {
        throw IllegalArgumentException("Must be a supported setting!")
    }

    analytics.setUserProperty(key, value)
}