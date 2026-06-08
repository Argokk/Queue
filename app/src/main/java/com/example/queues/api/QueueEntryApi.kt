package com.example.queues.api

import com.example.queues.dto.CreateQueueEntryDto
import com.example.queues.dto.QueueEntryDto
import com.example.queues.dto.UpdateQueueEntryDto
import retrofit2.Response
import retrofit2.http.*

interface QueueEntryApi {
    @GET("/api/queueEntry")
    suspend fun getAllQueueEntryes(): List<QueueEntryDto>
    @GET("/api/queueEntry/{id}")
    suspend fun getQueueEntryById(
        @Path("id") id: Long
    ): Response<QueueEntryDto>
    @POST("/api/queueEntry")
    suspend fun createQueueEntry(
        @Body queueEntry: CreateQueueEntryDto
    ): Response<QueueEntryDto>
    @PUT("/api/queueEntry/leave/{id}")
    suspend fun leaveQueue(
        @Path("id") id: Long
    ): Response<QueueEntryDto>
    @PUT("/api/queueEntry/call/{queueId}")
    suspend fun callNext(
        @Path("queueId") queueId: Long
    ): Response<QueueEntryDto>
    @DELETE("/api/queueEntry/{id}")
    suspend fun deleteQueueEntryById(
        @Path("id") id: Long
    ): Response<Unit>
    @DELETE("/api/queueEntry")
    suspend fun deleteAllQueueEntryes()
    @PUT("/api/queueEntry/{id}")
    suspend fun updateQueueEntry(
        @Path("id") id: Long,
        @Body queueEntry: UpdateQueueEntryDto
    ): Response<QueueEntryDto>
}