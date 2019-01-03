package com.example.eddy.basetrackerpsyegb

import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import java.text.SimpleDateFormat
import java.util.*

class RunUtils(val metrics: RunMetrics, val gpsList: ArrayList<GPS>){
    lateinit var lastGPS : GPS
    fun getLength() : Long{
        for (g in gpsList){
            if(!(::lastGPS.isInitialized)){

            }



        }
        return 0L
    }

    fun getLatArr()  {

    }
    fun getDistance() = metrics.totalDistance
    fun getDuration(): Long= metrics.endTime - metrics.startTime
}

fun RunUtils.getDate(timestamp: Long): String {
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val date = Date(timestamp)
    return format.format(date)
}

