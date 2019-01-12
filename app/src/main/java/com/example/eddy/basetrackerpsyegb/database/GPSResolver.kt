package com.example.eddy.basetrackerpsyegb.database


import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.ELE
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.PKEY
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.LONGITUDE
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.LATITUDE
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.PARENTID
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.SPEED
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.TIME

const val GPS_AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.database.contentprovider/gpsdb"
fun ContentResolver.addGPS(gps: GPS): Int {
    val cv = ContentValues()

    cv.put(PKEY, gps.pKey)
    cv.put(PARENTID, gps.parentId)
    cv.put(TIME, gps.timestamp)
    cv.put(LONGITUDE, gps.longitude)
    cv.put(LATITUDE, gps.latitude)

    cv.put(GPS.ELE, gps.elevation)
    cv.put(GPS.SPEED, gps.speed)

    val uriRet = insert(Uri.parse(GPS_AUTHORITY), cv)
    val gpsID = ContentUris.parseId(uriRet)
    gps.pKey = gpsID.toInt()
    Log.v("Resolver: AddGPS", gps.toString())
    return gps.parentId
}




fun ContentResolver.getGPSList(id: Int): ArrayList<GPS> {
    val cursor = query(
        ContentUris.withAppendedId(Uri.parse(GPS_AUTHORITY), id.toLong()),
        null,
        null,
        null,
        null)

    val gpsList = ArrayList<GPS>()
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {

        var pKey = cursor.getInt(cursor.getColumnIndex(PKEY))
        var parentId = cursor.getInt(cursor.getColumnIndex(PARENTID))
        var time = cursor.getLong(cursor.getColumnIndex(TIME))
        var lat = cursor.getDouble(cursor.getColumnIndex(LATITUDE))
        var long = cursor.getDouble(cursor.getColumnIndex(LONGITUDE))
        var ele = cursor.getDouble(cursor.getColumnIndex(GPS.ELE))
        var speed = cursor.getFloat(cursor.getColumnIndex(GPS.SPEED))

        val gps = GPS(pKey = pKey, parentId = parentId, timestamp = time, latitude = lat, longitude = long,elevation = ele, speed = speed)
        gpsList.add(gps)
        cursor.moveToNext()
//        Log.v("Resolver: GetGPSList", gps.toString())
    }
    cursor.close()
    return gpsList
}





fun ContentResolver.getAllGPSList(): List<GPS> {
    val cursor = query(
        Uri.parse(GPS_AUTHORITY),
        null,
        null,
        null,
        null)


    val gpsList = arrayListOf<GPS>()
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        var pKey = cursor.getInt(cursor.getColumnIndex(PKEY))
        var parentId = cursor.getInt(cursor.getColumnIndex(PARENTID))
        var time = cursor.getLong(cursor.getColumnIndex(TIME))
        var lat = cursor.getDouble(cursor.getColumnIndex(LATITUDE))
        var long = cursor.getDouble(cursor.getColumnIndex(LONGITUDE))
        var ele = cursor.getDouble(cursor.getColumnIndex(ELE))
        var speed = cursor.getFloat(cursor.getColumnIndex(SPEED))

        val gps = GPS(pKey = pKey, parentId = parentId, timestamp = time, latitude = lat, longitude = long, elevation = ele, speed = speed)

//        gps.ele = cursor.getDouble(cursor.getColumnIndex(GPS.ELE))
        gpsList.add(gps)
        cursor.moveToNext()
        Log.v("Resolver: GetGPSList", gps.toString())
    }
    cursor.close()
    return gpsList
}