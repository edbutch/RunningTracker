package com.example.eddy.basetrackerpsyegb.DB

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity


class RunMetrics{

    companion object{
        const val ID ="id"
        const val START_TIME = "starttime"
        const val END_TIME = "starttime"
        const val RUN_SPEED = "runspeed"
    }

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var startTime : Long = 0L
    var endtTime : Long = 0L



}