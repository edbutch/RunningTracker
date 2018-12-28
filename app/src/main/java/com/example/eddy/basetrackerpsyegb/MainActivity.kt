package com.example.eddy.basetrackerpsyegb

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.v(TAG, "onCreate")




        btnStartTracker.setOnClickListener { startTracker() }





    }

    private fun startTracker() {
//        startService(Intent(this, MyLocationService::class.java))
        startActivity(Intent(this, TrackingActivity::class.java))
    }
}
