package com.example.queues.dto

data class UpdateQueueDto(
    var isEnabled: Boolean,
    var averageServiceSeconds: Int
)
