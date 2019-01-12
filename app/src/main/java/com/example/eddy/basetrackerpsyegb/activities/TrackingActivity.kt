package com.example.eddy.basetrackerpsyegb.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.UiThread
import android.util.Log
import com.example.eddy.basetrackerpsyegb.database.GPS
import com.example.eddy.basetrackerpsyegb.database.RunMetrics
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.service.MyLocationService
import com.example.eddy.basetrackerpsyegb.service.RECEIVER.RECEIVER_FILTER
import com.example.eddy.basetrackerpsyegb.service.ServiceEvent
import com.example.eddy.basetrackerpsyegb.utils.MapUtils
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_tracking.*
import org.greenrobot.eventbus.EventBus
import com.example.eddy.basetrackerpsyegb.service.COMMAND as COMMAND

class TrackingActivity : AppCompatActivity() {

    val TAG = "TrackingAcvtivity"
    private var currentTime: Long = 0L
    private var startTime: Long = 0L
    private var previousTime: Long = 0L

    var timeCounter: Long = 0L

//    timeElasped = RunUtils.getDuration(counter)


    var totalDistance : Float = 0F

    var id: Int = 0

    var map: GoogleMap? = null

    var state: STATE = STATE.STOPPED

    enum class STATE { PAUSED, STOPPED, STARTED }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)


        startService(Intent(this, MyLocationService::class.java))

        trackingBtnStart.setOnClickListener { startTracking() }
        trackingBtnStop.setOnClickListener { stopTracking() }
        trackingBtnPause.setOnClickListener { pausePlayTracking() }

        (trackView as SupportMapFragment).getMapAsync {
            this.map = it
            map?.uiSettings?.isMyLocationButtonEnabled = true
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


    private fun startTracking() {
        if (state == STATE.STOPPED) {
            state = STATE.STARTED
            Log.e("ey", "kasdkaskdasd")
            trackingBtnPause.text = "PAUSE"
            EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.START))
        }

    }


    private fun stopTracking() {
        if (state != STATE.STOPPED) {
            Log.e("STOPPED", "CUrrent time $currentTime start time $startTime previous time $previousTime")
            state = STATE.STOPPED
            val totalTime =  RunUtils.getDuration(timeCounter)
            Log.v("TRACKINGACTIVITY", "Stop Tracking : Time Elasped = $totalTime currentime = $currentTime")
            EventBus.getDefault().post(
                ServiceEvent(
                    control = ServiceEvent.Control.STOP,
                    totalTime = timeCounter,
                    id = id
                )
            )
            resetPreviousTime()

        }

    }

    private fun resetPreviousTime() {
        previousTime = 0L
    }


    private fun pausePlayTracking() {

        if (state != STATE.STOPPED) {
            if (state == STATE.STARTED) {
                Log.e(TAG, "STATE = START - > pause ")
                //We have clicked PAUSE whilst running, therefore we need to pause
                EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.PAUSE))
                state = STATE.PAUSED
                trackingBtnPause.text = "PLAY"
                resetPreviousTime()
            } else if (state == STATE.PAUSED) {
                Log.e(TAG, "STATE = PAISE - > START ")

                trackingBtnPause.text = "PAUSE"
                EventBus.getDefault()
                    .post(ServiceEvent(control = ServiceEvent.Control.RESUME, id = id))
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

                    val receivedTime = intent.getLongExtra(GPS.TIME, 0)

                    val totalDist = intent.getFloatExtra(GPS.DISTANCE,0F)
                    totalDistance += totalDist
                    val ele = intent.getDoubleExtra(GPS.ELE,0.0 )
                    val speed = intent.getFloatExtra(GPS.SPEED,0F)


//                    if(previousTime == 0L){
//                        previousTime = receivedTime
//                    }else{
//                        previousTime = currentTime
//                    }
//                    previousTime = currentTime
                    currentTime = receivedTime

                    Log.e("TRACKING", "__ CURRENTTIME $currentTime")

                    update(curLat, curLong, speed, ele)
                    updateTime()

                }

                COMMAND.STOP_TRACKING -> {
                    Log.e("STOP_TRACKING", "__")

                    val endTime = intent.getLongExtra(RunMetrics.END_TIME, 0L)
                    val totalDist = intent.getFloatExtra(RunMetrics.TOTAL_DISTANCE, 0F)
                    endUpdates(endTime, totalDist = totalDist)
                    state = STATE.STOPPED

                    if(intent.hasExtra("fromPendingIntent")){
                        EventBus.getDefault().post(ServiceEvent(control = ServiceEvent.Control.STOP_FROM_INTENT, id = id, totalTime = timeCounter))

                    }
                }

                COMMAND.PAUSE_TRACKING -> {
                    Log.e("PAUSED", "KASDKAKSDKASDK")
                    resetPreviousTime()
                    state = STATE.PAUSED

                }
                COMMAND.RESUME_TRACKING -> {
                    state = STATE.STARTED
                }
            }

        }
    }

    @UiThread
    private fun updateTime() {
        if (previousTime!= 0L && state == STATE.STARTED) {
            timeCounter += (currentTime - previousTime)
            val time = RunUtils.getDuration(timeCounter)
            val text = "Duration : $time"
            trackingTxtElaspedTime?.text = text

        }
        previousTime = currentTime
    }


    @UiThread
    private fun update(curLat: Double, curLong: Double, speed: Float, ele: Double) {
        map!!.clear()
        val speedFormatted = "Speed:" + "%.2f".format(speed) + "M/S"

        trackingSpeed.text = speedFormatted
        val ele = "Altitude:" +  "%.2f".format(ele) + "M"
        trackingEle.text = ele

        val totalD = totalDistance/1000
        val distFormatted = "%.2f".format(totalD) + "KM"

        val dist =  "Distance : $distFormatted"
        trackingTxtTotalDistance.text =dist


        var latLng = LatLng(curLat, curLong)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))



        map!!.addMarker(MarkerOptions()
            .position(latLng)
            .icon(MapUtils.bitmapDescriptorFromVector(this, R.drawable.ic_run)))
            .title = "You!"



        //    timeElasped = RunUtils.getDuration(counter)

    }


    @UiThread
    private fun startUpdates(id: Int, startTime: Long, startLat: Double, startLong: Double) {
        timeCounter = 0L
        this.id = id
        val start = RunUtils.getDate(startTime)
        this.startTime = startTime
        this.currentTime = startTime
        val title = "Run $id started at $start"
        trackingTxtTitle.text = title


        var latLng = LatLng(startLat, startLong)
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        map!!.addMarker(MarkerOptions()
            .position(latLng)
            .icon(MapUtils.bitmapDescriptorFromVector(this, R.drawable.ic_run)))
            .title = "You!"


        //changes

    }




    @UiThread
    private fun endUpdates(endTime: Long, totalDist: Float) {
        val end = RunUtils.getDate(endTime)
        val title = "Run ended at $end OR $endTime"
        trackingTxtTitle.text = title
        trackingTxtTotalDistance.text = totalDist.toString()
        resetPreviousTime()

    }


}
