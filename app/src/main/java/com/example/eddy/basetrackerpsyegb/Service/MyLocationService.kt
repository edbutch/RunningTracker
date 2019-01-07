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
import com.example.eddy.basetrackerpsyegb.activities.MainActivity
import com.example.eddy.basetrackerpsyegb.R
import org.jetbrains.anko.doAsync
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import com.example.eddy.basetrackerpsyegb.Service.ServiceEvent.Control.*
import org.greenrobot.eventbus.EventBus
import com.example.eddy.basetrackerpsyegb.Service.RECEIVER as BROADCAST


class MyLocationService : Service() {


    companion object {
        val KEY_LOCATION_INTERVAL = "LOCATION_INTERVAL"
        val KEY_LOCATION_DISTANCE = "LOCATION_DISTANCE"
        private val TAG = "MyLocationService"
    }

    private var locationManager: LocationManager? = null

    //    var timer: Boolean = false
    private var locationListener = (LocationListener(LocationManager.GPS_PROVIDER))


    private inner class LocationListener(provider: String) : android.location.LocationListener {
        lateinit var mLastLocation: Location
        var currentID: Int = 0
        var initialized: Boolean = false


        fun resumeTracking(id: Int) {
            initialized = true
            currentID = id
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


            var parentId = currentID
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
            broadcastData(BROADCAST.TRACKING, gps)

        }

        private fun startMetrics(gps: GPS) {
            val rm = RunMetrics()
            rm.startTime = gps.timestamp
            currentID = contentResolver.startMetrics(rm)
            gps.parentId = currentID
            contentResolver.addGPS(gps)
            broadcastData(BROADCAST.START_TRACKING, gps)
        }

        private fun broadcastData(action: String, gps: GPS? = null, totalDistance: String? = null) {

            val intent = Intent(action)



            when (action) {
                BROADCAST.START_TRACKING -> {
                    if (gps != null) {
                        intent.putExtra(RunMetrics.ID, currentID)
                        intent.putExtra(GPS.LATITUDE, gps.latitude)
                        intent.putExtra(GPS.LONGITUDE, gps.longitude)
                        intent.putExtra(RunMetrics.START_TIME, gps.timestamp)
                    }
                }

                BROADCAST.TRACKING -> {
                    if (gps != null) {
                        intent.putExtra(GPS.LATITUDE, gps.latitude)
                        intent.putExtra(GPS.LONGITUDE, gps.longitude)
                    }

                }

                BROADCAST.STOP_TRACKING -> {
                    if (gps != null) {
                        intent.putExtra(RunMetrics.END_TIME, gps.timestamp)
                        totalDistance ?: intent.putExtra(RunMetrics.TOTAL_DISTANCE, totalDistance)

                    }
                }
            }

            sendBroadcast(intent)

        }

        fun stopMetrics() {
            var rm = RunMetrics()
            initialized = false
            rm = contentResolver.endMetrics(mLastLocation.time, currentID)
            Log.e("stopmetrics", rm.toString())

        }


    }


    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")


        if (intent.action != null && intent.action == ACTION.STOP_SERVICE) {
            stopSelf()
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

    private fun stopTracking(totalTime: String = "", id: Int = 0) {
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

    private fun getContentIntent(): PendingIntent {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.action =
                return PendingIntent.getActivity(
                    applicationContext, 0,
                    notificationIntent, 0
                )
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ServiceEvent) {
        Log.e("hi", "hi")

        when (event.control) {
            START -> {
                startForeground()
                initializeLocationManager()
                startTracking()
            }
            STOP -> {
                stopTracking(event.totalTime, event.id)
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