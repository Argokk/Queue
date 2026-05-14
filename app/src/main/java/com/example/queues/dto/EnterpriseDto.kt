package com.example.queues.dto
data class EnterpriseDto(
    val id: Long,
    val name: String,
    val url: String,
    val locationX: Double,
    val locationY: Double,
    var queueId: Long
)