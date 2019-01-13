package com.example.eddy.basetrackerpsyegb.database


import android.arch.persistence.room.*
import android.database.Cursor


@Dao
interface   GPSDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGPS(gps: GPS) : Long

    @Delete
    fun deleteGPS(gps: GPS)

    @Update
    fun updateGPS(gps: GPS): Int

    @Query("Select * FROM GPS ORDER BY timestamp DESC")
    fun getGPSCursor(): Cursor

    @Query("SELECT * From GPS WHERE parentId == :id ")
    fun getGPSByIDCursor(id: Int): Cursor

    @Query("DELETE From GPS WHERE parentId == :id")
    fun deleteRuns(id: Int)






}



