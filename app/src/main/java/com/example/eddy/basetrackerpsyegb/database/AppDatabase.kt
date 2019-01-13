package com.example.eddy.basetrackerpsyegb.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context

@Database(entities = [GPS::class, RunMetrics::class], version = 2, exportSchema = false)


abstract class AppDatabase : RoomDatabase() {
    abstract fun gpsDao(): GPSDao
    abstract fun metricsDao(): RunMetricsDAO


    companion object {
        var INSTANCE: AppDatabase? = null

        val DB_NAME = "trackerDB"
        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null){
                synchronized(AppDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                        DB_NAME
                    ).build()
                }
            }
            return INSTANCE
        }

        fun  destroyDataBase () {
            INSTANCE = null
        }




    }
}


