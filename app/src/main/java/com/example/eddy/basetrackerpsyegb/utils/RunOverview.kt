package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import android.util.Log
import com.example.eddy.basetrackerpsyegb.database.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat

class RunOverview(context: Context, callBack: OverviewListener) {

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

            var maxEle  = GPS()
            var totalTime: Long = 0L
            var totalDistance: Float = 0F
            //Mount Everest is the highestp oint in the world, so this is initialized to mount everest
            var minEle = GPS(99,99,99L,27.9881,86.9250,8850.9,99F)
            var maxSpeed = GPS()
            var avgSpeed: Float = 0F
            for (overview in runsOverview) {
                Log.e("Overview: ", "Runlist ID ${overview.runList[0].parentId} + Metric ID ${overview.runMetric.id}")
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



    class Overview(var runMetric: RunMetrics, var runList: List<GPS>) {


        override fun toString(): String {
            return "Overview(runMetric=$runMetric, runList=$runList')"
        }

    }

    interface OverviewListener {
        fun DBReady(overview: OverviewData)
    }
}


