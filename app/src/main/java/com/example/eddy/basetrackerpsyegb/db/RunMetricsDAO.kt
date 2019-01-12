package com.example.eddy.basetrackerpsyegb.db

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

    @Query("UPDATE RunMetrics SET totalTime = :time WHERE id == :id")
    fun setTotalTIme(time: Long, id: Int) : Int

    @Query("UPDATE RunMetrics SET totalDistance = totalDistance + :distance WHERE id == :id")
    fun updateDistance(distance: Float, id: Int) : Int

    @Query("DELETE From RunMetrics WHERE id == :id")
    fun deleteRun(id: Int)


    @Query("SELECT * FROM RunMetrics WHERE startTime between :range1 and :range2")
    fun getRunInDateRange(range1: Long, range2: Long) : Cursor

}