package com.example.queues.dto

import java.time.Instant

data class QueueEntryDto(
    val id: Long,
    val userId: Long?,
    val sequenceNumber: Int,
    val status: String,
    val joinedAt: String,
    val calledAt: Instant? = null,
    val finishedAt: Instant? = null
)