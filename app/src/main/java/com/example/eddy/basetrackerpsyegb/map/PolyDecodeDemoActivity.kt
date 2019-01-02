package com.example.eddy.basetrackerpsyegb.map

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.getAllGPSList
import com.example.eddy.basetrackerpsyegb.DB.getGPSList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


class PolyDecodeDemoActivity : BaseDemoActivity() {

    var id : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent.extras?.getInt(RunMetrics.ID) != null){
            id = intent.extras!!.getInt(RunMetrics.ID)
        }
    }

    protected override fun startDemo() {
        val decodedPath = PolyUtil.decode(LINE)


        doAsync {
            var coordList = arrayListOf<LatLng>()
            var list = contentResolver.getGPSList(id)
            for(gps in list){
                val latitude = gps.latitude
                val longitude = gps.longitude
                Log.v("iterating: ", "gps : ${gps.toString()}")
                val latLong = LatLng(latitude, longitude)

                if(!coordList.contains(latLong)){
                    coordList.add(latLong)
                }
            }

            uiThread {
                if(!coordList.isEmpty()){
                    drawLine(coordList)
                }else{
                    Log.e("emptylist", "corord list is empty")
                }

            }
        }



//        map?.addPolyline(PolylineOptions().addAll(decodedPath))
    }


    fun drawLine(points: List<LatLng>?) {
        if (points == null) {
            Log.e("Draw Line", "got null as parameters")
            return
        }

        val mid = points.size / 2
        val line = map!!.addPolyline(PolylineOptions().width(3f).color(Color.RED))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(points[mid], 24f))

        line.points = points
    }

    companion object {
        private val LINE =
            "rvumEis{y[}DUaBGu@EqESyCMyAGGZGdEEhBAb@DZBXCPGP]Xg@LSBy@E{@SiBi@wAYa@AQGcAY]I]KeBm@_Bw@cBu@ICKB}KiGsEkCeEmBqJcFkFuCsFuCgB_AkAi@cA[qAWuAKeB?uALgB\\eDx@oBb@eAVeAd@cEdAaCp@s@PO@MBuEpA{@R{@NaAHwADuBAqAGE?qCS[@gAO{Fg@qIcAsCg@u@SeBk@aA_@uCsAkBcAsAy@AMGIw@e@_Bq@eA[eCi@QOAK@O@YF}CA_@Ga@c@cAg@eACW@YVgDD]Nq@j@}AR{@rBcHvBwHvAuFJk@B_@AgAGk@UkAkBcH{@qCuAiEa@gAa@w@c@o@mA{Ae@s@[m@_AaCy@uB_@kAq@_Be@}@c@m@{AwAkDuDyC_De@w@{@kB_A}BQo@UsBGy@AaA@cLBkCHsBNoD@c@E]q@eAiBcDwDoGYY_@QWEwE_@i@E}@@{BNaA@s@EyB_@c@?a@F}B\\iCv@uDjAa@Ds@Bs@EyAWo@Sm@a@YSu@c@g@Mi@GqBUi@MUMMMq@}@SWWM]C[DUJONg@hAW\\QHo@BYIOKcG{FqCsBgByAaAa@gA]c@I{@Gi@@cALcEv@_G|@gAJwAAUGUAk@C{Ga@gACu@A[Em@Sg@Y_AmA[u@Oo@qAmGeAeEs@sCgAqDg@{@[_@m@e@y@a@YIKCuAYuAQyAUuAWUaA_@wBiBgJaAoFyCwNy@cFIm@Bg@?a@t@yIVuDx@qKfA}N^aE@yE@qAIeDYaFBW\\eBFkANkANWd@gALc@PwAZiBb@qCFgCDcCGkCKoC`@gExBaVViDH}@kAOwAWe@Cg@BUDBU`@sERcCJ{BzFeB"
    }
}
