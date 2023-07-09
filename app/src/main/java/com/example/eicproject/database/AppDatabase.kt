package com.example.eicproject.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities=[PlantData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao() : PlantDao

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getInstance(context : Context) : AppDatabase{
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "plants_database"
                    ).build()
                }
                return instance
            }
        }
    }

}