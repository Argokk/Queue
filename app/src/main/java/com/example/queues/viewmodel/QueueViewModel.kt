package com.example.queues.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queues.api.ApiFactory
import com.example.queues.dto.CreateQueueEntryDto
import com.example.queues.dto.QueueDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QueueViewModel: ViewModel() {

    val queue = MutableLiveData<QueueDto>()
    val people_count = MutableLiveData<Int>()
    val myPosition = MutableLiveData<Int>()

    fun getQueueById(queueId: Long, myUserId: Long? = null){
        viewModelScope.launch {
            val response = ApiFactory.queueApi.getQueueByEntId(queueId)
            response.body()?.let { q ->
                queue.value = q
                people_count.value = q.entries?.size

                myUserId?.let { userId ->
                    val position = q.entries?.indexOfFirst { it.userId == userId }?:0
                    if(position != -1){
                        myPosition.value = position + 1
                    } else {
                        myPosition.value = 0
                    }
                }
            }
        }
    }

    fun startAutoRefresh(queueId: Long, myUserId: Long? = null){
        viewModelScope.launch {
            while(true){
                getQueueById(queueId, myUserId)
                delay(2000)
            }
        }
    }

    fun createNewEntry(queueId: Long, myUserId: Long? = 0){
        viewModelScope.launch {
            val response = ApiFactory.queueEntryApi.createQueueEntry(CreateQueueEntryDto(queueId))
            if(response.isSuccessful){
                getQueueById(queueId, myUserId)
            }
        }
    }
}