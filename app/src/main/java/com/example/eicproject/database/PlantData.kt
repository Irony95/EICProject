package com.example.eicproject.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_information")
data class PlantData(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "plant_data_id")
    val id : Int,

    @ColumnInfo(name="plant_id")
    val plantId : Int,

    @ColumnInfo(name="timestamp")
    val timeStamp : Long,

    @ColumnInfo(name = "moisture")
    val moisture : Float
)