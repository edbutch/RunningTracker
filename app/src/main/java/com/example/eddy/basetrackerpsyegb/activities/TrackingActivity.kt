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
import com.example.eddy.basetrackerpsyegb.Service.RECEIVER.RECEIVER_FILTER
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
import com.example.eddy.basetrackerpsyegb.Service.COMMAND as COMMAND

class TrackingActivity : AppCompatActivity() {

    var timeStarted: Long = 0L
    private var currentTime: Long = 0L
    var id: Int = 0

    var map: GoogleMap? = null
    var timer = Timer("Clock", false)

    var state: STATE = STATE.STOPPED

    enum class STATE  {PAUSED, STOPPED, STARTED}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)


        startService(Intent(this, MyLocationService::class.java))

        trackingBtnStart.setOnClickListener { startTracking() }
        trackingBtnStop.setOnClickListener { stopTracking() }
        trackingBtnPause.setOnClickListener { pausePlayTracking() }

        (trackView as SupportMapFragment).getMapAsync {
            Log.e("Asd", "asjkdkasdkasdk")
            this.map = it
            map?.uiSettings?.isMyLocationButtonEnabled = true
        }

        Log.v("cunt bitch", "state $state time $counter")

        if(state == STATE.STARTED){
            startTimer()
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadCastReceiver, IntentFilter(RECEIVER_FILTER))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadCastReceiver)

    }

    var counter: Long = 0L
    var timeElasped: String = ""
    private fun startTimer() {
        state = STATE.STARTED
        trackingBtnPause.text = "PAUSE"
        var it = 1000
        timer = Timer("Clock", false)
        timer.scheduleAtFixedRate(0, TimeUnit.SECONDS.toMillis(1)) {
            counter += it
            timeElasped = RunUtils.getDuration(counter)
            var txt = "Time Elasped: $timeElasped"
            trackingTxtElaspedTime.text = txt
        }
    }

    private fun stopTimer() {
        if(state != STATE.STOPPED){
            state = STATE.STOPPED
            counter = 0L
            timer.cancel()
            trackingTxtElaspedTime.text = "00:00"

        }

    }


    private fun pauseTimer() {
        if(state != STATE.PAUSED){
            state = STATE.PAUSED
            trackingBtnPause.text = "RESUME"
            timer.cancel()
        }

    }


    private fun startTracking() {
            if(state == STATE.STOPPED){
                state = STATE.STARTED
                Log.e("ey", "kasdkaskdasd")
                trackingBtnPause.text = "PAUSE"
                EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.START, time = currentTime))
            }

    }


    private fun stopTracking() {
        if( state != STATE.STOPPED){
            stopTimer()
            Log.v("TRACKINGACTIVITY", "Stop Tracking : Time Elasped = $timeElasped currentime = $currentTime")
            EventBus.getDefault().post(
                ServiceEvent(
                    control = ServiceEvent.Control.STOP,
                    totalTime = timeElasped,
                    id = id,
                    time = currentTime
                )
            )
        }

    }


    private fun pausePlayTracking() {

        if (state != STATE.STOPPED) {
            if (state == STATE.STARTED) {
                //We have clicked PAUSE whilst running, therefore we need to pause
                pauseTimer()
                EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.PAUSE, time = currentTime))
                state = STATE.PAUSED
                trackingBtnPause.text = "PLAY"
            } else if(state == STATE.PAUSED) {
                startTimer()
                trackingBtnPause.text = "PAUSE"
                EventBus.getDefault()
                    .post(ServiceEvent(control = ServiceEvent.Control.RESUME, id = id, time = currentTime))
                state = STATE.STARTED
            }
        }

    }


    val broadCastReceiver = object : BroadcastReceiver() {


        override fun onReceive(contxt: Context?, intent: Intent?) {
            Log.e("broadcastreciever", "did we make it")


            when (intent?.getIntExtra(COMMAND.COMMAND, 9)) {

                COMMAND.START_TRACKING -> {
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
                COMMAND.UPDATE_TRACKING -> {

                    val curLat = intent.getDoubleExtra(GPS.LATITUDE, 0.0)
                    val curLong = intent.getDoubleExtra(GPS.LONGITUDE, 0.0)
                    currentTime = intent.getLongExtra(GPS.TIME, 0)

                    Log.e("TRACKING", "__ CURRENTTIME $currentTime")

                    update(curLat, curLong)
                }

                COMMAND.STOP_TRACKING -> {
                    Log.e("STOP_TRACKING", "__")

                    val endTime = intent.getLongExtra(RunMetrics.END_TIME, 0L)
                    val totalDist = intent.getFloatExtra(RunMetrics.TOTAL_DISTANCE, 0F)
                    endUpdates(endTime, totalDist = totalDist)
                }

                COMMAND.PAUSE_TRACKING -> {
                    pauseTimer()
                }
                COMMAND.RESUME_TRACKING -> {
                    startTimer()
                }
            }
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


    @UiThread
    private fun startUpdates(id: Int, startTime: Long, startLat: Double, startLong: Double) {
        this.id = id
        val start = RunUtils.getDate(startTime)
        this.timeStarted = startTime
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


    @UiThread
    private fun endUpdates(endTime: Long, totalDist: Float) {
        val end = RunUtils.getDate(endTime)
        val title = "Run ended at $end"
        trackingTxtTitle.text = title
        trackingTxtTotalDistance.text = totalDist.toString()
        stopTimer()

    }


}
