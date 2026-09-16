package com.example.kotlintest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlintest.databinding.ItemDeviceBinding

class DeviceAdapter(
    private var devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit,
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    fun submitList(newDevices: List<Device>) {
        devices = newDevices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position], onDeviceClick)
    }

    override fun getItemCount(): Int = devices.size

    class DeviceViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device, onDeviceClick: (Device) -> Unit) {
            binding.textName.text = device.name
            binding.textIpAddress.text = device.ipAddress

            val statusColorRes = if (device.isOnline) R.color.device_status_online else R.color.device_status_offline
            binding.imageStatus.setColorFilter(ContextCompat.getColor(binding.root.context, statusColorRes))
            binding.imageStatus.setImageResource(android.R.drawable.presence_online)
            binding.imageStatus.contentDescription = binding.root.context.getString(
                if (device.isOnline) R.string.content_description_status_online else R.string.content_description_status_offline
            )

            binding.cardDevice.setOnClickListener { onDeviceClick(device) }
        }
    }
}
