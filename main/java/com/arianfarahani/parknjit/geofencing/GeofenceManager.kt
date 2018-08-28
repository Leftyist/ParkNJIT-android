package com.arianfarahani.parknjit.geofencing

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.arianfarahani.parknjit.BuildConfig
import com.arianfarahani.parknjit.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


class GeofenceManager(val context: Context) : ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = this.javaClass.simpleName
    private val PACKAGE_NAME = "com.arianfarahani.parknjit.geofencing"
    val GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum class PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    private val mGeofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private var mPendingGeofenceTask = PendingGeofenceTask.NONE

    fun add() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }

        addGeofences();
    }

    fun remove() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }

        removeGeofences();
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private fun addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(context, context.getString(R.string.insufficient_permissions))
            return
        }

        //if (!getGeofencesAdded()) {
        mGeofencingClient.addGeofences(
            getGeofencingRequest(),
            getGeofencePendingIntent()
        ).addOnCompleteListener(object : OnCompleteListener<Void> {
            override fun onComplete(p0: Task<Void>) {
                mPendingGeofenceTask = PendingGeofenceTask.NONE
                if (p0.isSuccessful()) {
                    updateGeofencesAdded(true)
                    Log.i(TAG, context.getString(R.string.geofences_added))
                } else {
                    // Get the status code for the error and log it using a user-friendly message.
                    val errorMessage = getErrorString(
                        context,
                        p0.exception as kotlin.Exception
                    )
                    Log.w(TAG, errorMessage)
                }
            }

        })
        //} else
        //Log.i(TAG, "Geofence already added.")
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private fun removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(context, context.getString(R.string.insufficient_permissions))
            return
        }

        if (getGeofencesAdded())
            mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(
                object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        mPendingGeofenceTask = PendingGeofenceTask.NONE
                        if (p0.isSuccessful()) {
                            updateGeofencesAdded(false)
                            Log.i(TAG, context.getString(R.string.geofences_removed))
                        } else {
                            // Get the status code for the error and log it using a user-friendly message.
                            val errorMessage = getErrorString(
                                context,
                                p0.exception as kotlin.Exception
                            )
                            Log.w(TAG, errorMessage)
                        }
                    }
                })
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private fun getGeofencesAdded(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            GEOFENCES_ADDED_KEY,
            false
        )
    }

    /**
     * Stores whether geofences were added ore removed in [SharedPreferences];
     *
     * @param added Whether geofences were added or removed.
     */
    private fun updateGeofencesAdded(added: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(GEOFENCES_ADDED_KEY, added)
            .apply()
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the NJIT campus geofence.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private fun getGeofencingRequest(): GeofencingRequest {
        val latitude = 40.742303
        val longitude = -74.178487
        val radius_in_meters = 3200f //roughly 2 miles
        val notification_delay = 1000 //1 seconds
        val requestID = "com.parknjit.geofence"
        val mGeofenceBuilder = Geofence.Builder()
            .setRequestId(requestID)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setCircularRegion(latitude, longitude, radius_in_meters)
            .setNotificationResponsiveness(notification_delay)
            .build()

        val fenceList = ArrayList<Geofence>()
        fenceList.add(mGeofenceBuilder)

        val mGeofencingRequestBuilder = GeofencingRequest.Builder()
        mGeofencingRequestBuilder.addGeofences(fenceList)
        mGeofencingRequestBuilder.setInitialTrigger(Geofence.GEOFENCE_TRANSITION_DWELL)
        return mGeofencingRequestBuilder.build()
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private fun performPendingGeofenceTask() {
        if (mPendingGeofenceTask === PendingGeofenceTask.ADD) {
            addGeofences()
        } else if (mPendingGeofenceTask === PendingGeofenceTask.REMOVE) {
            removeGeofences()
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as AppCompatActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(context, R.string.permission_rationale,
                android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.")
                performPendingGeofenceTask()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(context, R.string.permission_denied_explanation,
                    R.string.action_settings,
                    object : View.OnClickListener {
                        override fun onClick(view: View) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(context, intent, null)
                        }
                    })
                mPendingGeofenceTask = PendingGeofenceTask.NONE
            }
        }
    }

}