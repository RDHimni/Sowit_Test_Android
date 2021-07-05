package ridaz.ksk.sowit_test_android.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ridaz.ksk.sowit_test_android.db.PolygonDao
import ridaz.ksk.sowit_test_android.model.MyPolygon
import javax.inject.Inject

class PolygonRepository @Inject constructor(private val polygonDao: PolygonDao){

    val readMyPolygonFromRoom: LiveData<List<MyPolygon>> = polygonDao.readPolygons()

    suspend fun insertPolygonInRoom(myPolygon: MyPolygon){
        polygonDao.insertPolygonInRoom(myPolygon)
    }

}