package com.example.queues.dto

data class QueueDto(
    val id: Long,
    val isEnabled: Boolean,
    val averageServiceSeconds: Int,
    val peopleCount: Int?,
    val entries: List<QueueEntryDto>?,
    val enterpriseId: Long?
)