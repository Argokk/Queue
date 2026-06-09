package com.example.queues

import android.app.Application
import com.example.queues.auth.TokenManager

class QueuesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
