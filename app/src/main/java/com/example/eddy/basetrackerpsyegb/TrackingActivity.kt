package com.example.eddy.basetrackerpsyegb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.UiThread
import android.util.Log
import android.view.View.*
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.Service.MyLocationService
import com.example.eddy.basetrackerpsyegb.Service.ServiceEvent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_tracking.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate
import com.example.eddy.basetrackerpsyegb.Service.RECEIVER as BROADCAST

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onMapReady(p0: GoogleMap?) {
        Log.e("Asd", "asjkdkasdkasdk")
        this.map = p0
        map?.uiSettings?.isMyLocationButtonEnabled = true
    }


    var map: GoogleMap? = null
    val timer = Timer("Clock",false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        trackingTxtTotalDistance.visibility = INVISIBLE

        startService(Intent(this, MyLocationService::class.java))

        trackingBtnStart.setOnClickListener { startTracking() }
        trackingBtnStop.setOnClickListener { stopTracking() }

        (trackView as SupportMapFragment).getMapAsync(this)
    }

    private fun startTracking() {
        trackingBtnStart.visibility = INVISIBLE
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST.START_TRACKING))
        EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.START))
        trackingBtnStop.visibility = VISIBLE
    }


    private fun stopTracking() {
        trackingBtnStop.visibility = INVISIBLE
        EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.STOP))
        trackingBtnStart.visibility = VISIBLE

    }

//    @UiThread
//    private fun update(
//        id: String? = null, startTime: String? = null, startLat: String? = null, startLong:
//        String? = null, curLat: String? = null, curLong: String? = null,
//        endTime: String? = null, totalDistance: String? = null
//    ) {
//        val tag = "TrackingUpdate()"
//
//        id ?: Log.v(tag, "id $id")
//        startTime ?: Log.v(tag, "startTime $startTime")
//        startLat ?: Log.v(tag, "startLat $startLat")
//        startLong ?: Log.v(tag, "startLong $startLong")
//        curLat ?: Log.v(tag, "curLat $curLat")
//        curLong ?: Log.v(tag, "curLong $curLong")
//        endTime ?: Log.v(tag, "endTime $endTime")
//        totalDistance ?: Log.v(tag, "totalDistance $totalDistance")
//    }

    @UiThread
    private fun update(curLat: Double, curLong: Double) {
        val lat = "Latitude: $curLat"
        trackingLat.text = lat
        val long = "Longitude: $curLong"
        trackingLong.text = long



        var latLng = LatLng(curLat,curLong)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }


    @UiThread
    private fun startUpdates(id: Int, startTime: Long, startLat: Double, startLong: Double) {
        val start = RunUtils.getDate(startTime)
        val title = "Run $id started at $start"
        trackingTxtTitle.text = title
        val lat = "Latitude: $startLat"
        trackingLat.text = lat
        val long = "Longitude: $startLong"
        trackingLong.text = long


        var latLng = LatLng(startLat,startLong)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))


        var it = 1000
        var counter : Long = 0L
        timer.scheduleAtFixedRate(0,TimeUnit.SECONDS.toMillis(1)){
            counter += it
            var mins = (TimeUnit.MILLISECONDS.toMinutes(counter)) %60
            var seconds = (TimeUnit.MILLISECONDS.toSeconds(counter)) % 60
            var timeElasped = "Time Elasped : $mins:$seconds"
            trackingTxtElaspedTime.text = timeElasped
        }

    }

    @UiThread
    private fun endUpdates(endTime: Long, totalDist: Float) {
        val end = RunUtils.getDate(endTime)
        val title = "Run ended at $end"
        trackingTxtTitle.text = title
        trackingTxtTotalDistance.visibility = VISIBLE
        trackingTxtTotalDistance.text = totalDist.toString()
        unregisterReceiver(broadCastReceiver)
        timer.cancel()
    }


    val broadCastReceiver = object : BroadcastReceiver() {


        override fun onReceive(contxt: Context?, intent: Intent?) {
            Log.e("broadcastreciever", "did we make it")
            when (intent?.action) {

                BROADCAST.START_TRACKING -> {
                    Log.e("START_TRACKING", "__")
                    val id = intent.getIntExtra(RunMetrics.ID, 0)
                    val startTime = intent.getLongExtra(RunMetrics.START_TIME, 0L)
                    val startLat = intent.getDoubleExtra(GPS.LATITUDE, 0.0)
                    val startLong = intent.getDoubleExtra(GPS.LONGITUDE, 0.0)
                    startUpdates(
                        id = id,
                        startTime = startTime,
                        startLat = startLat,
                        startLong = startLong)

                }
                BROADCAST.TRACKING -> {
                    Log.e("TRACKING", "__")

                    val curLat = intent.getDoubleExtra(GPS.LATITUDE, 0.0)
                    val curLong = intent.getDoubleExtra(GPS.LONGITUDE, 0.0)
                    update(curLat, curLong)
                }

                BROADCAST.STOP_TRACKING -> {
                    Log.e("STOP_TRACKING", "__")

                    val endTime = intent.getLongExtra(RunMetrics.END_TIME, 0L)
                    val totalDist = intent.getFloatExtra(RunMetrics.TOTAL_DISTANCE, 0F)
                    endUpdates(endTime,totalDist = totalDist)
                }
            }
        }
    }

}
