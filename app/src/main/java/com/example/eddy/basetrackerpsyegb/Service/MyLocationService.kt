package com.example.eddy.basetrackerpsyegb.Service

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
import com.example.eddy.basetrackerpsyegb.DB.*
import com.example.eddy.basetrackerpsyegb.R
import org.jetbrains.anko.doAsync
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import com.example.eddy.basetrackerpsyegb.Service.ServiceEvent.Control.*
import org.greenrobot.eventbus.EventBus
import android.app.PendingIntent
import com.example.eddy.basetrackerpsyegb.activities.TrackingActivity


class MyLocationService : Service() {


    companion object {
        val KEY_LOCATION_INTERVAL = "LOCATION_INTERVAL"
        val KEY_LOCATION_DISTANCE = "LOCATION_DISTANCE"
        private val TAG = "MyLocationService"
    }

    private var locationManager: LocationManager? = null

    //    var timer: Boolean = false
    private var locationListener = (LocationListener(LocationManager.GPS_PROVIDER))

    lateinit var lastLocation: Location
    var currentTrackingPKey: Int = 0
    var isListenerInitialized: Boolean = false

    private inner class LocationListener(provider: String) : android.location.LocationListener {


        fun resumeTracking(id: Int) {
            isListenerInitialized = true
            currentTrackingPKey = id
        }


        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location.")


            var time = 0L
            var distance = 0F
            var speed: Float = 0F
            if (::lastLocation.isInitialized) {
                time = location.time - lastLocation.time
                distance = lastLocation.distanceTo(location)

                if (time != 0L) {
                    speed = distance / (time / 1000)
//                    speed = location.speed

                } else {
//                    speed = location.speed
                }


            } else {
                lastLocation = Location(LocationManager.GPS_PROVIDER)
            }


            Log.e(TAG, "time $time distance $distance speed $speed speed = distance / time ${distance / time}")

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
            broadcastData(COMMAND.UPDATE_TRACKING, gps, distance)

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
                        Log.e("TRACK", "ASKLDAKSD")
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
                var rm = RunMetrics()
                isListenerInitialized = false
                rm = contentResolver.endMetrics(lastLocation.time, currentTrackingPKey)
                Log.e("stopmetrics", rm.toString())

            }

        }


    }


    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")

        if (intent.action != null) {
            when (intent.action) {
                ACTION.STOP_TRACKING -> {
                    sendBroadcast(
                        Intent(RECEIVER.RECEIVER_FILTER).putExtra(
                            COMMAND.COMMAND,
                            COMMAND.STOP_TRACKING
                        ).putExtra("fromPendingIntent", true)
                    )
                    state = TrackingActivity.STATE.STOPPED

                }
                ACTION.PAUSE_TRACKING -> {
                    if (state == TrackingActivity.STATE.STARTED) {
                        /*These states have been included inside the service as well as the actitiy to REFLECT the state of the activity.
                        The course spec specified the user should select when they want the run to start, which is why it's here, because the run
                        otherwise could be stopped and then the user could press 'resume', as it hasn't flushed the current ID of the run in the service.
                        TODO I would like to refactor the state being passed into the event bus so we don't need to set it at all within the service.
                         */
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
                ACTION.STOP_SERVICE -> {
//                    stopTracking(fromPendingIntent = true)
//                    stopSelf()
                    //Doesn't seem necessary to stop the service.
                }
            }
        }

        return Service.START_STICKY
    }

    private fun resumePauseBroadcast() {

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
            /*TODO TEST PARAMS*/
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100L,
                0F,
                locationListener
            )
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

    }

    private fun stopTracking(totalTime: Long = 0L, id: Int = 0, time: Long = 0L, fromPendingIntent: Boolean = false) {
        Log.e("STOPTRACKING", "Totaltime $totalTime , id $id, time $time")
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
                true
            }

            if (!caught) {
                locationManager = null

            }
        } else if (id != 0 && time != 0L && totalTime != 0L && !fromPendingIntent) {
            //Basically if we call stop metrics from our activity we want to be abel to update the total duration using
            //The duration in the activity. This is to illustrate communication from Event Busses.
            //This is also a way to 'stop' a service that has been 'paused
            //THis was an issue as I wanted to close down the listener when paused as it's heavy on resources, however I wanted to
            //If i had more time, i'd refactor the timer inside Tracking acvitvity, and use my start time as a metric with the current time to calculate the time elasped
            //And take pause times for refrence as well.
            doAsync {
                contentResolver.endMetrics(time = time, id = id)
                contentResolver.updateTotalDuration(totalTime = totalTime, id = id)
                isListenerInitialized = false

            }

        }

        if (fromPendingIntent) {
            //If stop has been selected from the pending intent (notificaton), it will close down everything
            doAsync {
                contentResolver.endMetrics(time = lastLocation.time, id = id)
                contentResolver.updateTotalDuration(totalTime = totalTime, id = id)
                isListenerInitialized = false
                Log.e("STOP TRACKING", "FROM PENDING INTENT")

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
                true
            }

            if (!caught) {
                locationManager = null

            }
        }
    }

    private fun resumeTracking(id: Int) {
        initializeLocationManager()
        locationListener?.resumeTracking(id)
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
            .addAction(getStopAction())
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


    private fun getStopAction(): NotificationCompat.Action {
        val stopIntent = Intent(this, MyLocationService::class.java)
        stopIntent.action = ACTION.STOP_TRACKING
        val pStopIntent = PendingIntent.getService(
            this, 0,
            stopIntent, 0
        )
        val stopAction =
            NotificationCompat.Action.Builder(android.R.drawable.ic_menu_delete, "Stop", pStopIntent).build()

        return stopAction
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
                Log.e("hello world", "eeeee")
                startForeground()
                initializeLocationManager()
                startTracking()
                //As you can see here, the state is initialized to stop like Tracking activity, then started
                ///And all states are reflected within EVENT.CONTROL, or within the pause / resume intents
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

            STOP_FROM_INTENT -> {

                Log.e("STOP FROM INTENT", "${event.id} + ${event.totalTime}")

                stopTracking(totalTime = event.totalTime, id = event.id, fromPendingIntent = true)
            }


        }

    }


}