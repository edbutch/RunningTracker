package com.example.eddy.basetrackerpsyegb.database

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri

const val METRICS_AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.database.contentprovider/metricstable"
const val UPDATE_METRICS_AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.database.contentprovider/updatetable"
const val UPDATE_TOTAL_TIME_AUTHORITY = "content://com.example.eddy.basetrackerpsyegb.database.contentprovider/updatetimetable"


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

    }
    cursor.close()
    return runList
}



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
    return metrics.id


}


fun ContentResolver.endMetrics(time: Long, id: Int): RunMetrics {
    val cv = ContentValues()
    cv.put(RunMetrics.ID, id)
    cv.put(RunMetrics.END_TIME, time)

    val uriRet = update(Uri.parse(METRICS_AUTHORITY), cv, null, null)

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


