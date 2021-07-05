package ridaz.ksk.sowit_test_android.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ridaz.ksk.sowit_test_android.model.MyPolygon


@Dao
interface PolygonDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolygonInRoom(myPolygon: MyPolygon)

    @Query("SELECT * FROM polygon_table ORDER BY id ASC")
    fun readPolygons(): LiveData<List<MyPolygon>>

}