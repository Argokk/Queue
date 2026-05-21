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
    var entry = MutableLiveData<QueueEntryDto>()
    var queue = MutableLiveData<QueueDto>()
    fun createNewEntry(queueId: Long){
        viewModelScope.launch {
            entry.value = ApiFactory.queueEntryApi.createQueueEntry(CreateQueueEntryDto(queueId)).body()
        }
    }
    fun getQueueById(entId: Long){
        viewModelScope.launch {
            queue.value = ApiFactory.queueApi.getQueueByEntId(entId).body()
        }
    }
}