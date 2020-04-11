package com.kyrgyzcoder.myfitness.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.kyrgyzcoder.myfitness.DATE_FORMAT
import com.kyrgyzcoder.myfitness.DEFAULT_ZOOM
import com.kyrgyzcoder.myfitness.MAP_VIEW_BUNDLE_KEY
import com.kyrgyzcoder.myfitness.R
import kotlinx.android.synthetic.main.result_dialog.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ResultDialog : DialogFragment(), OnMapReadyCallback {

    private lateinit var listener: ResultDialogListener
    private lateinit var gMap: GoogleMap
    private lateinit var mMapView: MapView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)


        val inflater: LayoutInflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.result_dialog, null)

        mMapView = view.findViewById(R.id.marViewResult)

        initMap(savedInstanceState)
        val dateFormat: DateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)

        view.textViewResultTime.text = dateFormat.format(listener.getTimeStart())
        var lat = listener.getStartingPoint()
        var latStr = "[${lat.latitude} , ${lat.longitude}]"
        view.textViewResultStartingPoint.text = latStr
        lat = listener.getEndPoint()
        latStr = "[${lat.latitude} , ${lat.longitude} ]"
        view.textViewResultEndPoint.text = latStr

        var str = "${listener.getDistanceTravelled()} meters"
        view.textViewResultDistance.text = str
        str = "${listener.getTimeSpent() / 60} minustes, ${listener.getTimeSpent() % 60} seconds"
        view.textViewResultTimeSpent.text = str

        val speed = listener.getDistanceTravelled() / listener.getTimeSpent()
        val speedStr = "$speed m/s"
        view.textViewResultAvrgSpeed.text = speedStr

        builder.setView(view)
            .setTitle(getString(R.string.result_title))
            .setNegativeButton("Don't save") { _, _ ->
                Toast.makeText(activity, "Did not save to history!", Toast.LENGTH_SHORT).show()
            }
        builder.setPositiveButton("Save") { _, _ ->
            listener.save()
            Toast.makeText(activity, "Saved to the run history", Toast.LENGTH_SHORT).show()
        }

        return builder.create()
    }

    private fun initMap(savedInstanceState: Bundle?) {
        Log.d("NURI", "init googlemap Result")

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mMapView.onCreate(mapViewBundle)
        mMapView.getMapAsync(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as ResultDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "Error! Must Implement Activity")
        }
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    interface ResultDialogListener {
        fun getTimeStart(): Date
        fun getStartingPoint(): LatLng
        fun getEndPoint(): LatLng
        fun getDistanceTravelled(): Int
        fun getTimeSpent(): Int
        fun getLocation(): LatLng
        fun getList(): MutableList<LatLng>
        fun save()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            Log.d("NURI", "onMapReadyResult")
            gMap = googleMap
            gMap.isMyLocationEnabled = true
            gMap.uiSettings.isMyLocationButtonEnabled = true
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(listener.getLocation(), DEFAULT_ZOOM))
            drawPolylineOnMap(listener.getList())
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
}