package com.mdp.eddy.g53recipebook.DB

import android.arch.persistence.room.*
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(entities = [GPS::class], version = 1)
@TypeConverters(Converters::class)


abstract class AppDatabase : RoomDatabase() {
    abstract fun gpsDao(): GPSDao


    companion object {
        var INSTANCE: AppDatabase? = null

        val DB_NAME = "gpsTrackerDB"
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


class Converters {
    @TypeConverter
    fun fromString(value: String): ArrayList<String> {
        val listType = object : TypeToken<ArrayList<String>>() {

        }.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}