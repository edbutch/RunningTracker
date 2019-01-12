package com.example.eddy.basetrackerpsyegb.DB

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.util.Log

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
        run.endTime = cursor.getLong(cursor.getColumnIndex(RunMetrics.END_TIME))
        run.totalDistance = cursor.getFloat(cursor.getColumnIndex(RunMetrics.TOTAL_DISTANCE))
        run.totalTime = cursor.getLong(cursor.getColumnIndex(RunMetrics.TOTAL_TIME))
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
    run.totalTime = cursor.getLong(cursor.getColumnIndex(RunMetrics.TOTAL_TIME))
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

fun ContentResolver.updateTotalDuration(totalTime: Long, id: Int){
    val cv = ContentValues()
    cv.put(RunMetrics.ID, id)
    cv.put(RunMetrics.TOTAL_TIME, totalTime)


    update(Uri.parse(UPDATE_TOTAL_TIME_AUTHORITY), cv, null, null)

}




fun ContentResolver.deleteRun(id: Long) {
    delete(
        ContentUris.withAppendedId(Uri.parse(METRICS_AUTHORITY), id.toLong()),
        null,
        null
    )
}


