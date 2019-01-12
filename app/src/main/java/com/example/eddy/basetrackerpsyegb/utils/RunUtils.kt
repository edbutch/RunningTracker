package com.example.eddy.basetrackerpsyegb.utils

import android.util.Log
import com.example.eddy.basetrackerpsyegb.database.GPS
import com.example.eddy.basetrackerpsyegb.database.RunMetrics
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RunUtils(val metrics: RunMetrics, val gpsList: List<GPS>) {

    companion object {
        val TAG = "RunUtils"

        fun getDate(timeStamp: Long): String {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val date = Date(timeStamp)
            return format.format(date)
        }


        fun getDuration(currentTime: Long, previousTime: Long): String {

            var time = currentTime - previousTime
            var mins = (TimeUnit.MILLISECONDS.toMinutes(time)) % 60
            var seconds = (TimeUnit.MILLISECONDS.toSeconds(time)) % 60
            var hours = (TimeUnit.MILLISECONDS.toHours(time))

            var dur = ""
            if (hours > 0) {
                Log.v(TAG, "hour > 0 ")
                dur += "${hours}H:${mins}M:${seconds}S"

            } else {
                Log.v(TAG, " 0 > hours ")

                dur += "${mins}M:${seconds}S"
            }

            return dur
        }

        fun getDuration(time: Long): String {
            var mins = (TimeUnit.MILLISECONDS.toMinutes(time)) % 60
            var seconds = (TimeUnit.MILLISECONDS.toSeconds(time)) % 60
            var hours = (TimeUnit.MILLISECONDS.toHours(time))

            var dur = ""
            if (hours > 0) {
                Log.v(TAG, "hour > 0 ")
                dur += "${hours}H:${mins}M:${seconds}S"

            } else {
                Log.v(TAG, " 0 > hours ")

                dur += "${mins}M:${seconds}S"
            }

            return dur
        }




        fun formatDecimal(data: Any): String {
            return String.format("%.2f", data)
        }


        fun getDistance(d: Double): Double{
            return Math.round(d * 1000.0) / 1000.0
        }

        fun getAverageSpeed(totalDistance: Long, timeTaken: Long){


        }
        fun getAverageSpeed(list: List<GPS>): Float {
            var totalSpeed: Float = 0F
            list.map { totalSpeed += it.speed }
            return totalSpeed / list.size
        }

        fun getAverageEle(list: List<GPS>): Double {
            var totalEle: Double = 0.0
            list.map { totalEle += it.elevation }
            return totalEle / list.size
        }
    }


}

