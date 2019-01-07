package com.example.eddy.basetrackerpsyegb.DB

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics.Companion.END_TIME
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics.Companion.ID
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics.Companion.START_TIME
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics.Companion.TOTAL_DISTANCE
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics.Companion.TOTAL_TIME

const val METRICS_AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.DB.contentprovider/metricstable"
const val UPDATE_METRICS_AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.DB.contentprovider/updatetable"
const val UPDATE_TOTAL_TIME_AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.DB.contentprovider/updatetimetable"


fun ContentResolver.getRuns(): List<RunMetrics> {
    val cursor = query(Uri.parse(METRICS_AUTHORITY), null, null, null, null)!!
    val runList = ArrayList<RunMetrics>()
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {

        var run = RunMetrics()

        run.id = cursor.getInt(cursor.getColumnIndex(RunMetrics.ID))
        run.startTime = cursor.getLong(cursor.getColumnIndex(RunMetrics.START_TIME))
        run.endTime = cursor.getLong(cursor.getColumnIndex(RunMetrics.ID))
        run.totalDistance = cursor.getFloat(cursor.getColumnIndex(RunMetrics.TOTAL_DISTANCE))
        run.totalTime = cursor.getString(cursor.getColumnIndex(RunMetrics.TOTAL_TIME))
        runList.add(run)
        cursor.moveToNext()

        Log.v("Resolver: getRuns()", run.toString())
    }
    cursor.close()
    return runList
}


/*
1. Implement getting a run by it's id,
2. Implement ending a run by toggle
3. Test if these work
4. Create a list of all runs
5. Idk
 */
fun ContentResolver.getRun(id: Int): RunMetrics {
    val cursor = query(
        ContentUris.withAppendedId(Uri.parse(METRICS_AUTHORITY), id.toLong()),
        null,
        null,
        null,
        null
    )

    var run = RunMetrics()
    if(!cursor.moveToFirst()){cursor.moveToFirst()}

    run.id = cursor.getInt(cursor.getColumnIndex(RunMetrics.ID))
    run.startTime = cursor.getLong(cursor.getColumnIndex(RunMetrics.START_TIME))
    run.endTime = cursor.getLong(cursor.getColumnIndex(RunMetrics.END_TIME))
    run.totalDistance = cursor.getFloat(cursor.getColumnIndex(RunMetrics.TOTAL_DISTANCE))
    run.totalTime = cursor.getString(cursor.getColumnIndex(RunMetrics.TOTAL_TIME))
    cursor.moveToNext()

    Log.v("Resolver: getRuns()", run.toString())

    cursor.close()

    return run
}

fun ContentResolver.startMetrics(metrics: RunMetrics): Int {
    val cv = ContentValues()

    cv.put(RunMetrics.START_TIME, metrics.startTime)
    cv.put(RunMetrics.END_TIME, metrics.endTime)
    cv.put(RunMetrics.TOTAL_DISTANCE, metrics.totalDistance)
    val uriRet = insert(Uri.parse(METRICS_AUTHORITY), cv)
    val rmID = ContentUris.parseId(uriRet)
    metrics.id = rmID.toInt()
    Log.v("Resolver: StartMetrics", metrics.toString())
    return metrics.id


}


fun ContentResolver.endMetrics(time: Long, id: Int): RunMetrics {
    val cv = ContentValues()
    cv.put(RunMetrics.ID, id)
    cv.put(RunMetrics.END_TIME, time)

    val uriRet = update(Uri.parse(METRICS_AUTHORITY), cv, null, null)
    Log.e("ENDMETRICS", "URIRET $uriRet ID $id")

    if (uriRet == 1) {
        return getRun(id)
    } else {
        throw NoSuchFieldException("ID Not Found")
    }
}


fun ContentResolver.updateRunDistance(distance: Float, id: Int){
    val cv = ContentValues()
    cv.put(RunMetrics.ID, id)
    cv.put(RunMetrics.TOTAL_DISTANCE, distance)


    update(Uri.parse(UPDATE_METRICS_AUTHORITY), cv, null, null)

}

fun ContentResolver.updateTotalDuration(duration: String, id: Int){
    val cv = ContentValues()
    cv.put(RunMetrics.ID, id)
    cv.put(RunMetrics.TOTAL_TIME, duration)


    update(Uri.parse(UPDATE_TOTAL_TIME_AUTHORITY), cv, null, null)

}


//
//fun ContentResolver.getAllRuns(): ArrayList<RunMetrics> {
//    val cursor = query(
//        Uri.parse(METRICS_AUTHORITY),
//        null,
//        null,
//        null,
//        null)
//
//
//    val runs = ArrayList<RunMetrics>()
//    cursor.moveToFirst()
//    while (!cursor.isAfterLast) {
//        var id = cursor.getInt(cursor.getColumnIndex(ID))
//        var start = cursor.getLong(cursor.getColumnIndex(START_TIME))
//        var end = cursor.getLong(cursor.getColumnIndex(END_TIME))
//        var dist = cursor.getFloat(cursor.getColumnIndex(TOTAL_DISTANCE))
//        val totalTime = cursor.getString(cursor.getColumnIndex(TOTAL_TIME))
//
//        val rm = RunMetrics(id = id, startTime = start, endTime = end, totalDistance = dist, totalTime = totalTime)
////        gps.ele = cursor.getDouble(cursor.getColumnIndex(GPS.ELE))
//        runs.add(rm)
//        cursor.moveToNext()
//        Log.v("Resolver: getAllRuns()", rm.toString())
//    }
//    cursor.close()
//    return runs
//}

fun ContentResolver.deleteRun(id: Long) {
    delete(
        ContentUris.withAppendedId(Uri.parse(METRICS_AUTHORITY), id.toLong()),
        null,
        null
    )
}


