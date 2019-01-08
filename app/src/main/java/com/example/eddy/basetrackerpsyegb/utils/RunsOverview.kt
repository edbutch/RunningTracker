package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.jetbrains.anko.uiThread

class RunsOverview(context: Context) {
    var runsOverview: MutableList<Overview> = arrayListOf()

    init {
        doAsync {

            val metrics = context.contentResolver.getRuns()
            Log.e("METRCISIZE ", metrics.size.toString())

            for (metric in metrics) {
                val gps = context.contentResolver.getGPSList(metric.id)
                runsOverview.add(Overview(runMetric = metric, runList = gps))
            }

            for(overview in runsOverview){
               Log.v("runsOverview",overview
                   .toString() )
            }



        }
    }

    class Overview(var runMetric: RunMetrics, var runList: List<GPS>) {



        override fun toString(): String {
            return "Overview(runMetric=$runMetric, runList=$runList')"
        }

    }
}


