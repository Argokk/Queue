package com.example.queues.api

import com.example.queues.dto.CreateEnterpriseDto
import com.example.queues.dto.EnterpriseDto
import com.example.queues.dto.UpdateEnterpriseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EnterpriseApi {
    @GET("/api/enterprise")
    suspend fun getAllEnt(): List<EnterpriseDto>
    @GET("/api/enterprise/{id}")
    suspend fun getEntById(@Path("id") id: Long): Response<EnterpriseDto>
    @POST("/api/enterprise")
    suspend fun createEnt(@Body enterprise: CreateEnterpriseDto): Response<EnterpriseDto>
    @DELETE("/api/enterprise")
    suspend fun deleteAllEnt()
    @DELETE("/api/enterprise/{id}")
    suspend fun deleteEntById(@Path("id") id: Long)
    @PUT("/api/enterprise/{id}")
    suspend fun updateEnt(@Path("id") id: Long, @Body enterprise: UpdateEnterpriseDto )
}