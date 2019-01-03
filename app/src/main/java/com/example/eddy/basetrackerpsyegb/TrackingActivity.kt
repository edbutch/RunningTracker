package com.example.eddy.basetrackerpsyegb

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View.*
import com.example.eddy.basetrackerpsyegb.Service.MyLocationService
import com.example.eddy.basetrackerpsyegb.Service.ServiceEvent
import kotlinx.android.synthetic.main.activity_tracking.*
import org.greenrobot.eventbus.EventBus

class TrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)


        startService(Intent(this, MyLocationService::class.java))

        trackingBtnStart.setOnClickListener { startTracking() }
        trackingBtnStop.setOnClickListener { stopTracking() }


    }


    private fun startTracking() {
//        trackingBtnStart.visibility = INVISIBLE
        EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.START))
        trackingBtnStop.visibility = VISIBLE
    }


    private fun stopTracking() {
        trackingBtnStop.visibility = INVISIBLE
        EventBus.getDefault().post(ServiceEvent(ServiceEvent.Control.STOP))
        trackingBtnStart.visibility = VISIBLE

    }

}
