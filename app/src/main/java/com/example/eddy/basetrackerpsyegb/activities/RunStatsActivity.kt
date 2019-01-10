package com.example.eddy.basetrackerpsyegb.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.eddy.basetrackerpsyegb.AllJourneys
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.RunOverview
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import kotlinx.android.synthetic.main.activity_run_overview.*
import kotlinx.android.synthetic.main.activity_run_stats.*
import org.jetbrains.anko.doAsync

class RunStatsActivity : AppCompatActivity(),  RunOverview.OverviewListener {


    override fun DBReady(overview: RunOverview.OverviewData) {
        doAsync {
            val avgSpeed = "Average Speed : ${overview.avgSpeed}"
            overview_avg_speed.text = avgSpeed
            val totalDistance = "Total Distance: ${RunUtils.getDistance(overview.totalDistance.toDouble())}KM"
            overview_totalDistance.text = totalDistance

            val maxEle = "Highest Point: ${String.format("{0:f2}", overview.maxEle)}M"

            overview_maxEle.text = maxEle
            val minEle = "Lowest Point: ${String.format("{0:f2}", overview.minEle)}M"
            overview_minEle.text = minEle

            val timeRunning = "Total time ran: ${RunUtils.getDuration(overview.totalTime)}"


        }
    }


    private fun fillView() {


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_stats)
        RunOverview(this, this)

    }
}
