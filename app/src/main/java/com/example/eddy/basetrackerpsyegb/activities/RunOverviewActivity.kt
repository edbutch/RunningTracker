package com.example.eddy.basetrackerpsyegb.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.getGPSList
import com.example.eddy.basetrackerpsyegb.DB.getRun
import com.example.eddy.basetrackerpsyegb.utils.ChartUtils
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.MapUtils
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_run_overview.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class RunOverviewActivity : AppCompatActivity(), OnChartValueSelectedListener {


    val points = arrayListOf<LatLng>()
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
            }

        }
    }


    @UiThread
    private fun setOverviewData(duration: String, distance: String, avgSpeed: String) {
        overview_duration.text = duration
        overview_distance.text = distance
        overview_avg_speed.text = avgSpeed
    }

    private fun getRunDataAsync() {
        doAsync {
            gpsList = contentResolver.getGPSList(id)
            runMetrics = contentResolver.getRun(id)
            val eleLineData = ChartUtils.getEleLineData(gpsList)
            val speedLineData = ChartUtils.getSpeedLineData(gpsList)
            for (gps in gpsList) {
                val lat = gps.latitude
                val long = gps.longitude
                val latLng = LatLng(lat, long)
                if (!(points.contains(latLng))) {
                    points.add(latLng)
                }
            }
            uiThread {
//TODO
//                initBarChart()
                ChartUtils.initializeEleChart(this@RunOverviewActivity,
                    elechart,
                    eleLineData,
                    backgroundColor = getColor(R.color.colorAccent),
                    holeColor = getColor(R.color.colorPrimaryLight)
                )

                ChartUtils.initializeSpeedChart(this@RunOverviewActivity, speedchart, speedLineData, getColor(R.color.colorAccent), getColor(R.color.colorPrimaryLight))


                if (points.isNotEmpty()) {
                    polyLine = MapUtils.drawPolyLine(map, points)

                    val start = points[0]
                    val end = points[points.size - 1]



                    map!!.addMarker(MarkerOptions()
                        .position(start)
                        .icon(MapUtils.bitmapDescriptorFromVector(this@RunOverviewActivity, R.drawable.ic_run)))
                        .title = "START"

                    map!!.addMarker(MarkerOptions()
                        .position(end)
                        .icon(MapUtils.bitmapDescriptorFromVector(this@RunOverviewActivity, R.drawable.ic_run)))
                        .title = "END"

                }

//                elechart.setOnChartValueSelectedListener(this@RunOverviewActivity)

                val time = runMetrics.totalTime
                Log.v(TAG, "TIME : $time")
                val speedMetresSecond = runMetrics.totalDistance/ (runMetrics.totalTime/1000)
                val speed = "Average Speed: ${speedMetresSecond}m/s"
                val d = runMetrics.totalDistance / 1000

//                val distanceRounded:Double = Math.round(d * 1000.0) / 1000.0

                val distanceRounded = runMetrics.totalDistance
                val distance = "Distance:  ${distanceRounded}KM"
                setOverviewData(
                    duration = RunUtils.getDuration(time),
                    distance = distance,
                    avgSpeed = speed
                )

            }

        }
    }

    override fun onNothingSelected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        /*TODO*/
        /*Make it so when you select a value it takes you to the point on the run*/
        /*After spending 2 days on this, I've had to add it to the back stack*/
        var x = e?.x
        Log.e("aksdkasd", "aaaa $x")

        x!!
        if (points.size < x) {
            val lat = points[x?.toInt()].latitude
            val long = points[x?.toInt()].longitude
            val latLng = LatLng(lat, long)

            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 22f))

        }
    }


}
