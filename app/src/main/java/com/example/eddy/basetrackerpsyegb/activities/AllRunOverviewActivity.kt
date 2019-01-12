package com.example.eddy.basetrackerpsyegb.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.eddy.basetrackerpsyegb.database.GPS
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.ChartUtils
import com.example.eddy.basetrackerpsyegb.utils.MapUtils
import com.example.eddy.basetrackerpsyegb.utils.RunOverview
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import com.example.eddy.basetrackerpsyegb.utils.RunUtils.Companion.formatDecimal
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_run_stats.*
import kotlinx.android.synthetic.main.activity_run_stats.all_runs_map

class AllRunOverviewActivity : AppCompatActivity(), RunOverview.OverviewListener {


    override fun DBReady(overview: RunOverview.OverviewData) {


        //Uses View Model to format DB data and sets it to the Text Views. Also binds to the GPS points
        val speedFormat = "%.2f".format(overview.avgSpeed) + "M/S"
        val avgSpeed = "Average Speed : $speedFormat"
        overview_avgSpeed.text = avgSpeed
        val distance = RunUtils.getDistance(overview.totalDistance.toDouble())
        val totalDistance = "Total Distance: ${formatDecimal(distance)}KM"
        overview_totalDistance.text = totalDistance
        val maxEle = "Highest Point: ${formatDecimal(overview.maxEle.elevation)}M"
        overview_maxEle.text = maxEle
        overview_highest_point.setOnClickListener { moveMap(overview.maxEle, "Highest Point") }
        val minEle = "Lowest Point: ${formatDecimal(overview.minEle.elevation)}M"
        overview_minEle.text = minEle
        overview_lowest_point.setOnClickListener { moveMap(overview.minEle, "Lowest Point") }
        val timeRunning = "Total time ran: ${RunUtils.getDuration(overview.totalTime)}"
        overview_totalTime.text = timeRunning
        val maxSpeed = "Max Speed: ${formatDecimal(overview.maxSpeed.speed)}M/S"
        overview_maxSpeed.text = maxSpeed
        overview_fastest_speed.setOnClickListener { moveMap(overview.maxSpeed, "Max Speed") }


        //Filling up the Charts
        distancebarchart.setUpDistanceChart(overview.distanceList)

        val data = ChartUtils.getSpeedLineData(overview.speedList)


        Log.e("TEST", "tESTOMG")

        for (speed in overview.speedList) {
            if(speed.isInfinite()){
                Log.e("TEST", "overview speed $speed")

            }
        }


        ChartUtils.initializeLineChart(
            this,
            "Speed",
            "Average Speed per Journey",
            overallSpeedChart,
            data,
            backgroundColor = getColor(R.color.colorAccent),
            holeColor = getColor(R.color.colorPrimaryLight)
        )


    }


    private fun BarChart.setUpDistanceChart(distanceList: List<Float>) {

        val barData = ChartUtils.getDistanceBarData(this@AllRunOverviewActivity, distanceList)


        this.data = barData


        this.xAxis.position = XAxis.XAxisPosition.BOTTOM
//        this.xAxis.enableGridDashedLine(5f, 5f, 0f)
//        this.axisRight.enableGridDashedLine(5f, 5f, 0f)
//        this.axisLeft.enableGridDashedLine(5f, 5f, 0f)
//        this.description.position =

//        this.axisLeft.disableAxisLineDashedLine()
//        this.axisLeft.disableGridDashedLine()
//        this.axisRight.disableGridDashedLine()
//        this.axisRight.disableAxisLineDashedLine()
        this.setDrawGridBackground(false)

        this.description.isEnabled = true
        this.description.text = "Distance Per Journey"

        this.animateY(1000)
        this.legend.isEnabled = true
        this.setPinchZoom(false)
        this.data.setDrawValues(false)


    }


    private fun moveMap(gps: GPS, title: String) {
        Log.v(title, gps.toString())

        map?.clear()
        val latLng = LatLng(gps.latitude, gps.longitude)
        map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(MapUtils.bitmapDescriptorFromVector(this, R.drawable.ic_run))
        )
            .title = title

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))


    }



    lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_stats)
        RunOverview(this, this)



        (all_runs_map as SupportMapFragment).getMapAsync {
            map = it

        }

    }
}


