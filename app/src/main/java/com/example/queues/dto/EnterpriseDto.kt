package com.example.queues.dto

data class EnterpriseDto(
    val id: Long,
    val locationX: Double,
    val locationY: Double,
    val name: String,
    val url: String,
    val queue: QueueDto?
)