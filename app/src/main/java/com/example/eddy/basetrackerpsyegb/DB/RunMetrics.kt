package com.example.eddy.basetrackerpsyegb.DB

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
data class RunMetrics(@PrimaryKey(autoGenerate = true) var id : Int,
                      @ColumnInfo(name = "startTime") var startTime : Long,
                      @ColumnInfo(name = "endTime") var endTime : Long,
                      @ColumnInfo(name = "totalDistance") var totalDistance :Float,
                      @ColumnInfo(name = "totalTime") var totalTime: String){
    companion object{
        const val ID ="id"
        const val START_TIME = "startTime"
        const val END_TIME = "endTime"
        const val TOTAL_DISTANCE = "totalDistance"
        const val TOTAL_TIME = "totalDistance"
    }



    constructor():this(id = 0 , startTime = 0L, endTime = 0L, totalDistance = 0F,totalTime =  "00:00")

    override fun toString(): String {
        return "RunMetrics(id=$id, startTime=$startTime, endTime=$endTime, totalDistance=$totalDistance totalTime $totalTime)"
    }


}

