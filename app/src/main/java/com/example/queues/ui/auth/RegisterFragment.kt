package com.example.queues.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.queues.databinding.FragmentRegisterBinding
import com.example.queues.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.registerButton.setOnClickListener {
            authViewModel.register(
                username = binding.registerLoginEditText.text.toString(),
                password = binding.registerPasswordEditText.text.toString(),
                repeatPassword = binding.repeatPasswordEditText.text.toString()
            )
        }

        binding.loginTextButton.setOnClickListener {
            (activity as? AutorizationActivity)?.showLogin()
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.registerButton.isEnabled = !isLoading
        }

        authViewModel.message.observe(viewLifecycleOwner) { message ->
            binding.registerErrorText.text = message
            binding.registerErrorText.isVisible = !message.isNullOrBlank()
        }

        return binding.root
    }
}
