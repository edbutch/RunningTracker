package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat

class RunsOverview(context: Context) {
    var newRunOverview: MutableList<Overview> = arrayListOf()

    var runsOverview: MutableList<Overview> = arrayListOf()
    val sdf = SimpleDateFormat("yyyyMMdd")

    init {
        doAsync {

            val metrics = context.contentResolver.getRuns()

            for (metric in metrics) {
                val gps = context.contentResolver.getGPSList(metric.id)
                runsOverview.add(Overview(runMetric = metric, runList = gps))
            }


            lateinit var lastRun: Overview
            runsOverview.forEachIndexed { index, overview ->
                if (index == 0) {
                    lastRun = overview
                }else{
                    if(isSameDay(lastRun.runMetric.startTime, overview.runMetric.startTime)){

                        val newOverview = concatOverviews(lastRun, overview)
                        //The days match!

                    }
                    lastRun = overview
                }

            }


        }
    }

    private fun concatOverviews(lastRun: Overview, overview: Overview): OverallData {
        val retOverview : Overview


        val speed1 = lastRun.runList.sortedByDescending { it.speed }[lastRun.runList.lastIndex].speed
        val speed2 = overview.runList.sortedByDescending { it.speed }[overview.runList.lastIndex].speed
        val topSpeed = maxOf(speed1,speed2)

        val speedList : List<GPS> = (lastRun.runList + overview.runList)
        val averageSpeed = RunUtils.getAverageSpeed(speedList)

        val alt1 = lastRun.runList.sortedByDescending { it.elevation }[lastRun.runList.lastIndex].elevation
        val alt2 = overview.runList.sortedByDescending { it.elevation }[overview.runList.lastIndex].elevation
        val topAlt = maxOf(alt1,alt2)


        val totalDistance = lastRun.runMetric.totalDistance + overview.runMetric.totalDistance

        Log.e("concatOverviews", "\nTop speed = $topSpeed \nAverage Speed = $averageSpeed\n Top Altitude = $topAlt\n Total Distance = $totalDistance ")





        return OverallData(topSpeed, averageSpeed, topAlt, totalDistance)

    }

    fun isSameDay(date1: Long , date2: Long): Boolean{
        return sdf.format(date1).equals(sdf.format(date2))
    }

    data class OverallData(val topSpeed: Float, val averageSpeed: Float, val topAlt: Double, val totalDistance: Float)


    class Overview(var runMetric: RunMetrics, var runList: List<GPS>) {


        override fun toString(): String {
            return "Overview(runMetric=$runMetric, runList=$runList')"
        }

    }
}


