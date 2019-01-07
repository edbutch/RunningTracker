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
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import org.greenrobot.eventbus.EventBus
import android.app.PendingIntent




class MyLocationService : Service() {


    companion object {
        val KEY_LOCATION_INTERVAL = "LOCATION_INTERVAL"
        val KEY_LOCATION_DISTANCE = "LOCATION_DISTANCE"
        private val TAG = "MyLocationService"
    }

    private var locationManager: LocationManager? = null

    //    var timer: Boolean = false
    private var locationListener = (LocationListener(LocationManager.GPS_PROVIDER))

    lateinit var mLastLocation: Location
    var currentTrackingPKey: Int = 0
    private inner class LocationListener(provider: String) : android.location.LocationListener {
        var initialized: Boolean = false


        fun resumeTracking(id: Int) {
            initialized = true
            currentTrackingPKey = id
        }


        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location.")


            var time = 0L
            var distance = 0F
            if (::mLastLocation.isInitialized) {
                time = location.time - mLastLocation.time
                distance = mLastLocation.distanceTo(location)

            } else {
                mLastLocation = Location("")
            }

            mLastLocation.set(location)


            var parentId = currentTrackingPKey
            var latitude = location.latitude
            var longitude = location.longitude
            var ele = location.altitude
            var timestamp = location.time
            var speed = location.speed

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
                if (initialized) {
                    addGPS(gps)
                    updateDistance(gps.parentId, distance)
                } else {
                    startMetrics(gps)
                    initialized = true
                    updateDistance(gps.parentId, distance)
                }


            }
        }

        private fun updateDistance(parentId: Int, distance: Float) {
            contentResolver.updateRunDistance(distance, parentId)
        }


        private fun addGPS(gps: GPS) {
            contentResolver.addGPS(gps)
            broadcastData(COMMAND.UPDATE_TRACKING, gps)

        }

        private fun startMetrics(gps: GPS) {
            val rm = RunMetrics()
            rm.startTime = gps.timestamp
            currentTrackingPKey = contentResolver.startMetrics(rm)
            gps.parentId = currentTrackingPKey
            contentResolver.addGPS(gps)
            broadcastData(COMMAND.START_TRACKING, gps)
        }

        private fun broadcastData(command: Int, gps: GPS? = null, totalDistance: String? = null) {

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
                    }

                }

                COMMAND.STOP_TRACKING -> {
                    if (gps != null) {
                        intent.putExtra(RunMetrics.END_TIME, gps.timestamp)
                        totalDistance ?: intent.putExtra(RunMetrics.TOTAL_DISTANCE, totalDistance)

                    }
                }
            }

            intent.putExtra(COMMAND.COMMAND, command)

            intent.action = RECEIVER.RECEIVER_FILTER

            sendBroadcast(intent)

        }

        fun stopMetrics() {
            var rm = RunMetrics()
            initialized = false
            rm = contentResolver.endMetrics(mLastLocation.time, currentTrackingPKey)
            Log.e("stopmetrics", rm.toString())

        }


    }


    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")

        if(intent.action != null){
            when(intent.action){
                ACTION.STOP_SERVICE -> {
                    stopTracking(fromPendingIntent = true)
                    stopSelf()
                }
                ACTION.PAUSE_TRACKING -> {
                    pauseTracking()
                }
                ACTION.RESUME_TRACKING -> {
                    resumeTracking(currentTrackingPKey)
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

    private fun stopTracking(totalTime: String = "", id: Int = 0, time: Long = 0L, fromPendingIntent: Boolean = false) {
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

                doAsync {
                    locationListener.stopMetrics()
                    if (id != 0) {
                        contentResolver.updateTotalDuration(duration = totalTime, id = id)
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
        }else if (id!= 0 && time != 0L){
            //Basically if we call stop metrics from our activity we want to be abel to update the total duration using
            //The duration in the activity. This is to illustrate communication from Event Busses.
            doAsync {
                contentResolver.endMetrics(time = time, id = id)
                contentResolver.updateTotalDuration(duration = totalTime, id = id)

            }

        }

        if(fromPendingIntent){
            //If stop has been selected from the pending intent (notificaton), it will close down everything
            Log.e("FROMPENDING!!!!", "wE'RE IN")
            doAsync {
                val metric = contentResolver.getRun(currentTrackingPKey)
                Log.e("WERE  IN", metric.toString() + "current key $currentTrackingPKey")
                val time = RunUtils.Companion.getDuration(mLastLocation?.time, metric.startTime)
                contentResolver.endMetrics(mLastLocation.time, currentTrackingPKey)
                contentResolver.updateTotalDuration(time, currentTrackingPKey)
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
        stopIntent.action = ACTION.STOP_SERVICE
        val pStopIntent = PendingIntent.getService(
            this, 0,
            stopIntent, 0
        )
        val stopAction =
            NotificationCompat.Action.Builder(android.R.drawable.ic_menu_delete, "Stop", pStopIntent).build()

        return stopAction
    }

    private fun getPauseAction(): NotificationCompat.Action{

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

    private fun getResumeAction(): NotificationCompat.Action{

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
//        vhttps://stackoverflow.com/questions/5502427/resume-application-and-stack-from-notification
        val i = packageManager
            .getLaunchIntentForPackage(packageName)!!
            .setPackage(null)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        val pendingIntent = PendingIntent.getActivity(this, 0, i, 0)


        return PendingIntent.getActivity(
                    applicationContext, 0,
            i, 0
                )
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ServiceEvent) {

        when (event.control) {
            START -> {
                startForeground()
                initializeLocationManager()
                startTracking()
            }
            STOP -> {
                stopTracking(event.totalTime, event.id, event.time)
            }
            PAUSE -> {
                pauseTracking()
            }
            RESUME -> {
                if (event.id != 0) {
                    resumeTracking(event.id)
                }
            }


        }

    }


}