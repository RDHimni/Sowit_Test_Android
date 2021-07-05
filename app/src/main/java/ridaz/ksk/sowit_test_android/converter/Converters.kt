package ridaz.ksk.sowit_test_android.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng
import ridaz.ksk.sowit_test_android.model.Points
import java.lang.reflect.Type


class Converters {

        @TypeConverter
        fun fromListPointsToString(points: Points): String =
            Gson().toJson(points)



        @TypeConverter
        fun fromStringToPoints(stringPoints: String): Points =
            Gson().fromJson(stringPoints, Points::class.java)


}