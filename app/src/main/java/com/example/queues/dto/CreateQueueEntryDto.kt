package com.example.queues.dto

data class CreateQueueEntryDto(
    //val userId: Long,  когда будет авторизация
    var queueId: Long? = null,
)