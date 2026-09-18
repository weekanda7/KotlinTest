package com.example.kotlintest

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlintest.databinding.ActivityAddDeviceBinding
import java.util.Calendar

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDeviceBinding
    private var installDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addDevice.applySystemBarInsetsPadding()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.dropdownType.setSimpleItems(DeviceCatalog.types.toTypedArray())
        binding.dropdownType.setText(DeviceCatalog.types.first(), false)

        binding.editInstallDate.setOnClickListener { showDatePicker() }
        binding.buttonSave.setOnClickListener { attemptSave() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                installDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                binding.editInstallDate.setText(installDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun attemptSave() {
        binding.inputLayoutName.error = null
        binding.inputLayoutIpAddress.error = null

        val name = binding.editName.text?.toString().orEmpty().trim()
        val ipAddress = binding.editIpAddress.text?.toString().orEmpty().trim()
        var hasError = false

        if (name.isBlank()) {
            binding.inputLayoutName.error = getString(R.string.error_name_required)
            hasError = true
        }
        if (ipAddress.isBlank()) {
            binding.inputLayoutIpAddress.error = getString(R.string.error_ip_required)
            hasError = true
        }
        if (hasError) return

        val device = Device(
            id = DeviceCatalog.nextId(),
            name = name,
            ipAddress = ipAddress,
            isOnline = binding.checkboxOnline.isChecked,
            type = binding.dropdownType.text.toString(),
            installDate = installDate,
        )
        DeviceCatalog.add(device)

        setResult(RESULT_OK, Intent().putExtra(EXTRA_ADDED, true))
        finish()
    }

    companion object {
        const val EXTRA_ADDED = "extra_added"
    }
}
