package com.example.queues

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.queues.auth.TokenManager
import com.example.queues.databinding.FragmentSettingsBinding
import com.example.queues.ui.auth.AutorizationActivity

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.darkThemeSwitch.isChecked = AppSettings.isDarkTheme(requireContext())
        binding.notificationsSwitch.isChecked = AppSettings.notificationsEnabled(requireContext())
        binding.locationSwitch.isChecked = AppSettings.locationEnabled(requireContext())

        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppSettings.setBoolean(requireContext(), AppSettings.KEY_DARK_THEME, isChecked)
            AppSettings.applyTheme(requireContext())
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppSettings.setBoolean(requireContext(), AppSettings.KEY_NOTIFICATIONS_ENABLED, isChecked)
        }

        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppSettings.setBoolean(requireContext(), AppSettings.KEY_LOCATION_ENABLED, isChecked)
        }

        binding.logoutButton.setOnClickListener {
            TokenManager.clearToken()
            startActivity(Intent(requireContext(), AutorizationActivity::class.java))
            activity?.finish()
        }

        return binding.root
    }
}
