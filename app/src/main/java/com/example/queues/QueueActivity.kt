package com.example.queues

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.example.queues.databinding.ActivityQueueBinding
import com.example.queues.dto.EnterpriseDto
import com.example.queues.dto.QueueDto

class QueueActivity : AppCompatActivity() {

    lateinit var binding: ActivityQueueBinding
    lateinit var enterprise: EnterpriseDto
    lateinit var queue: QueueDto

    private val queueEntryViewModel: QueueEntryViewModel by viewModels()
    private val entViewModel: EnterprisesViewModel by viewModels()

    private lateinit var locationManager: LocationManager

    private val REQ_LOC_CODE = 1001
    private var lastCalledEntryId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        locationManager = getSystemService(LocationManager::class.java)

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
        }

        queueEntryViewModel.serviceTimerText.observe(this) { text ->
            binding.serviceTimerText.text = text
        }

        queueEntryViewModel.currentEntry.observe(this) { entry ->
            when (entry?.status) {
                "WAITING" -> {
                    binding.newEntryButton.text = "Выйти из очереди"
                    Log.d("queue_status", "Пользователь ожидает")
                }

                "CALLED" -> {
                    binding.newEntryButton.text = "Завершить обслуживание"

                    if (lastCalledEntryId != entry.id) {
                        lastCalledEntryId = entry.id

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
                }

                "MISSED" -> {
                    binding.newEntryButton.text = "Встать в очередь"
                    lastCalledEntryId = null
                }

                "FINISHED" -> {
                    binding.newEntryButton.text = "Встать в очередь"
                    lastCalledEntryId = null
                }

                null -> {
                    binding.newEntryButton.text = "Встать в очередь"
                    lastCalledEntryId = null
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

    override fun onDestroy() {
        super.onDestroy()

        queueEntryViewModel.stopAutoRefresh()
        queueEntryViewModel.stopDistanceCheck()
    }
}
