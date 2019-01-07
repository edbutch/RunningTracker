package com.example.eddy.basetrackerpsyegb.utils

import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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




//        fun sortTimeByDay(list: ArrayList<GPS>): ArrayList<GPS>{
//            val dateTimeStrToLocalDateTime: (String) -> LocalDateTime = {
//                LocalDateTime.parse(it, DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm"))
//            }
//
//        }
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

        fun getDuration(time: Long): String {
            var mins = (TimeUnit.MILLISECONDS.toMinutes(time)) % 60
            var seconds = (TimeUnit.MILLISECONDS.toSeconds(time)) % 60
            var hours = (TimeUnit.MILLISECONDS.toHours(time))

            var dur = ""
            if (hours > 0) {
                Log.v(TAG, "hour > 0 ")
                dur += "$hours:$mins:$seconds"

            } else {
                Log.v(TAG, " 0 > hours ")

                dur += "$mins:$seconds"
            }

            return dur
        }

        fun getAverageSpeed(list: ArrayList<GPS>): Float {
            var totalSpeed: Float = 0F
            list.map { totalSpeed += it.speed }
            return totalSpeed / list.size
        }
    }


}

