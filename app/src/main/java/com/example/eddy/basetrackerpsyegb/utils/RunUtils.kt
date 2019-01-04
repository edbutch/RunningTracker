package com.example.eddy.basetrackerpsyegb.utils

import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RunUtils(val metrics: RunMetrics, val gpsList: ArrayList<GPS>) {

    companion object {
        val TAG = "RunUtils"

        fun getDate(timeStamp: Long): String {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val date = Date(timeStamp)
            return format.format(date)
        }

        fun getDuration(endTime: Long, startTime: Long): String {
            var time = endTime - startTime
            var mins = (TimeUnit.MILLISECONDS.toMinutes(time)) % 60
            var seconds = (TimeUnit.MILLISECONDS.toSeconds(time)) % 60
            var hours = (TimeUnit.MILLISECONDS.toHours(time))

            var dur = "Duration: "
            if (hours > 0) {
                Log.v(TAG, "hour > 0 ")
                dur += "$hours:$mins:$seconds"

            } else {
                Log.v(TAG, " 0 > hours ")

                dur += "$mins:$seconds"
            }

            return dur
        }

        fun getAverageSpeed(list : ArrayList<GPS>): Float{
            var totalSpeed: Float = 0F
            list.map { totalSpeed+=it.speed }
            return totalSpeed/list.size
        }
    }


    lateinit var lastGPS: GPS
    fun getLength(): Long {
        for (g in gpsList) {
            if (!(::lastGPS.isInitialized)) {

            }


        }
        return 0L
    }

    fun getLatArr() {

    }

    fun getDistance() = metrics.totalDistance
    fun getDuration(): Long = metrics.endTime - metrics.startTime
}

