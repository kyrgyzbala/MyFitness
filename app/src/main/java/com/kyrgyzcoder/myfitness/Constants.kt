package com.kyrgyzcoder.myfitness

const val ERROR_DIALOG_REQUEST = 9001
const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002
const val PERMISSIONS_REQUEST_ENABLE_GPS = 9003

const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
const val DEFAULT_ZOOM = 16f

const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

const val CHANNEL_ID = "channel_01"
const val NOTIFICATION_ID = 12345678

const val EXTRA_STARTED_FROM_NOTIFICATION = " EXTRA_STARTED_FROM_NOTIFICATION"
const val ACTION_BROADCAST = "ACTION_BROADCAST"

const val EXTRA_LOCATION = "EXTRA_LOCATION"
const val KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES"
const val REQUEST_PERMISSIONS_REQUEST_CODE = 99

/**
 * The desired interval for location updates. Inexact. Updates may be more or less frequent.
 */
const val UPDATE_INTERVAL: Long = 15000 /* 15 seconds*/

/**
 * The fastest rate for active location updates. Updates will never be more frequent
 * than this value, but they may be less frequent.
 */
const val FASTEST_UPDATE_INTERVAL: Long = 5000 /* 5 seconds*/

const val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 //every 5 minutes


private const val MINIMUM_DISTANCE_CHANGE_FOR_UPDATES: Long = 1

// Meters
private const val MINIMUM_TIME_BETWEEN_UPDATES: Long = 1000


