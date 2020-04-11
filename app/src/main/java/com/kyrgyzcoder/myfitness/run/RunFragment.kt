package com.kyrgyzcoder.myfitness.run

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.kyrgyzcoder.myfitness.*
import com.kyrgyzcoder.myfitness.model.Runs
import com.kyrgyzcoder.myfitness.service.LocationUpdatesService
import com.kyrgyzcoder.myfitness.utils.ResultDialog
import com.kyrgyzcoder.myfitness.utils.Utils
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class RunFragment : Fragment(), OnMapReadyCallback, ResultDialog.ResultDialogListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance() = RunFragment()
    }

    private val TAG = "RUN Fragment"

    private lateinit var viewModel: RunViewModel

    private lateinit var list: MutableList<LatLng>

    //UI widgets
    private lateinit var mMapView: MapView
    private lateinit var gMap: GoogleMap
    private lateinit var mStartButton: Button
    private lateinit var mEndButton: Button

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var mStartLocation: LatLng
    private lateinit var mEndLocation: LatLng
    private var startTime: Long = 0
    private var timeRun: Int = 0
    private lateinit var runDate: Date
    private var distance: Int = 0
    private var running: Boolean = false

    private lateinit var currentLatLng: LatLng

    private lateinit var myReceiver: MyReceiver
    private var mService: LocationUpdatesService? = null
    private var mBound: Boolean = false

    // Monitors the state of the connection to the service.
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationUpdatesService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.run_fragment, container, false)
        Log.d("NURI", "onCreate View")
        currentLatLng = LatLng(0.0, 0.5)
        list = mutableListOf() //empty list

        mMapView = view.findViewById(R.id.mapView)

        myReceiver = MyReceiver()

        // Check that the user hasn't revoked permissions by going to Settings. User tupoi
        if (Utils.requestingLocationUpdates(activity!!)) {
            if (!checkPermissions())
                requestPermission()
        }

        mStartButton = view.findViewById(R.id.buttonStart)
        mEndButton = view.findViewById(R.id.buttonEnd)
        mEndButton.visibility = View.INVISIBLE
        initGoogleMap(savedInstanceState)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RunViewModel::class.java)
        PreferenceManager.getDefaultSharedPreferences(activity)
            .registerOnSharedPreferenceChangeListener(this)
        mStartButton.setOnClickListener {
            distance = 0
            startButtonClick()
        }
        mEndButton.setOnClickListener {
            endButtonClick()
        }

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        activity!!.bindService(
            Intent(activity, LocationUpdatesService::class.java),
            mServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermission()
            return
        }

        if (googleMap != null) {
            Log.d("NURI", "onMapReady")
            gMap = googleMap
            gMap.isMyLocationEnabled = true
            gMap.uiSettings.isMyLocationButtonEnabled = true
            getCurrentLocation()
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mMapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        Log.d("NURI", "onResume")

        mMapView.onResume()

        LocalBroadcastManager.getInstance(this.context!!).registerReceiver(
            myReceiver, IntentFilter(
                ACTION_BROADCAST
            )
        )
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onStop() {
        if (mBound) {
            activity!!.unbindService(mServiceConnection)
            mBound = false
        }
        PreferenceManager.getDefaultSharedPreferences(this.requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
        mMapView.onStop()
    }

    override fun onPause() {
        mMapView.onPause()
        Log.d("NURI", "OnPause()")
        LocalBroadcastManager.getInstance(this.requireContext()).unregisterReceiver(myReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        mMapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    /**
     * Handles start button clicked
     */
    private fun startButtonClick() {
        running = true
        list.clear()     //make it empty at the beginning of each run
        getCurrentLocation()
        list.add(currentLatLng)
        if (!checkPermissions()) {
            requestPermission()
        } else {
            mService!!.requestLocationUpdates()
        }
        mStartButton.visibility = View.INVISIBLE
        mStartButton.isEnabled = false
        mEndButton.isEnabled = true
        mEndButton.visibility = View.VISIBLE
        mStartLocation = currentLatLng
        Log.d("NUR", "Start location is -> $mStartLocation")

        val cal = Calendar.getInstance()
        startTime = cal.timeInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentT = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
            currentT.format(formatter)
            runDate = Date.from(currentT.atZone(ZoneId.systemDefault()).toInstant())
        } else {
            val formatter = SimpleDateFormat(DATE_FORMAT, Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            runDate = Calendar.getInstance().time
        }
    }

    /**
     *  Handles end button clicked
     */

    private fun endButtonClick() {
        running = false

        mService!!.removeLocationUpdates()
        mStartButton.isEnabled = true
        mStartButton.visibility = View.VISIBLE
        mEndButton.isEnabled = false
        mEndButton.visibility = View.INVISIBLE
        getCurrentLocation()
        mEndLocation = currentLatLng
        Log.d("NUR", "End location is -> $mEndLocation")
        val cal = Calendar.getInstance()
        val temp = cal.timeInMillis - startTime
        timeRun = (temp / 1000).toInt()

        showResults()
    }

    /**
     * Function that shows Run results
     */
    private fun showResults() {
        val resultDialog = ResultDialog()
        resultDialog.setTargetFragment(this, 1)
        resultDialog.show(activity!!.supportFragmentManager, "Result")
    }

    /**
     * Function that saves runs to database
     */
    private fun saveRun() {

        val newRun = Runs(
            runDate,
            distance,
            timeRun,
            mStartLocation.latitude.toString(),
            mStartLocation.longitude.toString(),
            mEndLocation.latitude.toString(),
            mEndLocation.longitude.toString()
        )
        viewModel.insertRun(newRun)
    }

    /**
     * Function that initializes map, called when view is created
     */
    private fun initGoogleMap(savedInstanceState: Bundle?) {
        Log.d("NURI", "init googlemap")

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mMapView.onCreate(mapViewBundle)
        mMapView.getMapAsync(this)
    }

    /**
     * Function to get current/ last known location
     */
    private fun getCurrentLocation() {
        Log.d("NURI", "getCurrent Location")

        val location: Task<Location> = mFusedLocationProviderClient.lastLocation
        var currentLocation: Location
        location.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentLocation = task.result as Location
                currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
            } else {
                Toast.makeText(activity, "Could not find location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getLocation(): LatLng {
        return currentLatLng
    }

    override fun getList(): MutableList<LatLng> {
        return list
    }

    override fun getTimeStart(): Date {
        return runDate
    }

    override fun getStartingPoint(): LatLng {
        return mStartLocation
    }

    override fun getEndPoint(): LatLng {
        return mEndLocation
    }

    override fun getDistanceTravelled(): Int {
        return distance
    }

    override fun getTimeSpent(): Int {
        return timeRun
    }

    override fun save() {
        saveRun()
    }

    /**
     * Implementations of Result Dialog Functions end here
     */

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val location = intent.getParcelableExtra<Location?>(EXTRA_LOCATION)
                if (location != null) {
                    val prev: LatLng = list.last() // as LatLong
                    val prevv = Location(LocationManager.GPS_PROVIDER) // As Location
                    prevv.latitude = prev.latitude
                    prevv.longitude = prev.longitude
                    distance += prevv.distanceTo(location).toInt()
                    list.add(LatLng(location.latitude, location.longitude))
                    Log.d(
                        TAG,
                        "onReceive() -> newLocation added, start drawing, distance is also calculating"
                    )
                    drawPolylineOnMap(list)
                }
            }
        }
    }

    /**
     * Util functions start here (Permissions, Permission Result etc....)
     */

    //Current state of permissions
    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    /* Requests permission if checkPermissions is false*/
    private fun requestPermission() {
        val shouldPrRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity as Activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val shouldPrRationale2 = ActivityCompat.shouldShowRequestPermissionRationale(
            activity as Activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (shouldPrRationale || shouldPrRationale2) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                view!!.findViewById(R.id.layout_run_id),
                getString(R.string.permission_rationale),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(
                R.string.ok
            ) {
                // Request permission
                ActivityCompat.requestPermissions(
                    activity as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), REQUEST_PERMISSIONS_REQUEST_CODE
                )
            }.show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                activity as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult() is called")

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled, tupoi user ((
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission is granted, nakonetsto
                if (running) {
                    mService!!.requestLocationUpdates()
                }
            } else {
                //Permission denied
                Snackbar.make(
                    view!!.findViewById(R.id.layout_run_id),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.settings) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_SETTINGS
                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }.show()
            }
        }
    }

    private fun drawPolylineOnMap(list: MutableList<LatLng>) {
        val polylineOptions = PolylineOptions().apply {
            color(Color.GREEN)
            width(5F)
            addAll(list)
        }
        Log.d("NURI", "drawPoly")
        gMap.clear()
        gMap.addPolyline(polylineOptions)

        val builder: LatLngBounds.Builder = LatLngBounds.builder()

        for (latLng in list) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        val cU = CameraUpdateFactory.newLatLngBounds(bounds, 10)
        gMap.animateCamera(cU)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d("CHANGED", "Changed")
    }


}
