package com.example.test1

data class Device(
    val id: String,
    val name: String,
    val ipAddress: String,
    val isOnline: Boolean,
    val type: String = "Other",
    val installDate: String = "",
)

/**
 * In-memory only - stands in for a local DB / network cache. Because this is a
 * process-wide singleton, instrumented tests that mutate it (add/delete) must call
 * [reset] in @Before, otherwise state leaks between tests that share the same
 * instrumentation process (no AndroidTestOrchestrator here).
 */
object DeviceCatalog {

    private fun defaultDevices() = mutableListOf(
        Device("1", "server-01", "192.168.1.10", isOnline = true, type = "Server"),
        Device("2", "server-02", "192.168.1.11", isOnline = true, type = "Server"),
        Device("3", "router-main", "192.168.1.1", isOnline = true, type = "Router"),
        Device("4", "printer-office", "192.168.1.42", isOnline = false, type = "Printer"),
        Device("5", "nas-backup", "192.168.1.50", isOnline = false, type = "NAS"),
    )

    private var devices = defaultDevices()

    val all: List<Device> get() = devices.toList()

    val types: List<String> = listOf("Server", "Router", "Printer", "NAS", "Other")

    fun findById(id: String): Device? = devices.find { it.id == id }

    fun add(device: Device) {
        devices.add(device)
    }

    fun remove(id: String) {
        devices.removeAll { it.id == id }
    }

    fun nextId(): String {
        val maxId = devices.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0
        return (maxId + 1).toString()
    }

    fun reset() {
        devices = defaultDevices()
    }
}
