package com.mdp.eddy.g53recipebook.DB

import android.arch.persistence.room.Room
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class GPSProvider : ContentProvider() {

    //I'd like to attribute this beautiful code to the Recipe Book Coursework i wrote

    companion object{

        const val AUTHORITY = "com.example.eddy.basetrackerpsyegb.DB.contentprovider"
        const val PATH_GPS = "gpsDB"
        val DB_NAME = "gpsDB"
        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private const val GPSCODE = 1
        private const val GPSCODE_ID = 2

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
        }

    }

    private lateinit var database: AppDatabase

    override fun onCreate(): Boolean {
        database = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
            DB_NAME
        )
            .build()
        return true
    }

    override fun getType(uri: Uri): String {
        return when (sURIMatcher.match(uri)) {
            GPSCODE -> "$AUTHORITY/$PATH_GPS"
            GPSCODE_ID -> "$AUTHORITY/$PATH_GPS"
            else -> throw IllegalArgumentException("Illegal Argument URI: $uri")
        }
    }


    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        if (sURIMatcher.match(uri) == GPSCODE) {

            val gps = GPS()
            gps.id = values?.get(GPS.ID) as Int
            gps.lat = values[GPS.LAT] as Double
            gps.long = values[GPS.LONG] as Double
            gps.ele = values[GPS.ELE] as Double
            gps.time = values[GPS.TIME] as Long


            val count = database.gpsDao().updateGPS(gps)
            return count
        } else {
            throw IllegalArgumentException("Unknown URI: $uri")
        }

    }

    override fun insert(uri: Uri, values: ContentValues): Uri {

        if (sURIMatcher.match(uri) == GPSCODE) {
            val gps = GPS()
            gps.id = values[GPS.ID] as Int
            gps.lat = values[GPS.LAT] as Double
            gps.long = values[GPS.LONG] as Double
            gps.ele = values[GPS.ELE] as Double
            gps.time = values[GPS.TIME] as Long

            val retVal = database.gpsDao().insertGPS(gps)
            return ContentUris.withAppendedId(uri, retVal)

        } else {
            throw IllegalArgumentException("Unknown URI: $uri")
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
            else -> throw IllegalArgumentException("Unknown  URI: $uri")
        }
    }


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        when (sURIMatcher.match(uri)) {
            GPSCODE_ID -> {

                val g = GPS()
                g.id = ContentUris.parseId(uri).toInt()
                database.gpsDao().deleteGPS(g)
                return 1
            }
            else -> throw IllegalArgumentException("Unknown  URI: $uri")
        }
    }


}