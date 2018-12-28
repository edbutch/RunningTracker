package com.example.eddy.basetrackerpsyegb

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.eddy.basetrackerpsyegb.Service.MyLocationService

class TrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)


        startService(Intent(this, MyLocationService::class.java))

        /*
        trackingTxtTitle
        trackingTxtElaspedTime
        trackingTxtTotalDistance
         */



    }



}
