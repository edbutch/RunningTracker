package com.example.eddy.basetrackerpsyegb.map

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.getGPSList
import com.example.eddy.basetrackerpsyegb.DB.getRun
import com.example.eddy.basetrackerpsyegb.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_run_overview.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class RunOverviewActivity : AppCompatActivity() {


    lateinit var polyLine: Polyline
    var map: GoogleMap? = null
    val TAG = "RunOverviewActivity"
    var id = 0
    lateinit var gpsList: ArrayList<GPS>
    lateinit var runMetrics: RunMetrics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_overview)

        (overview_map as SupportMapFragment).getMapAsync {
            Log.e(TAG, "mapasync???")
            map = it
            map?.uiSettings?.isMyLocationButtonEnabled = true

            if (intent.hasExtra(RunMetrics.ID)) {
                id = intent.getIntExtra(RunMetrics.ID, 0)
                getRunDataAsync()
                initializeChart()
            }

        }
    }

    private fun initializeChart() {
        
    }


    private fun getRunDataAsync() {
        doAsync {
            val points = arrayListOf<LatLng>()
            gpsList = contentResolver.getGPSList(id)
            runMetrics = contentResolver.getRun(id)
            for (gps in gpsList) {
                val lat = gps.latitude
                val long = gps.longitude
                val latLng = LatLng(lat, long)
                if (!(points.contains(latLng))) {
                    points.add(latLng)
                }
            }
            uiThread {
                if (points.isNotEmpty()) {
                    drawLine(points)
                }
            }

        }
    }

    @UiThread
    private fun drawLine(points: ArrayList<LatLng>) {
        val start = points[0]
        val end = points[points.size - 1]
        val mid = (points.size / 2)

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

}
