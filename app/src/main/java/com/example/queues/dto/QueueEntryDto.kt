package com.example.queues.dto

data class QueueEntryDto(
    val id: Long,
    val userId: Long?,
    val sequenceNumber: Int,
    val status: String,
    val joinedAt: String?,
    val calledAt: String? = null,
    val finishedAt: String? = null
)