package com.example.eddy.basetrackerpsyegb

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.eddy.basetrackerpsyegb.map.PolyDecodeDemoActivity
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : BaseActivity() {

    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Log.v(TAG, "onCreate")

        btnStartTracker.setOnClickListener { startTracker() }
        btnViewMap.setOnClickListener { viewMap() }
        btnViewTrips.setOnClickListener { viewAllRuns() }
        btnSettings.setOnClickListener{startLiteList()}
    }

    private fun startLiteList() {
        startActivity(Intent(this, LiteListDemoActivity::class.java))
    }

    private fun viewAllRuns() {
        startActivity(Intent(this, AllRunsActivity::class.java))
    }

    private fun viewMap() {
        startActivity(Intent(this, PolyDecodeDemoActivity::class.java
        ))
    }

    private fun startTracker() {
//        startService(Intent(this, MyLocationService::class.java))
        startActivity(Intent(this, TrackingActivity::class.java))
    }
}
