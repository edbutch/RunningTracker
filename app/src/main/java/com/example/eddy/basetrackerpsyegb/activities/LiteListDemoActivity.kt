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
 */

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.eddy.basetrackerpsyegb.AllJourneys
import com.example.eddy.basetrackerpsyegb.DB.GPS
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.utils.RunUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.lite_list_demo.*
import java.util.concurrent.TimeUnit


/**
 * This shows to include a map in lite mode in a ListView.
 * Note the use of the view holder pattern with the
 * [com.google.android.gms.maps.OnMapReadyCallback].
 */
class LiteListDemoActivity : AppCompatActivity(), AllJourneys.DBReadyCallback {


    val context: Context = this
    private var linearLayoutManager: LinearLayoutManager? = null
    private var mGridLayoutManager: GridLayoutManager? = null

    /**
     * RecycleListener that completely clears the [com.google.android.gms.maps.GoogleMap]
     * attached to a row in the RecyclerView.
     * Sets the map type to [com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE] and clears
     * the map.
     */
    private val mRecycleListener = RecyclerView.RecyclerListener { holder ->
        val mapHolder = holder as MapAdapter.ViewHolder
        if (mapHolder != null && mapHolder.map != null) {
            // Clear the map and free up resources by changing the map type to none.
            // Also reset the map when it gets reattached to layout, so the previous map would
            // not be displayed.
            mapHolder.map!!.clear()
            mapHolder.map!!.mapType = GoogleMap.MAP_TYPE_NONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lite_list_demo)

        mGridLayoutManager = GridLayoutManager(this, 2)
        linearLayoutManager = LinearLayoutManager(this)

        AllJourneys(this, this)

    }


    override fun dbReady(
        runMetrics: List<RunMetrics>,
        runList: List<List<GPS>>
    ) {
        recycler_view!!.setHasFixedSize(true)
        recycler_view!!.layoutManager = linearLayoutManager
        recycler_view!!.adapter = MapAdapter(runMetrics, runList)
        recycler_view!!.setRecyclerListener(mRecycleListener)
    }


    /**
     * Adapter that displays a title and [com.google.android.gms.maps.MapView] for each item.
     * The layout is defined in `lite_list_demo_row.xml`. It contains a MapView
     * that is programatically initialised in
     * [.]
     */
    private inner class MapAdapter(
        private val runMetrics: List<RunMetrics>,
        private val runList: List<List<GPS>>
    ) :
        RecyclerView.Adapter<MapAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.allruns_row_element, parent, false)
            )
        }

        /**
         * This function is called when the user scrolls through the screen and a new item needs
         * to be shown. So we will need to bind the holder with the details of the next item.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder == null) {
                return
            }
            holder.bindView(position)
        }

        override fun getItemCount(): Int {
            return runMetrics.size
        }

        /**
         * Holder for Views used in the [LiteListDemoActivity.MapAdapter].
         * Once the  the `map` field is set, otherwise it is null.
         * When the [.onMapReady] callback is received and
         * the [com.google.android.gms.maps.GoogleMap] is ready, it stored in the [.map]
         * field. The map is then initialised with the NamedLocation that is stored as the tag of the
         * MapView. This ensures that the map is initialised with the latest data that it should
         * display.
         */
        internal inner class ViewHolder internal constructor(var layout: View) : RecyclerView.ViewHolder(layout),
            OnMapReadyCallback {


            //Created my own view for this anc added my own views to the holder
            var mapView: MapView? = null
            var title: TextView
            var map: GoogleMap? = null
            var date: TextView = layout.findViewById(R.id.lite_listrow_date)
            var distance: TextView = layout.findViewById(R.id.lite_listrow_distance)
            var duration: TextView = layout.findViewById(R.id.lite_listrow_duration)

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

            /**
             * Displays a [LiteListDemoActivity.NamedLocation] on a
             * [com.google.android.gms.maps.GoogleMap].
             * Adds a marker and centers the camera on the NamedLocation with the normal map type.
             */

            /*TODO*/
            private fun setMapLocation() {
                if (map == null) return

                val data = mapView!!.tag as RunUtils ?: return


                val latLong = LatLng(data.gpsList[0].latitude, data.gpsList[0].longitude)


//
//                latlng.longitude = gpsList[0].longitude
//                latlng.latitude = gpsList[0].latitude
//                // Add a marker for this item and set the camera
                map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13f))
                map!!.addMarker(MarkerOptions().position(latLong))

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
                //todo
                val item = RunUtils(runMetrics[pos], runList[pos])
                // Store a reference of the ViewHolder object in the layout.
                layout.tag = this
                // Store a reference to the item in the mapView's tag. We use it to get the
                // coordinate of a location, when setting the map location.
                mapView!!.tag = item
                setMapLocation()

                title.text = "Run ${pos+1}"

                val dateText = "Started at ${RunUtils.getDate(runMetrics[pos].startTime)}"
                date.text =dateText
                duration.text = runMetrics[pos].totalTime
                //TODO DISTANCE

                val distanceRounded:Double = Math.round(runMetrics[pos].totalDistance * 1000.0) / 1000.0
                val dist = "${distanceRounded.toString()}KM"
                distance.text = dist

            }
        }
    }


    private fun calculateDuration(startTime: Long, endTime: Long): String {

        var durMillis = (endTime - startTime)

        val hms = String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(durMillis),
            TimeUnit.MILLISECONDS.toMinutes(durMillis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(durMillis) % TimeUnit.MINUTES.toSeconds(1)
        )

        return hms
    }


}