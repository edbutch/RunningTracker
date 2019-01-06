package com.example.eddy.basetrackerpsyegb.utils

import android.graphics.Color
import androidx.annotation.UiThread
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

class MapUtils{

    companion object {
        @UiThread
        fun drawPolyLine(map: GoogleMap?, points: ArrayList<LatLng>): Polyline {
            val start = points[0]
            val end = points[points.size - 1]
            val mid = (points.size / 2)

            val polyLine = map!!.addPolyline(PolylineOptions().width(3f).color(Color.BLACK))
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

            return polyLine
        }
    }

}