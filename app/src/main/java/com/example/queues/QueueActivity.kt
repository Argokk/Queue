package com.example.queues

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.example.queues.api.ApiFactory
import com.example.queues.databinding.ActivityQueueBinding
import com.example.queues.dto.EnterpriseDto
import com.example.queues.dto.QueueDto
import com.example.queues.dto.QueueEntryDto

class QueueActivity : AppCompatActivity() {
    lateinit var binding: ActivityQueueBinding
    lateinit var enterprise: EnterpriseDto
    lateinit var queue : QueueDto
    //private val adapter = QueueEntryAdapter()
    lateinit var queueEntry: QueueEntryDto
    private val queueViewModel: QueueViewModel by viewModels()
    private val entViewModel : EnterprisesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding.backBut.setOnClickListener { finish() }
        entViewModel.loadEnterpriseById(intent.getLongExtra("ent_id", -1))


        entViewModel.enterprise.observe(this@QueueActivity) {
            enterprise = it
            binding.data = enterprise
            Glide.with(this).load(enterprise.url).into(binding.imageView)
            queueViewModel.getQueueById(enterprise.id)
        }


        queueViewModel.queue.observe(this@QueueActivity){
            queue = it
        }

        binding.newEntryButton.setOnClickListener {
            queueViewModel.createNewEntry(queue.id)
        }
        queueViewModel.entry.observe(this@QueueActivity){
            queueEntry = it
        }
    }
}