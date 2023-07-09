package com.example.eicproject

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.eicproject.database.PlantDao
import com.example.eicproject.database.PlantData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class MainActivityViewModel(private val plantDao: PlantDao) : ViewModel() {
    val plantData = plantDao.getSpecificPlantData(1)
    var plantImage : MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()

    fun addPlantData(plantData : PlantData) = viewModelScope.launch(Dispatchers.IO) {
        plantDao.insertPlantData(plantData)
        Log.i("test", plantData.toString())
    }

    fun setNewImage(bmp : Bitmap) = viewModelScope.launch(Dispatchers.Main) {
        plantImage.value = bmp
    }
}

class MainActivityViewModelFactory(val plantDao: PlantDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(plantDao) as T
        }
        throw IllegalArgumentException("no canot")
    }
}