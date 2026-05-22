package com.example.queues.api

import com.example.queues.dto.CreateQueueDto
import com.example.queues.dto.CreateQueueEntryDto
import com.example.queues.dto.QueueDto
import com.example.queues.dto.QueueEntryDto
import com.example.queues.dto.UpdateQueueDto
import com.example.queues.dto.UpdateQueueEntryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface QueueEntryApi {
    @GET("/api/queueEntry")
    suspend fun getAllQueueEntryes(): List<QueueEntryDto>
    @GET("/api/queueEntry/{id}")
    suspend fun getQueueEntryById(@Path("id") id: Long): Response<QueueEntryDto>
    @POST("/api/queueEntry")
    suspend fun createQueueEntry(@Body queueEntry: CreateQueueEntryDto): Response<QueueEntryDto>
    @DELETE("/api/queueEntry")
    suspend fun deleteAllQueueEntryes()
    @DELETE("/api/queueEntry/{id}")
    suspend fun deleteQueueEntryById(@Path("id") id: Long)
    @PUT("/api/queueEntry/{id}")
    suspend fun updateQueueEntry(@Path("id") id: Long, @Body queueEntry: UpdateQueueEntryDto)
}