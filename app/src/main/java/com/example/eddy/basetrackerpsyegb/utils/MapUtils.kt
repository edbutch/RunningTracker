package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import android.graphics.Color
import androidx.annotation.UiThread
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.example.eddy.basetrackerpsyegb.R
import com.google.android.gms.maps.model.BitmapDescriptor



class MapUtils{

    companion object {
        @UiThread
        fun drawPolyLine(map: GoogleMap?, points: List<LatLng>): Polyline {
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


        fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
            val background = ContextCompat.getDrawable(context, R.drawable.ic_baseline_place_24px)
            background!!.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
            val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
            vectorDrawable!!.setBounds(40, 20, vectorDrawable.intrinsicWidth + 40, vectorDrawable.intrinsicHeight + 20)
            val bitmap =
                Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            background.draw(canvas)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

}