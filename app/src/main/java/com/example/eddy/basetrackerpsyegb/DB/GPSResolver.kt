package com.mdp.eddy.g53recipebook.DB

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.util.Log

const val AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.DB.contentprovider/gpsDB"
fun ContentResolver.addGPS(gps: GPS): Int{
    val cv = ContentValues()
    cv.put(GPS.PARENTID, gps.parentId)
    cv.put(GPS.LAT, gps.lat)
    cv.put(GPS.LONG, gps.long)
    cv.put(GPS.ELE, gps.ele)
    cv.put(GPS.TIME, gps.time)

    val uriRet = insert(Uri.parse(AUTHORITY), cv)
    val gpsID = ContentUris.parseId(uriRet)
    gps.parentId = gpsID.toInt()
    Log.v("Resolver: AddGPS", gps.toString())
    return gps.parentId
}




fun ContentResolver.getGPSList(): ArrayList<GPS> {
    val cursor = query(Uri.parse(AUTHORITY), null, null, null, null)!!
    val gpsList = ArrayList<GPS>()
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        val gps = GPS()
        gps.parentId = cursor.getInt(cursor.getColumnIndex(GPS.PARENTID))
        gps.lat = cursor.getDouble(cursor.getColumnIndex(GPS.LAT))
        gps.long = cursor.getDouble(cursor.getColumnIndex(GPS.LONG))
        gps.ele = cursor.getDouble(cursor.getColumnIndex(GPS.ELE))
        gps.time = cursor.getLong(cursor.getColumnIndex(GPS.TIME))
        gpsList.add(gps)
        cursor.moveToNext()
        Log.v("Resolver: GetGPSList", gps.toString())
    }
    cursor.close()
    return gpsList
}


