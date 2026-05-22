package com.example.queues.api

import com.example.queues.dto.CreateEnterpriseDto
import com.example.queues.dto.CreateQueueDto
import com.example.queues.dto.EnterpriseDto
import com.example.queues.dto.QueueDto
import com.example.queues.dto.UpdateEnterpriseDto
import com.example.queues.dto.UpdateQueueDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface QueueApi {
    @GET("/api/queue")
    suspend fun getAllQueue(): List<QueueDto>
    @GET("/api/queue/{enterpriseId}")
    suspend fun getQueueByEntId(@Path("enterpriseId") id: Long): Response<QueueDto>
    @POST("/api/queue")
    suspend fun createQueue(@Body queue: CreateQueueDto): Response<QueueDto>
    @DELETE("/api/queue")
    suspend fun deleteAllQueue()
    @DELETE("/api/queue/{id}")
    suspend fun deleteQueueById(@Path("id") id: Long)
    @PUT("/api/queue/{id}")
    suspend fun updateQueue(@Path("id") id: Long, @Body queue: UpdateQueueDto )
}