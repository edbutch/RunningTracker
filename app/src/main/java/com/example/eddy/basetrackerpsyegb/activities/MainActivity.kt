package com.example.eddy.basetrackerpsyegb.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.RunsOverview
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Log.v(TAG, "onCreate")

        RunsOverview(context = this)

        btnStartTracker.setOnClickListener { startTracker() }
        btnViewAllRuns.setOnClickListener { viewAllRuns() }
        btnOverview.setOnClickListener { viewRunOverview() }
        btnBoringList.setOnClickListener { startBoringList() }
    }

    private fun viewRunOverview() {

    }

    private fun viewAllRuns() {
        startActivity(Intent(this, LiteListDemoActivity::class.java))
    }
    private fun startBoringList(){
                startActivity(Intent(this, AllRunsActivity::class.java))

    }

    private fun startTracker() {
        startActivity(Intent(this, TrackingActivity::class.java))
    }
}
