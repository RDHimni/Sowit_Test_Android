package ridaz.ksk.sowit_test_android.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.mapbox.mapboxsdk.geometry.LatLng

@Entity(tableName = "polygon_table")
data class MyPolygon(
 val name: String,
 val points: Points
){
 @PrimaryKey(autoGenerate = true)
 var id: Int =0
}

data class Points(
 val points: ArrayList<LatLng>
)
