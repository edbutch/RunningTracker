package com.example.eddy.basetrackerpsyegb.utils

import android.graphics.Color
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.*

class ElevationChartUtils {


    companion object {

        const val TAG = "ElevationChartUtils"
        fun initializeChart(elechart: LineChart, lineData: LineData, holeColor: Int, backgroundColor: Int) {
            Log.e(TAG, "Init chart")
            (lineData.getDataSetByIndex(0) as LineDataSet).circleHoleColor = holeColor

            // no description text
            elechart.description.isEnabled = true
            elechart.description.text = "Elevation / Location"

            // chart.setDrawHorizontalGrid(false);
            // enable / disable grid background
            elechart.setDrawGridBackground(false)
//        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);

            // enable touch gestures
            elechart.setTouchEnabled(true)

            // enable scaling and dragging
            elechart.isDragEnabled = true
            elechart.setScaleEnabled(true)

            // if disabled, scaling can be done on x- and y-axis separately
            elechart.setPinchZoom(false)
            elechart.setBackgroundColor(backgroundColor)


            // set custom chart offsets (automatic offset calculation is hereby disabled)
            elechart.setViewPortOffsets(10f, 0f, 10f, 0f)

            // add data
            elechart.data = lineData

            // get the legend (only possible after setting data)
            val l = elechart.legend
            l.isEnabled = false

            elechart.axisLeft.isEnabled = false
            elechart.axisLeft.spaceTop = 40f
            elechart.axisLeft.spaceBottom = 40f
            elechart.axisRight.isEnabled = false

            elechart.xAxis.isEnabled = false

            // animate calls invalidate()...
            elechart.animateX(250)

        }


        fun getChartLineData(gpsList: ArrayList<GPS>): LineData {
            gpsList.sortByDescending { it.timestamp }
            gpsList.reverse()

            val entries = arrayListOf<Entry>()
            for ((index, gps) in gpsList.withIndex()) {
                entries.add(Entry(index.toFloat(), gps.elevation.toFloat()))
            }

            val dataSet = LineDataSet(entries, "Elevation ")

            dataSet.setLineWidth(1.75f)
            dataSet.setCircleRadius(5f)
            dataSet.setCircleHoleRadius(2.5f)
            dataSet.setColor(Color.WHITE)
            dataSet.setCircleColor(Color.WHITE)
            dataSet.setHighLightColor(Color.WHITE)
            dataSet.setDrawValues(false)

            return LineData(dataSet)
        }


    }

}