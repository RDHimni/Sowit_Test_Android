package ridaz.ksk.sowit_test_android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ridaz.ksk.sowit_test_android.converter.Converters
import ridaz.ksk.sowit_test_android.model.MyPolygon


@Database(entities = [MyPolygon::class], version = 1,exportSchema = false)
@TypeConverters(Converters::class)
abstract class PolygonDb : RoomDatabase() {

abstract fun polygonDao() : PolygonDao

}