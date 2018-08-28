package com.arianfarahani.parknjit.geofencing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.text.TextUtils
import android.util.Log
import com.arianfarahani.parknjit.MainActivity
import com.arianfarahani.parknjit.R
import com.arianfarahani.parknjit.data.LotObject
import com.arianfarahani.parknjit.realtime.RealtimeWebService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.Serializable


class GeofenceTransitionIntentService() : JobIntentService() {

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = getErrorString(
                this,
                geofencingEvent.errorCode
            )
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails =
                getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences)

            RealtimeWebService.refreshLiveData(object : RealtimeWebService.RealTimeDirectPull {
                override fun onReturn(data: MutableList<LotObject>?) {
                    if (data != null)
                        sendNotification(data)
                    else
                        Log.e(TAG, "Error downloading data")
                }
            })

            Log.i(TAG, geofenceTransitionDetails)
        } else {
            Log.e(
                TAG,
                getString(R.string.geofence_transition_invalid_type, geofenceTransition)
            )
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private fun sendNotification(lots: MutableList<LotObject>) {

        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        val text = String.format("The %s has %d spots available", lots[0].name, lots[0].available)

        // Cache lot data so when user clicks notification, app doesn't re-download data
        notificationIntent.putExtra("lots", lots as Serializable)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(this)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(this)

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setColor(Color.RED)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(notificationPendingIntent)
            .setChannelId("park_njit_main_channel")

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Get an instance of the Notification manager
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //create channel to issue on
        if (android.os.Build.VERSION.SDK_INT >= 26) makeNotificationChannel(mNotificationManager)

        // Issue the notification
        mNotificationManager.notify(0, builder.build())

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val tts_enabled = prefs.getBoolean(
            getString(R.string.pref_geofencing_tts_key),
            getString(R.string.pref_geofencing_tts_default).toBoolean()
        )
        Log.d(TAG, "TTS status: $tts_enabled")
        if (tts_enabled) {
            val intent = Intent(applicationContext, TTS::class.java)
            intent.putExtra(TTS.TEXT_INTENT_KEY, text)
            applicationContext.startService(intent)
        }


        FirebaseAnalytics.getInstance(this).logEvent("Sent_Geofence_Notification", null)
    }

    private fun makeNotificationChannel(mNotificationManager: NotificationManager) {
        if (android.os.Build.VERSION.SDK_INT < 26)
            return

        // The id of the channel.
        val id = "park_njit_main_channel"

        // The user-visible name of the channel.
        val name = "Lot Status Update"

        // The user-visible description of the channel.
        val description =
            "This notification goes off as you get close to campus in order to show you the most available parking."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(id, name, importance)

        // Configure the notification channel.
        mChannel.description = description
        mChannel.enableLights(true)

        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(200, 400, 200, 400)
        mNotificationManager.createNotificationChannel(mChannel)
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private fun getTransitionString(transitionType: Int): String {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> return getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT -> return getString(R.string.geofence_transition_exited)
            Geofence.GEOFENCE_TRANSITION_DWELL -> return getString(R.string.geofence_transition_dwelled)
            else -> return getString(R.string.unknown_geofence_transition)
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {

        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString
    }

    companion object {
        val TAG = "ParkNJITGeofenceService"
        val JOB_ID = 1000


        // Convenience method for enqueuing work in to this service.
        fun enqueueWork(context: Context, work: Intent) {
            try {
                JobIntentService.enqueueWork(
                    context,
                    GeofenceTransitionIntentService::class.java,
                    JOB_ID,
                    work
                )
            } catch (e: Exception) {
                Log.w(TAG, "Couldn't enqueue work!")
            }

        }
    }
}