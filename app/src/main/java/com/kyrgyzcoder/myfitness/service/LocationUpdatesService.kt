package com.kyrgyzcoder.myfitness.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.kyrgyzcoder.myfitness.*
import com.kyrgyzcoder.myfitness.R
import com.kyrgyzcoder.myfitness.utils.Utils

class LocationUpdatesService : Service() {

    private val TAG = "Nur: Location Service->"
    private var mChangingConfiguration = false
    private val mBinder: IBinder = LocalBinder()

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var currentLocation: Location
    private lateinit var mServiceHandler: Handler
    private lateinit var mNotificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "in onBind()")
        stopForeground(true)
        mChangingConfiguration = true
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult!!.lastLocation)
            }
        }

        createLocationRequest()
        getLastLocation()


        val handlerThread =
            HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called")
        val startedFromNotification = intent!!.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )
        // We got here because the user decided to remove location updates from the notification.

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "in onRebind()")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Last client unbound from service")
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.d(TAG, "inside onUnbid(), starting foreground service")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true
    }

    override fun onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null)
    }


    /**
     * Private functions start here
     */

    //Requests location updates
    //this function will be called from Run Fragment
    fun requestLocationUpdates() {
        Log.d(TAG, "Requesting Location Updates")

        Utils.setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
        try {
            mFusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
        } catch (ff: SecurityException) {
            Utils.setRequestingLocationUpdates(this, false)
            Log.d(TAG, "Lost location permission $ff")
        }
    }

    fun removeLocationUpdates() {
        Log.i(TAG, "Removing location updates")
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
            Utils.setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            Utils.setRequestingLocationUpdates(this, true)
            Log.e(
                TAG, "Lost location permission. Could not remove updates. $unlikely"
            )
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private fun getNotification(): Notification {
        Log.d(TAG, "getNotification() called")

        val intent = Intent(this, LocationUpdatesService::class.java)
        val text = Utils.getLocationText(currentLocation)

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent =
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // The PendingIntent to launch activity.
        val activityPendingIntent =
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .addAction(
                R.mipmap.ic_launch,
                getString(R.string.laucnh_activity),
                activityPendingIntent
            )
            .addAction(R.mipmap.ic_cancel, getString(R.string.stop_running), servicePendingIntent)
            .setContentText(text)
            .setContentTitle(getString(R.string.running))
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH) //used deprecated cuz target SDK is 21 (
            .setSmallIcon(R.mipmap.ic_launcher)
            .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }
        return builder.build()
    }


    private fun getLastLocation() {
        try {
            mFusedLocationProviderClient.lastLocation
                .addOnCompleteListener(OnCompleteListener<Location?> { task ->
                    if (task.isSuccessful && task.result != null) {
                        currentLocation = task.result!!
                    } else {
                        Log.w(
                            TAG,
                            "Failed to get location."
                        )
                    }
                })
        } catch (unlikely: SecurityException) {
            Log.e(
                TAG,
                "Lost location permission.$unlikely"
            )
        }
    }


    private fun onNewLocation(lastLocation: Location?) {
        Log.i(TAG, "New location: $lastLocation")

        currentLocation = lastLocation!!

        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, lastLocation)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)


        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification())
        }
    }

    /**
     * Sets the location request parameters.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = UPDATE_INTERVAL
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        fun getService(): LocationUpdatesService {
            return this@LocationUpdatesService
        }
    }

    /**
     * Returns true if this is a foreground service.
     */
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager: ActivityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }
}