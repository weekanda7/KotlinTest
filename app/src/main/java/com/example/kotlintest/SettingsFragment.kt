package com.example.kotlintest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.kotlintest.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                SettingsPreferences.setNotificationsEnabled(requireContext(), true)
            } else {
                binding.switchNotifications.isChecked = false
                Snackbar.make(
                    binding.root,
                    R.string.text_notification_permission_denied,
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchNotifications.isChecked = SettingsPreferences.isNotificationsEnabled(requireContext())
        binding.switchAutoRefresh.isChecked = SettingsPreferences.isAutoRefreshEnabled(requireContext())

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            onNotificationsToggled(isChecked)
        }
        binding.switchAutoRefresh.setOnCheckedChangeListener { _, isChecked ->
            SettingsPreferences.setAutoRefreshEnabled(requireContext(), isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onNotificationsToggled(enabled: Boolean) {
        if (!enabled) {
            SettingsPreferences.setNotificationsEnabled(requireContext(), false)
            return
        }

        val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED

        if (needsRuntimePermission) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            SettingsPreferences.setNotificationsEnabled(requireContext(), true)
        }
    }
}
