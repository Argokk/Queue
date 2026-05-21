package com.example.queues

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    exportSchema = false,
    entities = [Enterprise::class],
    version = 1
)
abstract class DataBase: RoomDatabase() {
    abstract fun getEnterpriseDao(): EnterpriseDao
}