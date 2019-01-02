package com.example.eddy.basetrackerpsyegb.map

import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import java.util.ArrayList

/*
 * Copyright 2015 Sean J. Barbeau
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




class PolySimplifyDemoActivity : BaseDemoActivity() {

    protected override fun startDemo() {
        val mMap = map

        // Original line
        val line = PolyUtil.decode(LINE)
        mMap?.addPolyline(
            PolylineOptions()
                .addAll(line)
                .color(Color.BLACK)
        )

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(28.05870, -82.4090), 15f))

        var simplifiedLine: List<LatLng>

        /*
         * Simplified lines - increasing the tolerance will result in fewer points in the simplified
         * line
         */
        var tolerance = 5.0 // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        mMap?.addPolyline(
            PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.RED - ALPHA_ADJUSTMENT)
        )

        tolerance = 20.0 // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        mMap?.addPolyline(
            PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.GREEN - ALPHA_ADJUSTMENT)
        )

        tolerance = 50.0 // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        mMap?.addPolyline(
            PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.MAGENTA - ALPHA_ADJUSTMENT)
        )

        tolerance = 500.0 // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        mMap?.addPolyline(
            PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.YELLOW - ALPHA_ADJUSTMENT)
        )

        tolerance = 1000.0 // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        mMap?.addPolyline(
            PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.BLUE - ALPHA_ADJUSTMENT)
        )


        // Triangle polygon - the polygon should be closed
        val triangle = ArrayList<LatLng>()
        triangle.add(LatLng(28.06025, -82.41030))  // Should match last point
        triangle.add(LatLng(28.06129, -82.40945))
        triangle.add(LatLng(28.06206, -82.40917))
        triangle.add(LatLng(28.06125, -82.40850))
        triangle.add(LatLng(28.06035, -82.40834))
        triangle.add(LatLng(28.06038, -82.40924))
        triangle.add(LatLng(28.06025, -82.41030))  // Should match first point

        mMap?.addPolygon(
            PolygonOptions()
                .addAll(triangle)
                .fillColor(Color.BLUE - ALPHA_ADJUSTMENT)
                .strokeColor(Color.BLUE)
                .strokeWidth(5f)
        )

        // Simplified triangle polygon
        tolerance = 88.0 // meters
        val simplifiedTriangle = PolyUtil.simplify(triangle, tolerance)
        mMap?.addPolygon(
            PolygonOptions()
                .addAll(simplifiedTriangle)
                .fillColor(Color.YELLOW - ALPHA_ADJUSTMENT)
                .strokeColor(Color.YELLOW)
                .strokeWidth(5f)
        )

        // Oval polygon - the polygon should be closed
        val oval = PolyUtil.decode(OVAL_POLYGON)
        mMap?.addPolygon(
            PolygonOptions()
                .addAll(oval)
                .fillColor(Color.BLUE - ALPHA_ADJUSTMENT)
                .strokeColor(Color.BLUE)
                .strokeWidth(5f)
        )

        // Simplified oval polygon
        tolerance = 10.0 // meters
        val simplifiedOval = PolyUtil.simplify(oval, tolerance)
        mMap?.addPolygon(
            PolygonOptions()
                .addAll(simplifiedOval)
                .fillColor(Color.YELLOW - ALPHA_ADJUSTMENT)
                .strokeColor(Color.YELLOW)
                .strokeWidth(5f)
        )
    }

    companion object {

        private val LINE =
            "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@"
        private val OVAL_POLYGON =
            "}wgjDxw_vNuAd@}AN{A]w@_Au@kAUaA?{@Ke@@_@C]D[FULWFOLSNMTOVOXO\\I\\CX?VJXJTDTNXTVVLVJ`@FXA\\AVLZBTATBZ@ZAT?\\?VFT@XGZAP"
        private val ALPHA_ADJUSTMENT = 0x77000000
    }
}
