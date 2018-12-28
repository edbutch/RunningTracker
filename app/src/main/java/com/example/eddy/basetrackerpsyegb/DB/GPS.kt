package com.mdp.eddy.g53recipebook.DB

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics


//https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/Cheese.java

@Entity(tableName = "Foo",
    foreignKeys = [
        ForeignKey(entity = RunMetrics::class,
            parentColumns = ["id"],
            childColumns = ["someOtherCol"],
            onDelete = CASCADE)])
class GPS {

    companion object  {

        const val PARENTID = "id"
        const val TIME = "time"
        const val LAT = "lat"
        const val LONG = "lat"
        const val ELE = "ele"

    }



    var parentId : Int = 0
    var time: Long = 0L
    var lat: Double = 0.0
    var long: Double = 0.0
    var ele: Double = 0.0


    override fun toString(): String {
        return "GPS id: $parentId , Time: $time , Lat: $lat, Long: $long, Ele: $ele"
    }
}


