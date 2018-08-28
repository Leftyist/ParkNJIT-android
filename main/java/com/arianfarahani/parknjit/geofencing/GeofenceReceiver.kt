package com.arianfarahani.parknjit.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent


class GeofenceReceiver : BroadcastReceiver() {

    companion object {
        val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            Log.e(TAG, geofencingEvent.errorCode.toString())

        } else {

            try {
                Log.i(
                    TAG,
                    geofencingEvent.triggeringLocation.latitude.toString() + ", " + geofencingEvent.triggeringLocation.longitude
                )

                GeofenceTransitionIntentService.enqueueWork(context, intent)
            } catch (e: Exception) {

            }
        }
    }
}