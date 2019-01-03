package com.example.eddy.basetrackerpsyegb.map

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.getGPSList
import com.example.eddy.basetrackerpsyegb.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.Arrays.asList
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Gap


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


class PolyDecodeDemoActivity : BaseDemoActivity(), GoogleMap.OnPolylineClickListener {


    override fun onPolylineClick(p0: Polyline?) {
        Log.v("OnPolyLine", ";asd;")

//        for(latlng in p0!!.points){
//            Log.e("itearating", "lat : ${latlng.latitude} long : ${latlng.longitude}")
//        }
        Log.e("onPolylineClick", "P0 ${p0.toString()}")
    }

    var id: Int = 0
    lateinit var polyLine: Polyline


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("EEEEEEEEEE", "EEEEEEEEEEEE")

        id = intent.getIntExtra(RunMetrics.ID, 0)
        Log.e("ONCREATE", "ID $id")


    }

    private lateinit var coordList: ArrayList<LatLng>

    protected override fun startDemo() {
        val decodedPath = PolyUtil.decode(LINE)

        map!!.setOnPolylineClickListener(this)



        doAsync {
            coordList = arrayListOf<LatLng>()
            val list = contentResolver.getGPSList(id)
            for (gps in list) {
                val latitude = gps.latitude
                val longitude = gps.longitude
                val latLong = LatLng(latitude, longitude)

                if (!coordList.contains(latLong)) {
                    coordList.add(latLong)
                }
            }

            uiThread {
                if (!coordList.isEmpty()) {
                    drawLine(coordList)
                } else {
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


        val start = points[0]
        val end = points[points.size - 1]

        val mid = points.size / 2
        polyLine = map!!.addPolyline(PolylineOptions().width(3f).color(Color.BLACK))
        polyLine.isClickable = true
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(points[mid], 18f))




        polyLine.width = 8F
        polyLine.jointType = JointType.ROUND
        val gap: PatternItem = Gap(20F)

        val dot = Dot()


        val patternList = arrayListOf(gap, dot)
        polyLine.pattern = patternList

//        polyLine.startCap = CustomCap(BitmapDescriptorFactory.fromResource(android.R.drawable.arrow_down_float))
//        polyLine.endCap = CustomCap(BitmapDescriptorFactory.fromResource(android.R.drawable.arrow_down_float))


        map!!.addMarker(
            MarkerOptions()
                .position(start)
                .title("START")

        )
        map!!.addMarker(
            MarkerOptions()
                .position(end)
                .title("END")
        )




        polyLine.points = points
    }

    companion object {
        private val LINE =
            "rvumEis{y[}DUaBGu@EqESyCMyAGGZGdEEhBAb@DZBXCPGP]Xg@LSBy@E{@SiBi@wAYa@AQGcAY]I]KeBm@_Bw@cBu@ICKB}KiGsEkCeEmBqJcFkFuCsFuCgB_AkAi@cA[qAWuAKeB?uALgB\\eDx@oBb@eAVeAd@cEdAaCp@s@PO@MBuEpA{@R{@NaAHwADuBAqAGE?qCS[@gAO{Fg@qIcAsCg@u@SeBk@aA_@uCsAkBcAsAy@AMGIw@e@_Bq@eA[eCi@QOAK@O@YF}CA_@Ga@c@cAg@eACW@YVgDD]Nq@j@}AR{@rBcHvBwHvAuFJk@B_@AgAGk@UkAkBcH{@qCuAiEa@gAa@w@c@o@mA{Ae@s@[m@_AaCy@uB_@kAq@_Be@}@c@m@{AwAkDuDyC_De@w@{@kB_A}BQo@UsBGy@AaA@cLBkCHsBNoD@c@E]q@eAiBcDwDoGYY_@QWEwE_@i@E}@@{BNaA@s@EyB_@c@?a@F}B\\iCv@uDjAa@Ds@Bs@EyAWo@Sm@a@YSu@c@g@Mi@GqBUi@MUMMMq@}@SWWM]C[DUJONg@hAW\\QHo@BYIOKcG{FqCsBgByAaAa@gA]c@I{@Gi@@cALcEv@_G|@gAJwAAUGUAk@C{Ga@gACu@A[Em@Sg@Y_AmA[u@Oo@qAmGeAeEs@sCgAqDg@{@[_@m@e@y@a@YIKCuAYuAQyAUuAWUaA_@wBiBgJaAoFyCwNy@cFIm@Bg@?a@t@yIVuDx@qKfA}N^aE@yE@qAIeDYaFBW\\eBFkANkANWd@gALc@PwAZiBb@qCFgCDcCGkCKoC`@gExBaVViDH}@kAOwAWe@Cg@BUDBU`@sERcCJ{BzFeB"
    }
}
