package com.example.queues

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queues.api.ApiFactory
import com.example.queues.dto.CreateQueueEntryDto
import com.example.queues.dto.QueueDto
import com.example.queues.dto.QueueEntryDto
import kotlinx.coroutines.launch

class QueueViewModel: ViewModel() {
    val entry = MutableLiveData<QueueEntryDto>()
    val queue = MutableLiveData<QueueDto>()

    fun getQueueById(queueId: Long){
        viewModelScope.launch {
            val response = ApiFactory.queueApi.getQueueByEntId(queueId)
            queue.value = response.body()
        }
    }

    fun createNewEntry(queueId: Long){
        viewModelScope.launch {
            val response = ApiFactory.queueEntryApi.createQueueEntry(CreateQueueEntryDto(queueId))
            entry.value = response.body()
            getQueueById(queueId) // сразу обновляем очередь
        }
    }
}