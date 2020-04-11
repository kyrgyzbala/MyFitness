package com.kyrgyzcoder.myfitness.utils

import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.kyrgyzcoder.myfitness.KEY_REQUESTING_LOCATION_UPDATES

internal object Utils {
    private var list: MutableList<LatLng> = mutableListOf()
    fun addToList(latLng: LatLng) {
        list.add(latLng)
    }

    fun getListOfLocations(): MutableList<LatLng> {
        return list
    }

    fun setDrawPolyline() {

    }

    fun requestingLocationUpdates(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            KEY_REQUESTING_LOCATION_UPDATES, false
        )
    }

    fun setRequestingLocationUpdates(context: Context, set: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
            KEY_REQUESTING_LOCATION_UPDATES, set
        ).apply()
    }

    fun getLocationText(location: Location?): CharSequence {
        return if (location == null)
            "Unknown Location"
        else
            "[ ${location.latitude} , ${location.longitude} ]"
    }

}