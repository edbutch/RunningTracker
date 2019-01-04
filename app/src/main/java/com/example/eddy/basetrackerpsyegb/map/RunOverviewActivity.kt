package com.example.eddy.basetrackerpsyegb.map

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.getGPSList
import com.example.eddy.basetrackerpsyegb.DB.getRun
import com.example.eddy.basetrackerpsyegb.utils.ElevationChartUtils
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.MapUtils
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
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
            }

        }
    }


    @UiThread
    private fun setOverviewData(duration: String, distance: String, avgSpeed: String, calories: String) {
        overview_duration.text = duration
        overview_distance.text = distance
        overview_avg_speed.text = avgSpeed
        overview_calories.text = calories
    }

    private fun getRunDataAsync() {
        doAsync {
            val points = arrayListOf<LatLng>()
            gpsList = contentResolver.getGPSList(id)
            runMetrics = contentResolver.getRun(id)
            val lineData = ElevationChartUtils.getChartLineData(gpsList)

            for (gps in gpsList) {
                val lat = gps.latitude
                val long = gps.longitude
                val latLng = LatLng(lat, long)
                if (!(points.contains(latLng))) {
                    points.add(latLng)
                }
            }
            uiThread {
                ElevationChartUtils.initializeChart(
                    elechart,
                    lineData,
                    backgroundColor = getColor(R.color.colorAccent),
                    holeColor = getColor(R.color.colorPrimaryLight)
                )
                if (points.isNotEmpty()) {
                    polyLine = MapUtils.drawPolyLine(map, points)
                }

                val time = RunUtils.getDuration(runMetrics.endTime, runMetrics.startTime)
                val speed = "Average Speed: ${RunUtils.getAverageSpeed(gpsList).toString()}m/s"
                val distance = "Distance:  ${runMetrics.totalDistance}M"
                setOverviewData(
                    duration = time,
                    distance = distance,
                    avgSpeed = speed,
                    calories = "No Calories"
                )

            }

        }
    }


}
