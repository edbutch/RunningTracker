package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import com.example.eddy.basetrackerpsyegb.database.GPS
import com.example.eddy.basetrackerpsyegb.database.RunMetrics
import com.example.eddy.basetrackerpsyegb.database.getGPSList
import com.example.eddy.basetrackerpsyegb.database.getRuns
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
//All Journeys takes activity context , and a interface. It uses the context to asynchronously
//
class AllJourneys(val context: Context, callBack: DBReadyCallback) {
    interface DBReadyCallback {
        fun dbReady(
            runMetrics: List<RunMetrics>,
            runList: List<List<GPS>>
        )
    }
    init {
        doAsync {
            val runMetrics = context.contentResolver.getRuns()
            val runList = arrayListOf<List<GPS>>()
            for (metric in runMetrics) {
                runList.add(context.contentResolver.getGPSList(metric.id))
            }
            uiThread {
                callBack.dbReady(runMetrics, runList)
            }
        }
    }





}