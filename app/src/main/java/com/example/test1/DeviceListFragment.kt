package com.example.test1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test1.databinding.FragmentDeviceListBinding
import com.google.android.material.snackbar.Snackbar

class DeviceListFragment : Fragment() {

    private var _binding: FragmentDeviceListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DeviceAdapter
    private var allDevices: List<Device> = emptyList()
    private var currentQuery: String = ""

    private val addDeviceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK &&
            result.data?.getBooleanExtra(AddDeviceActivity.EXTRA_ADDED, false) == true
        ) {
            refreshList()
            Snackbar.make(binding.root, R.string.text_device_added, Snackbar.LENGTH_SHORT).show()
        }
    }

    private val deviceDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK &&
            result.data?.getBooleanExtra(DeviceDetailActivity.EXTRA_DELETED, false) == true
        ) {
            refreshList()
            Snackbar.make(binding.root, R.string.text_device_deleted, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchDevices.isEnabled = false
        binding.searchDevices.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterDevices(newText.orEmpty())
                return true
            }
        })

        adapter = DeviceAdapter(emptyList()) { device ->
            val intent = Intent(requireContext(), DeviceDetailActivity::class.java).apply {
                putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, device.id)
            }
            deviceDetailLauncher.launch(intent)
        }
        binding.recyclerDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDevices.adapter = adapter

        binding.fabAddDevice.setOnClickListener {
            addDeviceLauncher.launch(Intent(requireContext(), AddDeviceActivity::class.java))
        }

        loadDevices()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDevices() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.recyclerDevices.visibility = View.GONE
        binding.textEmptyState.visibility = View.GONE

        DeviceRepository.loadDevices { devices ->
            if (_binding == null) return@loadDevices

            allDevices = devices
            adapter.submitList(devices)

            binding.progressLoading.visibility = View.GONE
            binding.recyclerDevices.visibility = View.VISIBLE
            binding.searchDevices.isEnabled = true
        }
    }

    private fun refreshList() {
        allDevices = DeviceCatalog.all
        filterDevices(currentQuery)
    }

    private fun filterDevices(query: String) {
        currentQuery = query
        val filtered = if (query.isBlank()) {
            allDevices
        } else {
            allDevices.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filtered)

        binding.textEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerDevices.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }
}
