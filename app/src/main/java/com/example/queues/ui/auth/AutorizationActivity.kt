package com.example.queues.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.queues.MainActivity
import com.example.queues.auth.TokenManager
import com.example.queues.databinding.ActivityAutorizationBinding
import com.example.queues.viewmodel.AuthViewModel

class AutorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutorizationBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAutorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        if (!TokenManager.getToken().isNullOrBlank()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        if (savedInstanceState == null) {
            showLogin()
        }

        authViewModel.authSuccess.observe(this) { success ->
            if (success == true) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.findFragmentById(binding.authFragmentContainer.id) is RegisterFragment) {
                        showLogin()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    fun showLogin() {
        supportFragmentManager.beginTransaction()
            .replace(binding.authFragmentContainer.id, LoginFragment())
            .commit()
    }

    fun showRegister() {
        supportFragmentManager.beginTransaction()
            .replace(binding.authFragmentContainer.id, RegisterFragment())
            .commit()
    }

}
