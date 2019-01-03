package com.example.eddy.basetrackerpsyegb

import android.content.Context
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.getGPSList
import com.example.eddy.basetrackerpsyegb.DB.getRuns
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AllJourneys(val context: Context, callBack: DBReadyCallback) {
    interface DBReadyCallback {
        fun dbReady(
            runMetrics: ArrayList<RunMetrics>,
            runList: ArrayList<ArrayList<GPS>>
        )
    }
    init {
        doAsync {
            val runMetrics = context.contentResolver.getRuns()
            val runList = arrayListOf<ArrayList<GPS>>()
            for (metric in runMetrics) {
                runList.add(context.contentResolver.getGPSList(metric.id))
                Log.e("metric in ", "metric${toString()}")
            }
            uiThread {
                Log.e("EEK", "EEK")
                callBack.dbReady(runMetrics, runList)
            }
        }
    }


}