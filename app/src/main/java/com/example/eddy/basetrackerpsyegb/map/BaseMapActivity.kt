package com.example.eddy.basetrackerpsyegb.map

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.example.eddy.basetrackerpsyegb.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

/*
 * Copyright 2013 Google Inc.
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

abstract class BaseDemoActivity : FragmentActivity(), OnMapReadyCallback {
    protected var map: GoogleMap? = null
        private set




    protected val layoutId: Int
        get() = R.layout.map

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        setUpMap()
    }

    override fun onResume() {
        super.onResume()
        setUpMap()
    }

    override fun onMapReady(map: GoogleMap) {
        if (this.map != null) {
            return
        }
        this.map = map
        startDemo()
    }

    private fun setUpMap() {
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
    }

    /**
     * Run the demo-specific code.
     */
    protected abstract fun startDemo()
}
