package com.example.eddy.basetrackerpsyegb

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics

class RunStats : AppCompatActivity(), AllJourneys.DBReadyCallback {

    lateinit var runMetrics: ArrayList<RunMetrics>
    lateinit var runList: ArrayList<ArrayList<GPS>>
    override fun dbReady(runMetrics: ArrayList<RunMetrics>, runList: ArrayList<ArrayList<GPS>>) {
        this.runMetrics = runMetrics
        this.runList = runList
        filLView()
    }

    private fun filLView() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_stats)
        val journeys = AllJourneys(this, this)
    }
}
