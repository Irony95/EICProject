package com.example.eicproject.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlantDao {
    @Insert
    fun insertPlantData(data : PlantData)

    @Query("SELECT * FROM plant_information WHERE plant_id= :plantId")
    fun getSpecificPlantData(plantId : Int) : LiveData<List<PlantData>>
}