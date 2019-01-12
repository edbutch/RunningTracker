package com.example.eddy.basetrackerpsyegb.DB

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.RunMetricsDAO

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
                    ).addMigrations(MIGRATION_1_2)
                        .build()
                }
            }
            return INSTANCE
        }

        fun  destroyDataBase () {
            INSTANCE = null
        }



        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // https://developer.android.com/reference/android/arch/persistence/room/ColumnInfo
                /*
                database.execSQL("ALTER TABLE pin "
                        + " ADD COLUMN is_location_accurate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE pin "
                        + " SET is_location_accurate = 0 WHERE latitude IS NULL")
                database.execSQL("UPDATE pin "
                        + " SET is_location_accurate = 1 WHERE latitude IS NOT NULL")
                        */

                database.execSQL("ALTER TABLE GPS"
                        + " ADD COLUMN longitude FLOAT(53)")


            }
        }
    }
}


