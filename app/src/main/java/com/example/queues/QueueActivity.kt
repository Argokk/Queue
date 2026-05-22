package com.example.queues

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.example.queues.databinding.ActivityQueueBinding
import com.example.queues.dto.EnterpriseDto
import com.example.queues.dto.QueueDto

class QueueActivity : AppCompatActivity() {
    lateinit var binding: ActivityQueueBinding
    lateinit var enterprise: EnterpriseDto
    lateinit var queue: QueueDto
    private val queueViewModel: QueueViewModel by viewModels()
    private val entViewModel: EnterprisesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding.backBut.setOnClickListener { finish() }
        val entId = intent.getLongExtra("ent_id", -1)
        if(entId != -1L){
            entViewModel.loadEnterpriseById(entId)
        } else {
            entViewModel.loadEnterprises()
        }
        queueViewModel.people_count.observe(this@QueueActivity){
            binding.queueNumber.text = "Очередь: ${it}"
        }
        entViewModel.enterprise.observe(this) { ent ->
            ent?.let {
                enterprise = it
                binding.data = it
                Glide.with(this).load(it.url).into(binding.imageView)
                it.queue?.let { q -> queueViewModel.getQueueById(q.id) }
            }
        }
        queueViewModel.myPosition.observe(this) {
            binding.myNumber.text = it?.let { pos -> "Ваш номер: $pos" } ?: "Вы не в очереди"
        }
        queueViewModel.queue.observe(this) { q ->
            q?.let {
                queue = it
                Log.d("loggg", "Hello")
                queueViewModel.startAutoRefresh(queue.id)
            }
        }

        binding.newEntryButton.setOnClickListener {
            if(::queue.isInitialized){
                queueViewModel.createNewEntry(queue.id)
            }
        }
    }
}