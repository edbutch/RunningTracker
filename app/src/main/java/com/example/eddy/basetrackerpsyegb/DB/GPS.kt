package com.example.eddy.basetrackerpsyegb.DB

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


//https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/Cheese.java
@Entity
data class GPS (@PrimaryKey(autoGenerate = true) var pKey : Int,
                @ColumnInfo(name = "parentId") var parentId : Int,
                @ColumnInfo(name = "timestamp") var timestamp : Long,
                @ColumnInfo(name = "latitude") var latitude : Double,
                @ColumnInfo(name = "longitude") var longitude : Double){


    companion object{
        const val PARENTID = "parentId"
        const val PKEY = "pKey"
        const val TIME = "timestamp"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        //        const val ELE = "elevation"

    }


    constructor():this(0,0,0L,0.0,0.0)

    override fun toString(): String {
        return "GPS(pKey=$pKey, parentId=$parentId, timestamp=$timestamp, latitude=$latitude, longitude=$longitude)"
    }
}










