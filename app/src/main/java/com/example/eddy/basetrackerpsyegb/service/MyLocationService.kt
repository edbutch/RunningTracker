package com.example.eddy.basetrackerpsyegb.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.support.v4.app.NotificationCompat
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import com.example.eddy.basetrackerpsyegb.database.*
import com.example.eddy.basetrackerpsyegb.R
import org.jetbrains.anko.doAsync
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import com.example.eddy.basetrackerpsyegb.service.ServiceEvent.Control.*
import org.greenrobot.eventbus.EventBus
import android.app.PendingIntent
import com.example.eddy.basetrackerpsyegb.activities.TrackingActivity


class MyLocationService : Service() {


    companion object {
        private val TAG = "MyLocationService"
    }


    private var locationManager: LocationManager? = null
    private var locationListener = (LocationListener(LocationManager.GPS_PROVIDER))
    lateinit var lastLocation: Location
    lateinit var lastGPS: GPS
    var currentTrackingPKey: Int = 0
    var isListenerInitialized: Boolean = false


    //https://github.com/codepath/android_guides/issues/220
    //Referred to this discussion when creating my listener service.
    private inner class LocationListener(provider: String) : android.location.LocationListener {


        override fun onLocationChanged(location: Location) {


            var time = 0L
            var distance = 0F
            var speed: Float = 0F
            if (::lastLocation.isInitialized) {
                time = location.time - lastLocation.time
                distance = lastLocation.distanceTo(location)

                if (time != 0L) {
                    speed = distance / (time / 1000)
                    if (speed.isInfinite()) {
                        //As we have updated in under the 1S interval
                        //Speed will be 1000, we cannot use speed from location object#
                        //so we must do speed = d / interval, to give highest accuracy
                        speed = distance / 1000
                    }
                }
            } else {
                lastLocation = Location(LocationManager.GPS_PROVIDER)
            }



            var parentId = currentTrackingPKey
            var latitude = location.latitude
            var longitude = location.longitude
            var ele = location.altitude
            var timestamp = location.time


            val gps =
                GPS(
                    pKey = 0,
                    parentId = parentId,
                    longitude = longitude,
                    latitude = latitude,
                    timestamp = timestamp,
                    elevation = ele,
                    speed = speed
                )

            putDB(gps, time, distance)
            lastGPS = gps
            lastLocation.set(location)

        }


        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
            stopMetrics()
        }


        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }


        fun resumeTracking(id: Int) {
            isListenerInitialized = true
            currentTrackingPKey = id
        }


        private fun putDB(gps: GPS, time: Long, distance: Float) {

            doAsync {
                if (isListenerInitialized) {
                    addGPS(gps, distance)
                    updateDistance(gps.parentId, distance)
                } else {
                    startMetrics(gps)
                    isListenerInitialized = true
                    updateDistance(gps.parentId, distance)
                }


            }
        }

        private fun updateDistance(parentId: Int, distance: Float) {
            contentResolver.updateRunDistance(distance, parentId)
        }


        private fun addGPS(gps: GPS, distance: Float) {
            contentResolver.addGPS(gps)
            broadcastData(COMMAND.UPDATE_TRACKING, gps, distance = distance)

        }

        private fun startMetrics(gps: GPS) {
            val rm = RunMetrics()
            rm.startTime = gps.timestamp
            currentTrackingPKey = contentResolver.startMetrics(rm)
            gps.parentId = currentTrackingPKey
            contentResolver.addGPS(gps)
            broadcastData(COMMAND.START_TRACKING, gps)
        }

        private fun broadcastData(command: Int, gps: GPS? = null, totalDistance: Float = 0F, distance: Float = 0F) {


            val intent = Intent()



            when (command) {
                COMMAND.START_TRACKING -> {
                    if (gps != null) {
                        intent.putExtra(RunMetrics.ID, currentTrackingPKey)
                        intent.putExtra(GPS.LATITUDE, gps.latitude)
                        intent.putExtra(GPS.LONGITUDE, gps.longitude)
                        intent.putExtra(RunMetrics.START_TIME, gps.timestamp)
                    }
                }


                COMMAND.UPDATE_TRACKING -> {
                    if (gps != null) {
                        intent.putExtra(GPS.LATITUDE, gps.latitude)
                        intent.putExtra(GPS.LONGITUDE, gps.longitude)
                        intent.putExtra(GPS.TIME, gps.timestamp)
                        intent.putExtra(GPS.SPEED, gps.speed)
                        intent.putExtra(GPS.ELE, gps.elevation)
                        intent.putExtra(GPS.DISTANCE, distance)
                    }

                }

                COMMAND.STOP_TRACKING -> {
                    if (gps != null) {
                        intent.putExtra(RunMetrics.END_TIME, gps.timestamp)
                        if (totalDistance != 0F) {
                            intent.putExtra(RunMetrics.TOTAL_DISTANCE, totalDistance)
                        }

                    }
                }
            }

            intent.putExtra(COMMAND.COMMAND, command)

            intent.action = RECEIVER.RECEIVER_FILTER

            sendBroadcast(intent)

        }

        fun stopMetrics() {
            doAsync {
                isListenerInitialized = false
                contentResolver.endMetrics(lastLocation.time, currentTrackingPKey)

            }

        }


    }


    override fun onBind(arg0: Intent): IBinder? {
        //Service Control is being done via event bus
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")

        if (intent.action != null) {
            when (intent.action) {
                ACTION.PAUSE_TRACKING -> {
                    if (state == TrackingActivity.STATE.STARTED) {
                        pauseTracking()
                        sendBroadcast(
                            Intent(RECEIVER.RECEIVER_FILTER).putExtra(
                                COMMAND.COMMAND,
                                COMMAND.PAUSE_TRACKING
                            )
                        )
                        state = TrackingActivity.STATE.PAUSED
                    }

                }
                ACTION.RESUME_TRACKING -> {
                    if (state == TrackingActivity.STATE.PAUSED) {
                        state = TrackingActivity.STATE.STARTED
                        resumeTracking(currentTrackingPKey)
                        sendBroadcast(
                            Intent(RECEIVER.RECEIVER_FILTER).putExtra(
                                COMMAND.COMMAND,
                                COMMAND.RESUME_TRACKING
                            )
                        )
                    }

                }

            }
        }

        return Service.START_STICKY
    }


    override fun onCreate() {
        Log.e(TAG, "onCreate")
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        stopTracking()
    }

    private fun initializeLocationManager() {
        if (locationManager == null) {
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    private fun startTracking() {
        try {
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0F,
                locationListener
            )
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

    }

    private fun stopTracking(totalTime: Long = 0L, id: Int = 0, time: Long = 0L) {
        if (locationManager != null) {
            var caught = false

            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }


                locationListener.stopMetrics()
                if (id != 0) {
                    doAsync {

                        contentResolver.updateTotalDuration(totalTime = totalTime, id = id)

                    }
                }


                locationManager!!.removeUpdates(locationListener)
            } catch (ex: Exception) {
                Log.i(TAG, "fail to remove location listener, ignore", ex)
                caught = true
            }

            if (!caught) {
                locationManager = null

            }
        } else if (id != 0 && totalTime != 0L) {
            //to 'stop' a service that has been 'paused
            //The event bus sends this data from the activity to the service to stop.
            doAsync {
                contentResolver.endMetrics(time = time, id = id)
                contentResolver.updateTotalDuration(totalTime = totalTime, id = id)
                isListenerInitialized = false

            }

        }


    }


    private fun pauseTracking() {
        if (locationManager != null) {
            var caught = false

            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                locationManager!!.removeUpdates(locationListener)
            } catch (ex: Exception) {
                Log.i(TAG, "fail to remove location listener, ignore", ex)
                caught = true
            }

            if (!caught) {
                locationManager = null

            }
        }
    }

    private fun resumeTracking(id: Int) {
        initializeLocationManager()
        locationListener.resumeTracking(id)
        startTracking()
    }


    //https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1
    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }


        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setContentTitle(TAG)
            .setContentIntent(getContentIntent())
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(getPauseAction())
            .addAction(getResumeAction())
            .build()
        startForeground(101, notification)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    private fun getPauseAction(): NotificationCompat.Action {

        val pauseIntent = Intent(this, MyLocationService::class.java)
        pauseIntent.action = ACTION.PAUSE_TRACKING
        val pPauseIntent = PendingIntent.getService(
            this, 0,
            pauseIntent, 0
        )
        val pauseAction =
            NotificationCompat.Action.Builder(android.R.drawable.ic_menu_delete, "Pause", pPauseIntent).build()

        return pauseAction
    }

    private fun getResumeAction(): NotificationCompat.Action {

        val resumeIntent = Intent(this, MyLocationService::class.java)
        resumeIntent.action = ACTION.RESUME_TRACKING
        val pResumeIntent = PendingIntent.getService(
            this, 0,
            resumeIntent, 0
        )
        val resumeAction =
            NotificationCompat.Action.Builder(android.R.drawable.ic_menu_delete, "Resume", pResumeIntent).build()

        return resumeAction
    }


    private fun getContentIntent(): PendingIntent {
        // https://stackoverflow.com/questions/5502427/resume-application-and-stack-from-notification
        //This showed me how to get a pending intent which takes you to the activity and maintaints its stack!
        val i = packageManager
            .getLaunchIntentForPackage(packageName)!!
            .setPackage(null)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)



        return PendingIntent.getActivity(
            applicationContext, 0,
            i, 0
        )
    }


    var state: TrackingActivity.STATE = TrackingActivity.STATE.STOPPED
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ServiceEvent) {

        when (event.control) {
            START -> {
                startForeground()
                initializeLocationManager()
                startTracking()
                ///all states are reflected within EVENT.CONTROL, or within the pause / resume intents
                //I.e, the only place the user CAN control the app.
                state = TrackingActivity.STATE.STARTED
            }
            STOP -> {
                stopTracking(event.totalTime, event.id)
                state = TrackingActivity.STATE.STOPPED
            }
            PAUSE -> {
                pauseTracking()
                state = TrackingActivity.STATE.PAUSED
            }
            RESUME -> {
                state = TrackingActivity.STATE.STARTED
                if (event.id != 0) {
                    resumeTracking(event.id)
                }
            }
            GET_STATE -> {
                when(this.state){
                    //Broadcasts back the state , as the Activity would have missed it if it was destroyed.
                    TrackingActivity.STATE.STARTED -> {
                        sendBroadcast(
                            Intent(RECEIVER.RECEIVER_FILTER).putExtra(
                                COMMAND.COMMAND,
                                COMMAND.RESUME_TRACKING
                            ))
                    }
                    TrackingActivity.STATE.PAUSED ->
                    {
                        sendBroadcast(
                            Intent(RECEIVER.RECEIVER_FILTER).putExtra(
                                COMMAND.COMMAND,
                                COMMAND.PAUSE_TRACKING
                            )
                        )
                    }
                    else -> {
                        //Nothing needed
                    }
                }
            }


        }

    }


}