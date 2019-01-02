package com.example.eddy.basetrackerpsyegb.DB


import android.arch.persistence.room.*
import android.database.Cursor


@Dao
interface   GPSDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertGPS(GPS: GPS) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGPS(gps: GPS) : Long

    @Delete
    fun deleteGPS(gps: GPS)

    @Update
    fun updateGPS(gps: GPS): Int

//    @Query("SELECT * FROM GPS ORDER BY timestamp DESC")
    @Query("Select * FROM GPS ORDER BY timestamp DESC")
    fun getGPSCursor(): Cursor

    @Query("SELECT * From GPS WHERE parentId == :id ")
    fun getGPSByIDCursor(id: Int): Cursor

    /* @Query("SELECT * FROM RunMetrics WHERE id = :id")
    fun getMetricsByID(id: Int): Cursor

*/




}



