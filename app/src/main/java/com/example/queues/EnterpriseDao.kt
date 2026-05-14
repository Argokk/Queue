package com.example.queues

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EnterpriseDao{
    @Query("SELECT * FROM enterprises")
    suspend fun getAllEnterprises(): MutableList<Enterprise>

    @Query("DELETE  FROM enterprises WHERE id = :id")
    suspend fun deleteEnterprise(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEnterprise(enterprise: Enterprise)

    @Query("SELECT * FROM enterprises WHERE id = :id")
    suspend fun getEnterprise(id: Int): Enterprise
}