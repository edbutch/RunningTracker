package com.example.eddy.basetrackerpsyegb.DB

import android.arch.persistence.room.*
import android.database.Cursor

@Dao
interface RunMetricsDAO {

    @Insert
    fun insertRunMetrics(runMetrics: RunMetrics) : Long

    @Delete
    fun deleteRunMetrics(runMetrics: RunMetrics)

    @Query("SELECT * FROM RunMetrics ORDER BY startTime DESC")
    fun getRuns() : Cursor

    @Query("SELECT * FROM RunMetrics WHERE id = :id")
    fun getMetricsByID(id: Int): Cursor

    @Query("Select * FROM RunMetrics ORDER BY startTime DESC")
    fun getMetricsCursorOrderByTime(): Cursor


    @Query("SELECT * FROM RunMetrics WHERE startTime == :time OR endTime == :time")
    fun getMetricsByTime(time: Long): Cursor

    @Query("UPDATE RunMetrics SET endTime = :time WHERE id == :id")
    fun updateEndTime(time: Long, id: Int) : Int


    @Query("UPDATE RunMetrics SET totalDistance = totalDistance + :distance WHERE id == :id")
    fun updateDistance(distance: Long, id: Int) : Int

}