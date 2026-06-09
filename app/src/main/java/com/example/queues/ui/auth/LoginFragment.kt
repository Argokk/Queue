package com.example.queues.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.queues.databinding.FragmentLoginFragmentBinding
import com.example.queues.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginFragmentBinding

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginFragmentBinding.inflate(inflater, container, false)

        binding.loginButton.setOnClickListener {
            authViewModel.login(
                username = binding.loginEditText.text.toString().trim(),
                password = binding.passwordEditText.text.toString()
            )
        }

        binding.registerTextButton.setOnClickListener {
            (activity as? AutorizationActivity)?.showRegister()
        }

        authViewModel.message.observe(viewLifecycleOwner) { message ->
            binding.authErrorText.text = message
            binding.authErrorText.isVisible = !message.isNullOrBlank()
        }

        return binding.root
    }
}
