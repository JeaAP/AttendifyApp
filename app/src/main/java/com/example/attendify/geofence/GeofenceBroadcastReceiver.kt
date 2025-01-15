package com.example.attendify.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        Log.d("GeofenceReceiver", "onReceive called")
//        Log.d("GeofenceReceiver", "Intent data: ${intent.extras}")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e("GeofenceReceiverNull", "Error: geofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val transitionMessage = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Masuk ke dalam geofence"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Keluar dari geofence"
            else -> "Perubahan geofence tidak diketahui"
        }

        Toast.makeText(context, transitionMessage, Toast.LENGTH_SHORT).show()
//        Log.d("GeofenceReceiver", transitionMessage)

//        val location = geofencingEvent.triggeringLocation
//
//        Log.d(
//            "GeofenceReceiver",
//            "Transition: $geofenceTransition, Latitude: ${location?.latitude}, Longitude: ${location?.longitude}"
//        )
    }
}