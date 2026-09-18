package com.example.kotlintest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlintest.databinding.ActivityDeviceDetailBinding

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceDetailBinding
    private lateinit var device: Device

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deviceDetail.applySystemBarInsetsPadding()

        binding.toolbar.setNavigationOnClickListener { finish() }

        val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID)
        val loadedDevice = deviceId?.let { DeviceCatalog.findById(it) }

        if (loadedDevice == null) {
            finish()
            return
        }
        device = loadedDevice

        binding.toolbar.title = device.name
        binding.textName.text = device.name
        binding.textStatus.text = getString(
            if (device.isOnline) R.string.text_status_online else R.string.text_status_offline
        )
        binding.textIpAddress.text = device.ipAddress
        binding.textType.text = getString(R.string.text_type, device.type)

        binding.buttonDelete.setOnClickListener { confirmDelete() }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_message)
            .setPositiveButton(R.string.dialog_delete_positive) { _, _ -> deleteDevice() }
            .setNegativeButton(R.string.dialog_delete_negative, null)
            .show()
    }

    private fun deleteDevice() {
        DeviceCatalog.remove(device.id)
        setResult(RESULT_OK, Intent().putExtra(EXTRA_DELETED, true))
        finish()
    }

    companion object {
        const val EXTRA_DEVICE_ID = "extra_device_id"
        const val EXTRA_DELETED = "extra_deleted"
    }
}
