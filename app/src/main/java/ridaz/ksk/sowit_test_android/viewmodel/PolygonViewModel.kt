package ridaz.ksk.sowit_test_android.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ridaz.ksk.sowit_test_android.model.MyPolygon
import ridaz.ksk.sowit_test_android.repository.PolygonRepository
import javax.inject.Inject

@HiltViewModel
class PolygonViewModel @Inject constructor(private val polygonRepository: PolygonRepository) : ViewModel(){

    val readMyPolygonFromRoom: LiveData<List<MyPolygon>>  = polygonRepository.readMyPolygonFromRoom





    fun insertPolygonInRoom(myPolygon: MyPolygon){

        Log.d("here", "insertPolygonInRoom: $myPolygon")
        viewModelScope.launch(Dispatchers.IO){
            polygonRepository.insertPolygonInRoom(myPolygon)
        }

    }

}