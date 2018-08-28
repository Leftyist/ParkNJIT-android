package com.arianfarahani.parknjit.geofencing

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.arianfarahani.parknjit.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes

/**
 * Shows a [Snackbar] using `text`.
 *
 * @param text The Snackbar text.
 */
fun showSnackbar(context: Context, text: String) {
    val container = (context as AppCompatActivity).findViewById<View>(android.R.id.content)
    if (container != null) {
        Snackbar.make(container, text, Snackbar.LENGTH_LONG).show()
    }

}

/**
 * Shows a [Snackbar].
 *
 * @param mainTextStringId The id for the string resource for the Snackbar text.
 * @param actionStringId   The text of the action item.
 * @param listener         The listener associated with the Snackbar action.
 */
fun showSnackbar(
    context: Context,
    mainTextStringId: Int,
    actionStringId: Int,
    listener: View.OnClickListener
) {
    Snackbar.make(
        (context as AppCompatActivity).findViewById<View>(android.R.id.content),
        context.getString(mainTextStringId),
        Snackbar.LENGTH_INDEFINITE
    ).setAction(
        context.getString(actionStringId),
        listener
    ).show()
}

fun getErrorString(context: Context, e: Exception): String {

    return if (e is ApiException) {
        getErrorString(
            context,
            e.statusCode
        )
    } else {
        context.getResources().getString(R.string.unknown_geofence_error)
    }

}


/**
 * Returns the error string for a geofencing error code.
 */
fun getErrorString(context: Context, errorCode: Int): String {
    val mResources = context.getResources()
    when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return mResources.getString(R.string.geofence_not_available)
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return mResources.getString(R.string.geofence_too_many_geofences)
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return mResources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> return mResources.getString(R.string.unknown_geofence_error)
    }
}