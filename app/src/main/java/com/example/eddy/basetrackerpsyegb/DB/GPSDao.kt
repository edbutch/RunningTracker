package com.mdp.eddy.g53recipebook.DB

import android.arch.persistence.room.*
import android.database.Cursor


@Dao
interface GPSDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertGPS(GPS: GPS) : Long

    @Insert
    fun insertGPS(GPS: GPS) : Long

    @Delete
    fun deleteGPS(gps: GPS)

    @Update
    fun updateGPS(gps: GPS): Int

    @Query("SELECT * FROM GPS ORDER BY time DESC")
    fun getGPSCursor(): Cursor

    @Query("SELECT * From GPS WHERE id == :id")
    fun getGPSByIDCursor(id: Int): Cursor




}



