package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.jetbrains.anko.uiThread

class RunsOverview(context: Context){
    lateinit var runsOverview: Array<Overview>
    
    lateinit var runMetrics : List<RunMetrics>
    lateinit var gpsList : MutableList<List<GPS>>
    init {
        doAsync {
             runMetrics = context.contentResolver.getRuns()
            gpsList = arrayListOf<List<GPS>>()
            for (metric in runMetrics) {
                gpsList.add(context.contentResolver.getGPSList(metric.id))
            }

            uiThread {

            }


         }
    }

    class Overview(runMetric: RunMetrics , runList: List<GPS> ){

    }
}


