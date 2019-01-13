package com.example.eddy.basetrackerpsyegb.activities

import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.CalendarView
import com.example.eddy.basetrackerpsyegb.database.GPS
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.*
import com.example.eddy.basetrackerpsyegb.utils.RunUtils.Companion.formatDecimal
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.savvi.rangedatepicker.CalendarPickerView
import kotlinx.android.synthetic.main.activity_run_stats.*
import kotlinx.android.synthetic.main.activity_run_stats.all_runs_map
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class AllRunOverviewActivity : AppCompatActivity(), RunOverview.OverviewListener {


    lateinit var map: GoogleMap

    override fun DBReady(overview: RunOverview.OverviewData) {


        //Uses View Model to format DB data and sets it to the Text Views. Also binds to the GPS points

        val speedFormat = "%.2f".format(overview.avgSpeed) + "M/S"
        val avgSpeed = "Average Speed : $speedFormat"
        overview_avgSpeed.text = avgSpeed
        var dist = RunUtils.getDistance(overview.totalDistance.toDouble())
        dist /= 1000
        val distanceFormat = "%.2f".format(dist) + "KM"
        val totalDistance = "Total Distance: $distanceFormat"
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
        this.setDrawGridBackground(false)

        this.description.isEnabled = true
        this.description.text = "Distance Per Journey"

        this.animateY(1000)
        this.legend.isEnabled = true
        this.setPinchZoom(false)
        this.data.setDrawValues(false)


    }


    private fun moveMap(gps: GPS, title: String) {

        if (::map.isInitialized) {
            map.clear()
            val latLng = LatLng(gps.latitude, gps.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(MapUtils.bitmapDescriptorFromVector(this, R.drawable.ic_run))
            )
                .title = title

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))

        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_stats)



        initDateRange()


        (all_runs_map as SupportMapFragment).getMapAsync {
            map = it

        }

    }


    fun initDateRange() {
        //Gets a rough min/max for the last
        //7 days = week
        //30 days = month
        //last 24 hours as Day
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val now = Calendar.getInstance()
        val weekMin = Calendar.getInstance()
        weekMin.add(Calendar.DATE, -3)
        val weekMax = Calendar.getInstance()
        weekMax.add(Calendar.DATE, 4)
        val monthMax = Calendar.getInstance()
        monthMax.add(Calendar.DATE, 15)
        val monthMin = Calendar.getInstance()
        monthMin.add(Calendar.DATE, -15)
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)










        btnDateNow.setOnClickListener { RunOverview(this, this, min = yesterday.time.time, max = now.time.time) }

        btnDateWeek.setOnClickListener { RunOverview(this, this, min = weekMin.time.time, max = weekMax.time.time) }
        btnDateMonth.setOnClickListener { RunOverview(this, this, min = monthMin.time.time, max = monthMax.time.time) }

        btnDateAll.setOnClickListener { RunOverview(this, this, 0L, 0L) }

    }

}


