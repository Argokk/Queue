package com.example.queues

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.load.model.GlideUrl

@Entity(tableName = "enterprises")
data class Enterprise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val url: String,
    val locationX: Double,
    val locationY: Double
)
