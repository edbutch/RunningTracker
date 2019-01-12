package com.example.eddy.basetrackerpsyegb.activities

import android.content.Context
import android.content.Intent


/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Used and modified code from this java class :
  * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo/LiteListDemoActivity.java
  *
 *
 */

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.eddy.basetrackerpsyegb.utils.AllJourneys
import com.example.eddy.basetrackerpsyegb.database.GPS
import com.example.eddy.basetrackerpsyegb.database.RunMetrics
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.MapUtils
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_run_list.*


class RunListActivity : AppCompatActivity(), AllJourneys.DBReadyCallback {


    val context: Context = this
    private var linearLayoutManager: LinearLayoutManager? = null


    private val recyclerListener = RecyclerView.RecyclerListener { holder ->
        val mapHolder = holder as RunsListAdapter.ViewHolder
        if (mapHolder.map != null) {
            // Clear the map and free up resources by changing the map type to none.
            // Also reset the map when it gets reattached to layout, so the previous map would
            // not be displayed.
            mapHolder.map!!.clear()
            mapHolder.map!!.mapType = GoogleMap.MAP_TYPE_NONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_list)

        linearLayoutManager = LinearLayoutManager(this)

    }

    override fun onResume() {
        super.onResume()
        AllJourneys(this, this)
    }

    //AllJourneys callback
    override fun dbReady(
        runMetrics: List<RunMetrics>,
        runList: List<List<GPS>>
    ) {
        run_list_view!!.adapter = null
        run_list_view!!.layoutManager = null
        run_list_view!!.setHasFixedSize(true)
        run_list_view!!.layoutManager = linearLayoutManager
        run_list_view!!.adapter = RunsListAdapter(runMetrics, runList)
        run_list_view!!.setRecyclerListener(recyclerListener)
    }





    private inner class RunsListAdapter(
        private val runMetrics: List<RunMetrics>,
        private val runList: List<List<GPS>>
    ) :
        RecyclerView.Adapter<RunsListAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.allruns_row_element, parent, false)
            )
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindView(position)
        }

        override fun getItemCount(): Int {
            return runMetrics.size
        }


        internal inner class ViewHolder internal constructor(var layout: View) : RecyclerView.ViewHolder(layout),
            OnMapReadyCallback {


            var mapView: MapView? = null
            var title: TextView
            var map: GoogleMap? = null
            var date: TextView = layout.findViewById(R.id.lite_listrow_date)
            var distance: TextView = layout.findViewById(R.id.lite_listrow_distance)
            var duration: TextView = layout.findViewById(R.id.lite_listrow_duration)
            val cardView: CardView = layout.findViewById(R.id.row_element_card)

            init {
                mapView = layout.findViewById(R.id.lite_listrow_map)
                title = layout.findViewById(R.id.lite_listrow_title)
                if (mapView != null) {
                    // Initialise the MapView
                    mapView!!.onCreate(null)
                    // Set the map ready callback to receive the GoogleMap object
                    mapView!!.getMapAsync(this)
                }
            }

            override fun onMapReady(googleMap: GoogleMap) {
                MapsInitializer.initialize(applicationContext)
                map = googleMap
                setMapLocation()
            }


            private fun setMapLocation() {
                if (map == null) return

                val data = mapView!!.tag as RunUtils


                if(data.gpsList.isNotEmpty()){
                    val latLong = LatLng(data.gpsList[0].latitude, data.gpsList[0].longitude)
                    map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13f))


                    val points = arrayListOf<LatLng>()
                    for (gps in data.gpsList) {
                        val lat = gps.latitude
                        val long = gps.longitude
                        val latLng = LatLng(lat, long)
                        if (!(points.contains(latLng))) {
                            points.add(latLng)
                        }
                    }
                    if(points.isNotEmpty()){MapUtils.drawPolyLine(map,points)}

                }


                // Set the map type back to normal.
                map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
                map!!.setOnMapClickListener {
                    startMapViewActivity(data.metrics.id)
                }
            }


            private fun startMapViewActivity(id: Int) {
                Log.e("startMapViewActivity", "hello ID $id")
                val intent = Intent(context, RunOverviewActivity::class.java)
                intent.putExtra(RunMetrics.ID, id)
                context.startActivity(intent)
            }

            internal fun bindView(pos: Int) {
                val item = RunUtils(runMetrics[pos], runList[pos])
                // Store a reference of the ViewHolder object in the layout.
                layout.tag = this
                // Store a reference to the item in the mapView's tag. We use it to get the
                // coordinate of a location, when setting the map location.
                mapView!!.tag = item
                setMapLocation()

                //"Run ${pos+1}"
                val runTitle = "Run ${runMetrics.size - pos}"
                title.text = runTitle
                cardView.setOnClickListener { startMapViewActivity(runMetrics[pos].id) }


                val dateText = "Started at ${RunUtils.getDate(runMetrics[pos].startTime)}"
                date.text =dateText
                duration.text = RunUtils.getDuration(runMetrics[pos].totalTime)



                val d = "Distance:  ${RunUtils.totalDistanceToKm(runMetrics[pos].totalDistance)}KM"
                distance.text = "$d"


            }
        }
    }




}