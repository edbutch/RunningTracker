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
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import com.example.eddy.basetrackerpsyegb.DB.*
import com.example.eddy.basetrackerpsyegb.MainActivity
import com.example.eddy.basetrackerpsyegb.R
import org.jetbrains.anko.doAsync
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import com.example.eddy.basetrackerpsyegb.Service.ServiceEvent.Control.*
import org.greenrobot.eventbus.EventBus



class MyLocationService : Service() {


    companion object {

        //TODO THIS INTO SETTINGS THIS
        var LOCATION_INTERVAL = 1
        var LOCATION_DISTANCE = 1f

        val KEY_LOCATION_INTERVAL = "LOCATION_INTERVAL"
        val KEY_LOCATION_TIMER = "LOCATION_TIMER"
        val KEY_LOCATION_DISTANCE = "LOCATION_DISTANCE"

        private val TAG = "MyLocationService"


    }

    private var mLocationManager: LocationManager? = null

    //    var timer: Boolean = false
    private var mLocationListeners = arrayOf(LocationListener(LocationManager.PASSIVE_PROVIDER))

    private inner class LocationListener(provider: String) : android.location.LocationListener {
        var initialized = false
        internal var currentID: Int = 0
        internal var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
            Log.v(
                TAG,
                "Location?? ${mLastLocation.time} ${mLastLocation.altitude} ${mLastLocation.latitude} ${mLastLocation.longitude}"
            )
        }

        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location.")

            var time = location.time - mLastLocation.time


            mLastLocation.set(location)

            var parentId = currentID
            var latitude = location.latitude
            var longitude = location.longitude
//            gps.ele = location.altitude
            var timestamp = location.time

            val gps = GPS(pKey = 0, parentId = parentId, longitude = longitude,latitude = latitude,timestamp = timestamp)


            putDB(gps, time)


//            if(!timer){
//                Timer(KEY_LOCATION_TIMER, false).scheduleAtFixedRate(500, 2000){
//                    contentResolver.getGPSList()
//                }
//                timer = true
//            }

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


        private fun putDB(gps: GPS, time: Long) {
            doAsync {
                if (initialized) {
                    addGPS(gps)
                    updateTime(gps.parentId, time)
                } else {
                    startMetrics(gps)
                    initialized = true
                    updateTime(gps.parentId, time)

                }

            }
        }

        private fun updateTime(parentId: Int, time: Long) {
            contentResolver.updateRunDistance(time, parentId)
        }


        private fun addGPS(gps: GPS) {
            Log.e("ADDGPS", "gps = " + gps.toString())

            contentResolver.addGPS(gps)

        }

        private fun startMetrics(gps: GPS) {

            Log.e("startMetrics", "gps = ${gps.toString()}")

            val rm = RunMetrics()
            rm.startTime = gps.timestamp
            currentID = contentResolver.startMetrics(rm)
            Log.e("STARMETRICS", "returned id is $currentID which will be assigned")
            gps.parentId = currentID
            contentResolver.addGPS(gps)

        }

        public fun stopMetrics() {
            Log.e("STOPMETRICS", "STOPPING METRICS")
            var rm = RunMetrics()


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


    fun getLocationFromPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val resources = getResources()


        LOCATION_INTERVAL = prefs.getInt(
            KEY_LOCATION_INTERVAL,
            LOCATION_INTERVAL
        )
        LOCATION_DISTANCE = prefs.getFloat(
            KEY_LOCATION_DISTANCE,
            LOCATION_DISTANCE
        )

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
        if (mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    private fun startTracking() {
        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_INTERVAL.toLong(),
                LOCATION_DISTANCE,
                mLocationListeners[0]
            )
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

    }

    private fun stopTracking() {
        if (mLocationManager != null) {
            var caught = false
            for (i in mLocationListeners.indices) {
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
                    doAsync{
                        mLocationListeners[i].stopMetrics()
                    }


                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (ex: Exception) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex)
                    true
                }

            }
            if (!caught) {
                mLocationManager = null

            }
        }
    }

    override fun stopService(name: Intent?): Boolean {
        stopTracking()
        return super.stopService(name)
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
                stopTracking()
            }
            PAUSE -> {

            }


        }

    }


}