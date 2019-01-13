package com.example.eddy.basetrackerpsyegb.utils

import android.graphics.Color
import android.util.Log
import com.example.eddy.basetrackerpsyegb.database.GPS
import com.github.mikephil.charting.charts.LineChart
import java.util.*
import android.view.Gravity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.widget.FrameLayout
import com.example.eddy.basetrackerpsyegb.R
import com.github.mikephil.charting.data.*


class ChartUtils {


    companion object {

        const val TAG = "ChartUtils"


        fun initializeSpeedChart(context: Context, speedchart: LineChart, lineData: LineData, holeColor: Int, backgroundColor: Int): LineChart{
            Log.e(TAG, "Init chart")
            (lineData.getDataSetByIndex(0) as LineDataSet).circleHoleColor = holeColor


            speedchart.setDrawGridBackground(false)
            speedchart.setTouchEnabled(true)

            speedchart.isDragEnabled = false
            speedchart.setScaleEnabled(false)

            speedchart.setPinchZoom(false)
            speedchart.setBackgroundColor(backgroundColor)



            speedchart.data = lineData

            val l = speedchart.legend
            l.isEnabled = false

            speedchart.axisLeft.isEnabled = true
//            speedchart.axisLeft.spaceTop = 40f
            speedchart.axisLeft.spaceBottom = 40f
            speedchart.axisRight.isEnabled = false

            speedchart.xAxis.isEnabled = false


            // no description text
            speedchart.description.isEnabled = true
            speedchart.description.text = "Speed Time Graph"

            val yAxisName = VerticalTextView(context, null)
            yAxisName.text = "Speed(m/s)"
            val params2 =
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            params2.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL


            speedchart.addView(yAxisName, params2)




            // animate calls invalidate()...
            speedchart.animateX(250)
            return speedchart

        }


        fun initializeLineChart(context: Context,title: String, description: String, elechart: LineChart, lineData: LineData, holeColor: Int, backgroundColor: Int): LineChart {
            Log.e(TAG, "Init chart")
            (lineData.getDataSetByIndex(0) as LineDataSet).circleHoleColor = holeColor


            // chart.setDrawHorizontalGrid(false);
            // enable / disable grid background
            elechart.setDrawGridBackground(false)

            // set to false.
            //Will be interesting to work with after the exam season
            //Clicking points could take you there on the map
            // I have shown this functionality in the buttons, however integrating with the graphing would be very fun
            //CLicking bar graphs could draw the journeys on the overview map too
            elechart.setTouchEnabled(false)

            // enable scaling and dragging
            elechart.isDragEnabled = false
            elechart.setScaleEnabled(false)

            // if disabled, scaling can be done on x- and y-axis separately
            elechart.setPinchZoom(false)
            elechart.setBackgroundColor(backgroundColor)



            // add data
            elechart.data = lineData

            // get the legend (only possible after setting data)
            val l = elechart.legend
            l.isEnabled = false

            elechart.axisLeft.isEnabled = true
//            elechart.axisLeft.spaceTop = 40f
            elechart.axisLeft.spaceBottom = 40f
            elechart.axisRight.isEnabled = false


            elechart.xAxis.isEnabled = false





            // no description text
            elechart.description.isEnabled = true
            elechart.description.text = description

            val yAxisName = VerticalTextView(context, null)
            yAxisName.setText(title)
            val params2 =
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            params2.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL


            elechart.addView(yAxisName, params2)

            // animate calls invalidate()...
            elechart.animateX(250)
            return elechart

        }



        fun gpsToEleLine(gpsList: ArrayList<GPS>): LineData {
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

        fun gpsToSpeedLine(gpsList: ArrayList<GPS>): LineData {
            gpsList.sortByDescending { it.timestamp }
            gpsList.reverse()

            val entries = arrayListOf<Entry>()
            for ((index, gps) in gpsList.withIndex()) {
                Log.v("SortedBy","REVERSETIME ${gps.toString()}")

                entries.add(Entry(index.toFloat(), gps.speed.toFloat()))
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
        fun getSpeedLineData( speedList: List<Float>): LineData{
            val entries = arrayListOf<Entry>()

            for((index, speed) in speedList.withIndex()){
                if(speed.isFinite()){
                    entries.add(Entry(index.toFloat(), speed))
                }else{
                    entries.add(Entry(index.toFloat(), 0F))

                }
            }
            val dataSet = LineDataSet(entries, "Speed ")

            dataSet.setLineWidth(1.75f)
            dataSet.setCircleRadius(5f)
            dataSet.setCircleHoleRadius(2.5f)
            dataSet.setColor(Color.WHITE)
            dataSet.setCircleColor(Color.WHITE)
            dataSet.setHighLightColor(Color.WHITE)
            dataSet.setDrawValues(false)

            return LineData(dataSet)

        }

        fun getDistanceBarData(context: Context, distanceList: List<Float>): BarData {
            val entries = arrayListOf<BarEntry>()

            for((index, dist) in distanceList.withIndex()){
                Log.e("Testttt", "inted ${index.toFloat()}, dist $dist")
                entries.add(BarEntry(index.toFloat(), dist, "Run $index"))
            }
            val dataSet = BarDataSet(entries, "A Run")

            dataSet.highLightColor = context.getColor(R.color.colorPrimaryDark)
            dataSet.setDrawValues(false)


            dataSet.color = ContextCompat.getColor(context, R.color.colorPrimaryLight)

            return BarData(dataSet)



        }




    }

}