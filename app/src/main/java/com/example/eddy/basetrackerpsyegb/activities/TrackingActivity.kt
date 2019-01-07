package com.example.eddy.basetrackerpsyegb.activities

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
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.Service.MyLocationService
import com.example.eddy.basetrackerpsyegb.Service.ServiceEvent
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_tracking.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate
import com.example.eddy.basetrackerpsyegb.Service.RECEIVER as BROADCAST

class TrackingActivity : AppCompatActivity() {


    var map: GoogleMap? = null
    var timer = Timer("Clock", false)

    var id: Int = 0
    var pauseFlag: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        trackingTxtTotalDistance.visibility = INVISIBLE

        startService(Intent(this, MyLocationService::class.java))

        trackingBtnStart.setOnClickListener { startTracking() }
        trackingBtnStop.setOnClickListener { stopTracking() }
        trackingBtnPause.setOnClickListener { pausePlayTracking() }

        (trackView as SupportMapFragment).getMapAsync {
            Log.e("Asd", "asjkdkasdkasdk")
            this.map = it
            map?.uiSettings?.isMyLocationButtonEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST.START_TRACKING))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadCastReceiver)

    }

    private fun startTracking() {
        trackingBtnStart.visibility = INVISIBLE
        trackingBtnPause.text = "PAUSE"
        EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.START))
        trackingBtnStop.visibility = VISIBLE
    }


    private fun stopTracking() {
        stopTimer()
        trackingBtnStop.visibility = INVISIBLE
        EventBus.getDefault().post(ServiceEvent(control = ServiceEvent.Control.STOP, totalTime = timeElasped, id = id))
        trackingBtnStart.visibility = VISIBLE
    }


    private fun pausePlayTracking() {
        if (pauseFlag) {
            //Paused
            pauseTimer()
            EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.PAUSE))
            trackingBtnPause.text = "PLAY"
            pauseFlag = false
        } else {
            resumeTimer()
            trackingBtnPause.text = "PAUSE"
            EventBus.getDefault().post(ServiceEvent(id = id, control = ServiceEvent.Control.RESUME))
            pauseFlag = true
        }
    }


    @UiThread
    private fun update(curLat: Double, curLong: Double) {
        val lat = "Latitude: $curLat"
        trackingLat.text = lat
        val long = "Longitude: $curLong"
        trackingLong.text = long


        var latLng = LatLng(curLat, curLong)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }


    var counter: Long = 0L
    @UiThread
    private fun startUpdates(id: Int, startTime: Long, startLat: Double, startLong: Double) {
        this.id = id
        val start = RunUtils.getDate(startTime)
        val title = "Run $id started at $start"
        trackingTxtTitle.text = title
        val lat = "Latitude: $startLat"
        trackingLat.text = lat
        val long = "Longitude: $startLong"
        trackingLong.text = long


        var latLng = LatLng(startLat, startLong)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))


        //changes
        startTimer()

    }

    var timeElasped : String = ""
    private fun startTimer() {
        var it = 1000
        timer = Timer("Clock", false)
        timer.scheduleAtFixedRate(0, TimeUnit.SECONDS.toMillis(1)) {
            counter += it
            timeElasped =  RunUtils.getDuration(counter)
            var txt = "Time Elasped: $timeElasped"
            trackingTxtElaspedTime.text = txt
        }
    }

    private fun stopTimer() {
        counter = 0L
        timer.cancel()

    }

    private fun resumeTimer() {
        startTimer()
    }

    private fun pauseTimer() {
        timer.cancel()
    }

    @UiThread
    private fun endUpdates(endTime: Long, totalDist: Float) {
        val end = RunUtils.getDate(endTime)
        val title = "Run ended at $end"
        trackingTxtTitle.text = title
        trackingTxtTotalDistance.visibility = VISIBLE
        trackingTxtTotalDistance.text = totalDist.toString()
        stopTimer()
    }


    val broadCastReceiver = object : BroadcastReceiver() {


        override fun onReceive(contxt: Context?, intent: Intent?) {
            Log.e("broadcastreciever", "did we make it")
            when (intent?.action) {

                BROADCAST.START_TRACKING -> {
                    id = intent.getIntExtra(RunMetrics.ID, 0)
                    Log.e("START_TRACKING", "__")
                    val startTime = intent.getLongExtra(RunMetrics.START_TIME, 0L)
                    val startLat = intent.getDoubleExtra(GPS.LATITUDE, 0.0)
                    val startLong = intent.getDoubleExtra(GPS.LONGITUDE, 0.0)
                    startUpdates(
                        id = id,
                        startTime = startTime,
                        startLat = startLat,
                        startLong = startLong
                    )

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
                    endUpdates(endTime, totalDist = totalDist)
                }
            }
        }
    }

}
