package com.example.kotlintest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.kotlintest.databinding.ActivityDeviceListBinding

class DeviceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceListBinding
    private lateinit var deviceListFragment: DeviceListFragment
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.deviceList) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            deviceListFragment = DeviceListFragment()
            settingsFragment = SettingsFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, settingsFragment, TAG_SETTINGS)
                .hide(settingsFragment)
                .add(R.id.fragment_container, deviceListFragment, TAG_DEVICES)
                .commit()
        } else {
            @Suppress("UNCHECKED_CAST")
            deviceListFragment = supportFragmentManager.findFragmentByTag(TAG_DEVICES) as DeviceListFragment
            @Suppress("UNCHECKED_CAST")
            settingsFragment = supportFragmentManager.findFragmentByTag(TAG_SETTINGS) as SettingsFragment
        }

        binding.bottomNav.selectedItemId = R.id.navigation_devices
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_devices -> {
                    showTab(show = deviceListFragment, hide = settingsFragment)
                    binding.toolbar.title = getString(R.string.title_devices)
                    true
                }
                R.id.navigation_settings -> {
                    showTab(show = settingsFragment, hide = deviceListFragment)
                    binding.toolbar.title = getString(R.string.title_settings)
                    true
                }
                else -> false
            }
        }
    }

    private fun showTab(show: Fragment, hide: Fragment) {
        supportFragmentManager.beginTransaction()
            .show(show)
            .hide(hide)
            .commit()
    }

    companion object {
        private const val TAG_DEVICES = "tag_device_list"
        private const val TAG_SETTINGS = "tag_settings"
    }
}
