package com.example.queues

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.example.queues.databinding.ActivityQueueBinding
import com.example.queues.dto.EnterpriseDto
import com.example.queues.dto.QueueDto
import com.example.queues.viewmodel.EnterprisesViewModel
import com.example.queues.viewmodel.QueueEntryViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class QueueActivity : AppCompatActivity() {

    lateinit var binding: ActivityQueueBinding
    lateinit var enterprise: EnterpriseDto
    lateinit var queue: QueueDto

    private val queueEntryViewModel: QueueEntryViewModel by viewModels()
    private val entViewModel: EnterprisesViewModel by viewModels()

    private lateinit var locationManager: LocationManager

    private val REQ_LOC_CODE = 1001
    private val queueChannelId = "queue_notifications"
    private var lastCalledEntryId: Long? = null
    private var lastMinuteNotificationEntryId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        locationManager = getSystemService(LocationManager::class.java)

        createQueueNotificationChannel()
        requestLocationPermission()

        binding.backBut.setOnClickListener {
            finish()
        }

        val entId = intent.getLongExtra("ent_id", -1)

        if (entId != -1L) {
            entViewModel.loadEnterpriseById(entId)
        } else {
            entViewModel.loadEnterprises()
        }

        entViewModel.enterprise.observe(this) { ent ->
            ent?.let {
                enterprise = it
                binding.data = it

                Glide.with(this)
                    .load(it.url)
                    .into(binding.imageView)

                queueEntryViewModel.loadQueueByEnterpriseId(it.id)

                queueEntryViewModel.startAutoRefresh(it.id)

                queueEntryViewModel.startDistanceCheck(
                    enterpriseId = it.id,
                    enterprise = it,
                    locationProvider = { getLastLocation() }
                )
            }
        }

        queueEntryViewModel.queue.observe(this) { q ->
            q?.let {
                queue = it
                Log.d("queue_log", "queue loaded: ${it.id}")
                checkOneMinuteNotification()
            }
        }

        queueEntryViewModel.peopleCount.observe(this) { count ->
            binding.queueNumber.text = "Очередь: $count"
        }

        queueEntryViewModel.stateText.observe(this) { text ->
            binding.myNumber.text = text
        }

        queueEntryViewModel.myPosition.observe(this) { position ->
            if (position != null) {
                binding.myNumber.text = "Ваш номер: $position"
            }
            checkOneMinuteNotification()
        }

        queueEntryViewModel.serviceTimerText.observe(this) { text ->
            binding.serviceTimerText.text = text
        }

        queueEntryViewModel.currentEntry.observe(this) { entry ->
            when (entry?.status) {
                "WAITING" -> {
                    binding.newEntryButton.text = "Выйти из очереди"
                    Log.d("queue_status", "Пользователь ожидает")
                    checkOneMinuteNotification()
                }

                "CALLED" -> {
                    binding.newEntryButton.text = "Завершить обслуживание"

                    if (lastCalledEntryId != entry.id) {
                        lastCalledEntryId = entry.id
                        lastMinuteNotificationEntryId = null

                        Toast.makeText(
                            this,
                            "Ваша очередь подошла",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                "LEFT" -> {
                    binding.newEntryButton.text = "Встать в очередь"
                    lastCalledEntryId = null
                    lastMinuteNotificationEntryId = null
                }

                "MISSED" -> {
                    binding.newEntryButton.text = "Встать в очередь"
                    lastCalledEntryId = null
                    lastMinuteNotificationEntryId = null
                }

                "FINISHED" -> {
                    binding.newEntryButton.text = "Встать в очередь"
                    lastCalledEntryId = null
                    lastMinuteNotificationEntryId = null
                }

                null -> {
                    binding.newEntryButton.text = "Встать в очередь"
                    lastCalledEntryId = null
                    lastMinuteNotificationEntryId = null
                }
            }
        }

        queueEntryViewModel.message.observe(this) { text ->
            if (!text.isNullOrBlank()) {
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        }

        binding.newEntryButton.setOnClickListener {
            if (!::queue.isInitialized || !::enterprise.isInitialized) {
                Toast.makeText(
                    this,
                    "Очередь ещё не загружена",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val entry = queueEntryViewModel.currentEntry.value

            when (entry?.status) {
                null, "LEFT", "MISSED", "FINISHED" -> {
                    queueEntryViewModel.createEntry(
                        queueId = queue.id,
                        enterpriseId = enterprise.id
                    )
                }

                "WAITING" -> {
                    queueEntryViewModel.leaveQueue(
                        enterpriseId = enterprise.id
                    )
                }

                "CALLED" -> {
                    queueEntryViewModel.leaveQueue(
                        enterpriseId = enterprise.id
                    )
                }
            }
        }
    }

    private fun requestLocationPermission() {
        val fineGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQ_LOC_CODE
            )
        }
    }

    private fun getLastLocation(): Location? {
        val fineGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            return null
        }

        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkOneMinuteNotification() {
        if (!::queue.isInitialized || !::enterprise.isInitialized || !AppSettings.notificationsEnabled(this)) {
            return
        }

        val entry = queueEntryViewModel.currentEntry.value ?: return
        val position = queueEntryViewModel.myPosition.value ?: return

        if (entry.status != "WAITING" || lastMinuteNotificationEntryId == entry.id) {
            return
        }

        val secondsToTurn = calculateSecondsToTurn(position)

        if (secondsToTurn in 1..60) {
            lastMinuteNotificationEntryId = entry.id
            showQueueNotification()
        }
    }

    private fun calculateSecondsToTurn(position: Int): Long {
        val serviceSeconds = queue.averageServiceSeconds.toLong().coerceAtLeast(1)
        val calledEntry = queue.entries?.firstOrNull { it.status == "CALLED" }

        val calledEntrySecondsLeft = calledEntry?.let { entry ->
            val calledAt = parseBackendInstant(entry.calledAt) ?: return@let serviceSeconds
            val finishTime = calledAt.plusSeconds(serviceSeconds)

            Duration.between(Instant.now(), finishTime).seconds.coerceAtLeast(0)
        } ?: 0

        return calledEntrySecondsLeft + ((position - 1).coerceAtLeast(0).toLong() * serviceSeconds)
    }

    private fun showQueueNotification() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(this, QueueActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (::enterprise.isInitialized) {
                putExtra("ent_id", enterprise.id)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, queueChannelId)
            .setSmallIcon(R.drawable.ic_main)
            .setContentTitle("Скоро ваша очередь")
            .setContentText("До вашего вызова осталось примерно 1 минута")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(enterprise.id.toInt(), notification)
    }

    private fun createQueueNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            queueChannelId,
            "Уведомления очереди",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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

    override fun onDestroy() {
        super.onDestroy()

        queueEntryViewModel.stopAutoRefresh()
        queueEntryViewModel.stopDistanceCheck()
    }
}
