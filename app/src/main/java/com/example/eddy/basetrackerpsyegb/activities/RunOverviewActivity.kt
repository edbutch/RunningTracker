package com.example.eddy.basetrackerpsyegb.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.UiThread
import com.example.eddy.basetrackerpsyegb.database.*
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

class RunOverviewActivity : AppCompatActivity() {


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
            map = it
            map?.uiSettings?.isMyLocationButtonEnabled = true

            if (intent.hasExtra(RunMetrics.ID)) {
                id = intent.getIntExtra(RunMetrics.ID, 0)
                getRunDataAsync()
                deleteCard.setOnClickListener { delete(id) }
            }

        }
    }

    private fun delete(id: Int) {
        doAsync {
            this@RunOverviewActivity.contentResolver.deleteRun(id.toLong())
            uiThread {
                finish()
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
            val eleLineData = ChartUtils.gpsToEleLine(gpsList)
            val speedLineData = ChartUtils.gpsToSpeedLine(gpsList)
            for (gps in gpsList) {
                val lat = gps.latitude
                val long = gps.longitude
                val latLng = LatLng(lat, long)
                if (!(points.contains(latLng))) {
                    points.add(latLng)
                }
            }
            uiThread {

                ChartUtils.initializeLineChart(this@RunOverviewActivity,
                    "Elevation(M)",
                    "Time Elevation Graph",
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


                val time = runMetrics.totalTime
                var speedMetresSecond = runMetrics.totalDistance/ (runMetrics.totalTime/1000)

                if(speedMetresSecond.isInfinite()){speedMetresSecond = 0F}
                val sp = "%.2f".format(speedMetresSecond)
                val speed = "Average Speed: ${sp}m/s"





                val distance = "Distance:  ${RunUtils.totalDistanceToKm(runMetrics.totalDistance)}KM"
                setOverviewData(
                    duration = RunUtils.getDuration(time),
                    distance = distance,
                    avgSpeed = speed
                )

            }

        }
    }




}
