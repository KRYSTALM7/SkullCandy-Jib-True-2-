package com.sujan.jibeq

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context

/**
 * Check if any connected Bluetooth audio device looks like "Jib".
 * This runs once when the screen starts so we show "Connected"
 * even if the earbuds were already paired before opening the app.
 */
fun getInitialJibState(context: Context): EarbudBatteryState {
    val manager = context.getSystemService(BluetoothManager::class.java)
        ?: return EarbudBatteryState()

    val profiles = listOf(
        BluetoothProfile.HEADSET,
        BluetoothProfile.A2DP
    )

    val connectedDevices = profiles.flatMap { profile ->
        try {
            manager.getConnectedDevices(profile)
        } catch (_: Exception) {
            emptyList()
        }
    }.distinctBy { it.address }

    val jib = connectedDevices.firstOrNull { device ->
        (device.name ?: "").contains("jib", ignoreCase = true)
    }

    return if (jib != null) {
        EarbudBatteryState(
            connected = true,
            deviceName = jib.name
        )
    } else {
        EarbudBatteryState()
    }
}
