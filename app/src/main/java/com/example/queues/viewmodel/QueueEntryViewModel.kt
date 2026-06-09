package com.example.queues.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queues.api.ApiFactory
import com.example.queues.dto.CreateQueueEntryDto
import com.example.queues.dto.EnterpriseDto
import com.example.queues.dto.QueueDto
import com.example.queues.dto.QueueEntryDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class QueueEntryViewModel : ViewModel() {

    val queue = MutableLiveData<QueueDto?>()
    val currentEntry = MutableLiveData<QueueEntryDto?>()

    val peopleCount = MutableLiveData<Int>()
    val myPosition = MutableLiveData<Int?>()

    val message = MutableLiveData<String>()
    val stateText = MutableLiveData<String>("Вы не в очереди")
    val serviceTimerText = MutableLiveData<String>("Ожидание вызова")

    private var currentEntryId: Long? = null

    private var autoRefreshJob: Job? = null
    private var serviceTimerJob: Job? = null
    private var distanceCheckJob: Job? = null
    private var finishServiceJob: Job? = null

    private var timerEntryId: Long? = null
    private var timerCalledAt: String? = null
    private var callNextJob: Job? = null

    private val maxDistanceMeters = 20f
    private val waitingStatus = "WAITING"
    private val calledStatus = "CALLED"

    fun loadQueueByEnterpriseId(enterpriseId: Long) {
        viewModelScope.launch {
            refreshQueue(enterpriseId)
        }
    }

    private suspend fun refreshQueue(enterpriseId: Long) {
        try {
            val response = ApiFactory.queueApi.getQueueByEntId(enterpriseId)
            val loadedQueue = response.body()

            if (!response.isSuccessful || loadedQueue == null) {
                queue.value = null
                peopleCount.value = 0
                message.value = "Не удалось загрузить очередь"
                return
            }

            queue.value = loadedQueue
            peopleCount.value = calculatePeopleCount(loadedQueue)

            updateCurrentEntryFromQueue(
                loadedQueue = loadedQueue,
                enterpriseId = enterpriseId
            )

            updateMyPositionAndCallIfReady(loadedQueue, enterpriseId)
        } catch (e: Exception) {
            e.printStackTrace()
            message.value = "Не удалось загрузить очередь"
        }
    }

    fun createEntry(
        queueId: Long,
        enterpriseId: Long
    ) {
        viewModelScope.launch {
            try {
                val response = ApiFactory.queueEntryApi.createQueueEntry(
                    CreateQueueEntryDto(queueId)
                )

                val createdEntry = response.body()

                Log.d("CREATE_ENTRY", "queueId = $queueId")
                Log.d("CREATE_ENTRY", "code = ${response.code()}")
                Log.d("CREATE_ENTRY", "isSuccessful = ${response.isSuccessful}")
                Log.d("CREATE_ENTRY", "body = $createdEntry")
                Log.d("CREATE_ENTRY", "errorBody = ${response.errorBody()?.string()}")

                if (response.isSuccessful && createdEntry != null) {
                    currentEntryId = createdEntry.id
                    currentEntry.value = createdEntry

                    stateText.value = "Вы в очереди"
                    serviceTimerText.value = "Ожидание вызова"
                    message.value = "Вы встали в очередь"

                    refreshQueue(enterpriseId)
                } else {
                    message.value = "Не удалось встать в очередь"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                message.value = "Ошибка при создании записи"
            }
        }
    }

    fun leaveQueue(enterpriseId: Long) {
        val entryId = currentEntryId
        val wasCalled = currentEntry.value?.status == calledStatus

        if (entryId == null) {
            message.value = "Вы не стоите в очереди"
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiFactory.queueEntryApi.leaveQueue(entryId)

                if (response.isSuccessful) {
                    currentEntry.value = response.body()
                    clearLocalEntry()

                    val text = if (wasCalled) {
                        serviceTimerText.value = "Обслуживание завершено"
                        "Обслуживание завершено"
                    } else {
                        serviceTimerText.value = "Ожидание вызова"
                        "Вы вышли из очереди"
                    }

                    stateText.value = text
                    message.value = text

                    refreshQueue(enterpriseId)
                } else {
                    message.value = "Не удалось выйти из очереди"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                message.value = "Ошибка при выходе из очереди"
            }
        }
    }

    fun deleteEntry(enterpriseId: Long) {
        val entryId = currentEntryId

        if (entryId == null) {
            message.value = "Вы не стоите в очереди"
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiFactory.queueEntryApi.deleteQueueEntryById(entryId)

                if (response.isSuccessful) {
                    currentEntry.value = null
                    clearLocalEntry()

                    stateText.value = "Вы не в очереди"
                    message.value = "Запись удалена"

                    refreshQueue(enterpriseId)
                } else {
                    message.value = "Не удалось удалить запись"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                message.value = "Ошибка при удалении записи"
            }
        }
    }


    fun startAutoRefresh(enterpriseId: Long) {
        if (autoRefreshJob != null) return

        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                refreshQueue(enterpriseId)
                delay(3000)
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun startDistanceCheck(
        enterpriseId: Long,
        enterprise: EnterpriseDto,
        locationProvider: () -> Location?
    ) {
        if (distanceCheckJob != null) return

        distanceCheckJob = viewModelScope.launch {
            while (isActive) {
                val entry = currentEntry.value

                if (entry != null && entry.status == calledStatus) {
                    val userLocation = locationProvider()

                    if (userLocation != null) {
                        val enterpriseLocation = Location("enterprise").apply {
                            latitude = enterprise.locationX
                            longitude = enterprise.locationY
                        }

                        val distance = userLocation.distanceTo(enterpriseLocation)

                        if (distance > maxDistanceMeters) {
                            finishServiceByLeave(
                                enterpriseId = enterpriseId,
                                finishMessage = "Обслуживание завершено: вы отошли слишком далеко"
                            )
                        }
                    }
                }

                delay(5000)
            }
        }
    }

    fun stopDistanceCheck() {
        distanceCheckJob?.cancel()
        distanceCheckJob = null
    }

    private fun updateCurrentEntryFromQueue(
        loadedQueue: QueueDto,
        enterpriseId: Long
    ) {
        val entryId = currentEntryId ?: return
        val entries = loadedQueue.entries ?: emptyList()

        val updatedEntry = entries.firstOrNull { it.id == entryId }

        if (updatedEntry != null) {
            currentEntry.value = updatedEntry

            when (updatedEntry.status) {
                waitingStatus -> {
                    stateText.value = "Вы в очереди"
                    serviceTimerText.value = "Ожидание вызова"
                    stopServiceTimer()
                }

                calledStatus -> {
                    stateText.value = "Ваша очередь подошла"

                    startServiceTimer(
                        enterpriseId = enterpriseId,
                        entry = updatedEntry,
                        queueDto = loadedQueue
                    )
                }

                "LEFT" -> {
                    stateText.value = "Вы вышли из очереди"
                    clearLocalEntry()
                }

                "MISSED" -> {
                    stateText.value = "Вы пропустили очередь"
                    clearLocalEntry()
                }

                "FINISHED" -> {
                    stateText.value = "Обслуживание завершено"
                    clearLocalEntry()
                }
            }
        } else {
            currentEntry.value = null
            stateText.value = "Вы не в очереди"
            serviceTimerText.value = "Ожидание вызова"
            clearLocalEntry()
        }
    }

    private fun updateMyPositionAndCallIfReady(
        loadedQueue: QueueDto?,
        enterpriseId: Long
    ) {
        val entryId = currentEntryId

        if (loadedQueue == null || entryId == null) {
            myPosition.value = null
            return
        }

        val waitingEntries = loadedQueue.entries
            ?.filter { it.status == waitingStatus }
            ?.sortedBy { it.sequenceNumber }
            ?: emptyList()

        val index = waitingEntries.indexOfFirst { it.id == entryId }
        val position = if (index != -1) index + 1 else null

        myPosition.value = position

        val entry = currentEntry.value
        val hasCalledEntry = loadedQueue.entries?.any { it.status == calledStatus } == true

        if (entry?.status == waitingStatus && position == 1 && !hasCalledEntry) {
            callNext(loadedQueue.id, enterpriseId, showError = false)
        }
    }

    private fun startServiceTimer(
        enterpriseId: Long,
        entry: QueueEntryDto,
        queueDto: QueueDto
    ) {
        val calledAtString = entry.calledAt ?: "local-${entry.id}"

        if (
            timerEntryId == entry.id &&
            timerCalledAt == calledAtString &&
            serviceTimerJob?.isActive == true
        ) {
            return
        }

        serviceTimerJob?.cancel()

        timerEntryId = entry.id
        timerCalledAt = calledAtString

        val calledAt = parseBackendInstant(entry.calledAt) ?: Instant.now()
        val serviceSeconds = queueDto.averageServiceSeconds.toLong().coerceAtLeast(1)

        serviceTimerJob = viewModelScope.launch {
            while (isActive) {
                val now = Instant.now()
                val finishTime = calledAt.plusSeconds(serviceSeconds)

                val secondsLeft = Duration.between(now, finishTime).seconds

                if (secondsLeft <= 0) {
                    serviceTimerText.value = "Время обслуживания истекло"

                    finishServiceByLeave(
                        enterpriseId = enterpriseId,
                        finishMessage = "Обслуживание завершено: время истекло"
                    )

                    break
                } else {
                    val minutes = secondsLeft / 60
                    val seconds = secondsLeft % 60

                    serviceTimerText.value =
                        "Осталось: ${minutes}:${seconds.toString().padStart(2, '0')}"
                }

                delay(1000)
            }
        }
    }

    private fun stopServiceTimer() {
        serviceTimerJob?.cancel()
        serviceTimerJob = null
        timerEntryId = null
        timerCalledAt = null
    }

    private fun finishServiceByLeave(
        enterpriseId: Long,
        finishMessage: String
    ) {
        if (finishServiceJob?.isActive == true) return

        val entryId = currentEntryId ?: return

        finishServiceJob = viewModelScope.launch {
            try {
                val response = ApiFactory.queueEntryApi.leaveQueue(entryId)

                if (response.isSuccessful) {
                    currentEntry.value = response.body()

                    clearLocalEntry()

                    stateText.value = finishMessage
                    serviceTimerText.value = "Обслуживание завершено"
                    message.value = finishMessage

                    refreshQueue(enterpriseId)
                } else {
                    message.value = "Не удалось завершить обслуживание"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                message.value = "Ошибка при завершении обслуживания"
            }
        }
    }

    private fun clearLocalEntry() {
        currentEntryId = null
        myPosition.value = null
        stopServiceTimer()
    }

    private fun calculatePeopleCount(queueDto: QueueDto): Int {
        return queueDto.peopleCount
            ?: queueDto.entries?.count { it.status == waitingStatus }
            ?: 0
    }

    private fun callNext(
        queueId: Long,
        enterpriseId: Long,
        showError: Boolean
    ) {
        if (callNextJob?.isActive == true) return

        callNextJob = viewModelScope.launch {
            try {
                val response = ApiFactory.queueEntryApi.callNext(queueId)

                if (response.isSuccessful) {
                    response.body()?.let { calledEntry ->
                        if (calledEntry.id == currentEntryId) {
                            currentEntry.value = calledEntry
                            stateText.value = "Ваша очередь подошла"
                        }
                    }

                    refreshQueue(enterpriseId)
                } else if (showError) {
                    message.value = "Некого вызвать"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                if (showError) {
                    message.value = "Ошибка при вызове следующего"
                }
            }
        }
    }

    private fun parseBackendInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) return null

        return runCatching { Instant.parse(value) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            }.getOrNull()
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
        stopDistanceCheck()
        stopServiceTimer()
    }
}