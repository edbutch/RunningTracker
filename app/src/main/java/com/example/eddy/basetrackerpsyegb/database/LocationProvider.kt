package com.example.eddy.basetrackerpsyegb.database

import android.arch.persistence.room.Room
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.PKEY
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.LONGITUDE
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.LATITUDE
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.PARENTID
import com.example.eddy.basetrackerpsyegb.database.GPS.Companion.TIME

class LocationProvider : ContentProvider() {


    companion object{



        const val AUTHORITY = "com.example.eddy.basetrackerpsyegb.database.contentprovider"
        const val PATH_GPS = "gpsdb"
        const val PATH_METRICS = "metricstable"
        const val UPDATE = "updatetable"
        const val PATH_TOTALTIME = "updatetimetable"
        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private const val GPSCODE = 1
        private const val GPSCODE_ID = 2
        private const val METCODE = 3
        private const val METCODE_ID = 4
        private const val UPDATE_DISTANCE = 5
        private const val UPDATE_DISANCE_ID= 6
        private const val UPDATE_TOTAL_TIME= 7
        private const val UPDATE_TOTAL_TIME_ID= 8



        init {
            sURIMatcher.addURI(
                AUTHORITY,
                PATH_GPS,
                GPSCODE
            )
            sURIMatcher.addURI(
                AUTHORITY, "$PATH_GPS/#",
                GPSCODE_ID
            )
            sURIMatcher.addURI(
                AUTHORITY, PATH_METRICS,
                METCODE)
            sURIMatcher.addURI(AUTHORITY, "$PATH_METRICS/#",
                METCODE_ID)

            sURIMatcher.addURI(
                AUTHORITY, UPDATE,
                UPDATE_DISTANCE)

            sURIMatcher.addURI(AUTHORITY, "$UPDATE/#",
                UPDATE_DISANCE_ID)

            sURIMatcher.addURI(
                AUTHORITY, PATH_TOTALTIME,
                UPDATE_TOTAL_TIME)

            sURIMatcher.addURI(AUTHORITY, "$PATH_TOTALTIME/#",
                UPDATE_TOTAL_TIME_ID)




        }

    }

    private lateinit var database: AppDatabase

    override fun onCreate(): Boolean {
        database = Room.databaseBuilder(context!!.applicationContext, AppDatabase::class.java,
            AppDatabase.DB_NAME
        )
            .build()


        return true
    }

    override fun getType(uri: Uri): String {
        return when (sURIMatcher.match(uri)) {
            GPSCODE -> "$AUTHORITY/$PATH_GPS"
            GPSCODE_ID -> "$AUTHORITY/$PATH_GPS"
            METCODE -> "$AUTHORITY/$PATH_METRICS"
            METCODE_ID -> "$AUTHORITY/$PATH_METRICS"
            else -> throw IllegalArgumentException("Illegal Argument URI: $uri")
        }
    }


    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        when(sURIMatcher.match(uri)){
            GPSCODE -> {
                return updateGPS(values)
            }
            METCODE->{
                return updateMetrics(values)
            }
            UPDATE_DISTANCE -> {
                return updateDistance(values)
            }
            UPDATE_TOTAL_TIME -> {
                return updateTotalTIme(values)
            }


            else ->  throw IllegalArgumentException("Unknown URI: $uri")
        }



    }


    private fun updateTotalTIme(values: ContentValues?): Int {
        val time = values?.get(RunMetrics.TOTAL_TIME) as Long
        var id = values[RunMetrics.ID] as Int
        return database.metricsDao().setTotalTIme(time, id)

    }

    private fun updateDistance(values: ContentValues?): Int {

        var distance = values?.get(RunMetrics.TOTAL_DISTANCE) as Float
        var id = values[RunMetrics.ID] as Int
        return database.metricsDao().updateDistance(distance, id)

    }

    private fun updateMetrics(values: ContentValues?): Int {
        val runMetrics = RunMetrics()

        runMetrics.endTime = values?.get(RunMetrics.END_TIME) as Long
        runMetrics.id = values[RunMetrics.ID] as Int
       return database.metricsDao().updateEndTime(runMetrics.endTime, runMetrics.id)
    }

    private fun updateGPS(values: ContentValues?): Int {

        var parentId = values?.get(PARENTID) as Int
        var pkey = values[PKEY] as Int
        var time = values[TIME] as Long
        var lat = values[LATITUDE] as Double
        var long = values[LONGITUDE] as Double
        var ele = values[GPS.ELE] as Double
        var speed = values[GPS.SPEED] as Float


        val gps = GPS(pkey, parentId, time, lat, long, ele, speed)
        val count = database.gpsDao().updateGPS(gps)
        return count
    }

    override fun insert(uri: Uri, values: ContentValues): Uri {

        when(sURIMatcher.match(uri)){
            GPSCODE -> {

                val parentId = values?.get(PARENTID) as Int
                val pKey = values[PKEY] as Int
                val time = values[TIME] as Long
                val lat = values[LATITUDE] as Double
                val long = values[LONGITUDE] as Double
                val ele = values[GPS.ELE] as Double
                val speed = values[GPS.SPEED] as Float
                val gps = GPS(pKey, parentId, time, lat, long, ele, speed)
                val retVal = database.gpsDao().insertGPS(gps)
                return ContentUris.withAppendedId(uri, retVal)
            }

            METCODE -> {
                val rm = RunMetrics()
                rm.startTime = values[RunMetrics.START_TIME] as Long
                rm.endTime = values[RunMetrics.END_TIME] as Long
                val retVal = database.metricsDao().insertRunMetrics(rm)
                return ContentUris.withAppendedId(uri, retVal)
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }


    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return when (sURIMatcher.match(uri)) {
            GPSCODE -> database.gpsDao().getGPSCursor()
            GPSCODE_ID -> database.gpsDao().getGPSByIDCursor(uri.lastPathSegment.toInt())
            METCODE -> database.metricsDao().getMetricsCursorOrderByTime()
            METCODE_ID -> database.metricsDao().getMetricsByID(uri.lastPathSegment.toInt())
            else -> throw IllegalArgumentException("Unknown  URI: $uri")
        }
    }


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        when (sURIMatcher.match(uri)) {
            METCODE_ID ->{
                val rm = RunMetrics()
                rm.id = ContentUris.parseId(uri).toInt()
                database.metricsDao().deleteRunMetrics(rm)
                database.gpsDao().deleteRuns(rm.id)
                return 1
            }
            else -> throw IllegalArgumentException("Unknown  URI: $uri")
        }
    }


}