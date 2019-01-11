package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import android.util.Log
import androidx.annotation.UiThread
import com.example.eddy.basetrackerpsyegb.DB.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat

class RunOverview(context: Context, callBack: OverviewListener) {
    var newRunOverview: MutableList<Overview> = arrayListOf()

    var runsOverview: MutableList<Overview> = arrayListOf()
    val sdf = SimpleDateFormat("yyyyMMdd")

    init {
        doAsync {

            val metrics = context.contentResolver.getRuns()

            val distanceList = arrayListOf<Float>()
            val speedList = arrayListOf<Float>()

            for (metric in metrics) {
                val gps = context.contentResolver.getGPSList(metric.id)
                runsOverview.add(Overview(runMetric = metric, runList = gps))
            }

            var minEle  = GPS()
            var totalTime: Long = 0L
            var totalDistance: Float = 0F
            var maxEle = GPS()
            var maxSpeed = GPS()
            var avgSpeed: Float = 0F
            for (overview in runsOverview) {
                Log.e("Overview: ", "Runlist ID ${overview.runList[0].parentId} + Metric ID ${overview.runMetric.id}")
//                voverview.runList.sortedByDescending { it.speed }.get(0).speed
                avgSpeed += RunUtils.getAverageSpeed(overview.runList)


                totalTime += overview.runMetric.totalTime

                totalDistance += overview.runMetric.totalDistance
                val mSpeed = overview.runList.sortedByDescending { it.speed }[0]

                if (mSpeed.speed > maxSpeed.speed) {
                    maxSpeed = mSpeed

                }
                val mEle =  overview.runList.sortedByDescending { it.elevation }[0]
                if(mEle.elevation > maxEle.elevation){
                    maxEle = mEle
                }

                val mnEle = overview.runList.sortedBy { it.elevation }[0]

                if(mnEle.elevation < minEle.elevation){
                    minEle = mnEle
                }

                distanceList.add(overview.runMetric.totalDistance)



                val runSpeed = overview.runMetric.totalDistance/ (overview.runMetric.totalTime/1000)
                speedList.add(runSpeed)




            }



            //maxele, maxspeed
            avgSpeed = avgSpeed / runsOverview.size
            val averageDistance = totalDistance / runsOverview.size

            uiThread {
                callBack.DBReady(OverviewData(speedList = speedList,distanceList = distanceList,
                    topAlt = maxEle,totalDistance = totalDistance,totalTime = totalTime, maxEle = maxEle, maxSpeed = maxSpeed,
                    minEle = minEle,avgSpeed = avgSpeed, avgDistance = averageDistance ))


            }



        }
    }



    data class OverviewData(
        val speedList: MutableList<Float>,
        val distanceList: MutableList<Float>,
        val topAlt: GPS,
        val totalDistance: Float,
        val maxEle: GPS,
        val maxSpeed: GPS,
        val minEle: GPS,
        val avgSpeed: Float,
        val avgDistance: Float,
        val totalTime: Long
    )


//    private fun concatOverviews(lastRun: Overview, overview: Overview): OverallData {
//        val retOverview: Overview
//
//
//        val speed1 = lastRun.runList.sortedByDescending { it.speed }[lastRun.runList.lastIndex].speed
//        val speed2 = overview.runList.sortedByDescending { it.speed }[overview.runList.lastIndex].speed
//        val topSpeed = maxOf(speed1, speed2)
//
//        val speedList: List<GPS> = (lastRun.runList + overview.runList)
//        val averageSpeed = RunUtils.getAverageSpeed(speedList)
//
//        val alt1 = lastRun.runList.sortedByDescending { it.elevation }[lastRun.runList.lastIndex].elevation
//        val alt2 = overview.runList.sortedByDescending { it.elevation }[overview.runList.lastIndex].elevation
//        val topAlt = maxOf(alt1, alt2)
//
//
//        val totalDistance = lastRun.runMetric.totalDistance + overview.runMetric.totalDistance
//
//        Log.e(
//            "concatOverviews",
//            "\nTop speed = $topSpeed \nAverage Speed = $averageSpeed\n Top Altitude = $topAlt\n Total Distance = $totalDistance "
//        )
//
//
//
//
//
//        return OverallData(topSpeed, averageSpeed, topAlt, totalDistance)
//
//    }

    fun isSameDay(date1: Long, date2: Long): Boolean {
        return sdf.format(date1).equals(sdf.format(date2))
    }

//    data class OverallData(val topSpeed: Float, val averageSpeed: Float, val topAlt: Double, val totalDistance: Float)


    class Overview(var runMetric: RunMetrics, var runList: List<GPS>) {


        override fun toString(): String {
            return "Overview(runMetric=$runMetric, runList=$runList')"
        }

    }

    interface OverviewListener {
        fun DBReady(overview: OverviewData)
    }
}


