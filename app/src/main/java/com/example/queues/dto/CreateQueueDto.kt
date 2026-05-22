package com.example.queues.dto

data class CreateQueueDto(
    var isEnabled: Boolean,
    var enterpriseId: Long,
    var averageServiceSeconds: Int,
)