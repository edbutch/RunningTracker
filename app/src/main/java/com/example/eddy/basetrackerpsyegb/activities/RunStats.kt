package com.example.eddy.basetrackerpsyegb.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.eddy.basetrackerpsyegb.AllJourneys
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.R

class RunStats : AppCompatActivity(), AllJourneys.DBReadyCallback {

    lateinit var runMetrics: List<RunMetrics>
    lateinit var runList: List<List<GPS>>
    override fun dbReady(runMetrics: List<RunMetrics>, runList: List<List<GPS>>) {
        this.runMetrics = runMetrics
        this.runList = runList
        filLView()
    }

    private fun filLView() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_stats)
        AllJourneys(this, this)
    }
}
