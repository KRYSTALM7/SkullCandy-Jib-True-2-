package com.sujan.jibeq

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Raw strings so there are no unresolved Bluetooth constants.
private const val ACTION_BATTERY_LEVEL_CHANGED =
    "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
private const val EXTRA_BATTERY_LEVEL =
    "android.bluetooth.device.extra.BATTERY_LEVEL"

data class EarbudBatteryState(
    val connected: Boolean = false,
    val level: Int? = null,
    val deviceName: String? = null
)

@Composable
fun JibBatteryScreen() {
    val context = LocalContext.current
    var state by remember { mutableStateOf(EarbudBatteryState()) }

    // 1) On start, detect if Jib is already connected
    LaunchedEffect(Unit) {
        state = getInitialJibState(context)
    }

    // 2) Listen for battery / connect / disconnect broadcasts
    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(ACTION_BATTERY_LEVEL_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }

        val receiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent == null) return

                val device: BluetoothDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                device ?: return
                val name = device.name ?: ""
                val isJib = name.contains("jib", ignoreCase = true)
                if (!isJib) return

                when (intent.action) {
                    ACTION_BATTERY_LEVEL_CHANGED -> {
                        val lvl = intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1)
                        if (lvl in 0..100) {
                            state = state.copy(
                                connected = true,
                                level = lvl,
                                deviceName = name
                            )
                        }
                    }

                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        state = state.copy(
                            connected = true,
                            deviceName = name
                        )
                    }

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        state = EarbudBatteryState()
                    }
                }
            }
        }

        context.registerReceiver(receiver, filter)

        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    // UI ──────────────────────────────────────────────────────────────

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF050814) // dark background
    ) {
        val level = state.level ?: 0
        val lowBattery = state.level != null && level <= 20

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()                // moves away from camera / notch
                .verticalScroll(scrollState)        // safe on small screens
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Title
            Text(
                text = "Jib True 2",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF9EC5FF)
            )

            Text(
                text = if (state.connected)
                    "Connected to ${state.deviceName ?: "Jib True 2"}"
                else
                    "Connect your Jib True 2 to see battery level.",
                fontSize = 14.sp,
                color = Color(0xFF8D96B6)
            )

            // Earbuds image card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0B1020)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jib_true_2),
                    contentDescription = "Jib True 2",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Scan / connect button – opens system Bluetooth settings
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan / Connect via Bluetooth")
            }

            // Battery info card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF101626)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (state.connected) "Status: Connected" else "Status: Not connected",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Earbuds battery",
                        color = Color(0xFFBFC8E6),
                        fontSize = 13.sp
                    )

                    val batteryText = when {
                        state.level != null -> "${state.level}%"
                        state.connected -> "Charging / waiting for info"
                        else -> "--"
                    }

                    Text(
                        text = batteryText,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(
                        progress = (level / 100f).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        trackColor = Color(0xFF1A2235),
                        color = if (lowBattery) Color(0xFFFF6B6B) else Color(0xFF4DA3FF)
                    )

                    if (lowBattery) {
                        Text(
                            text = "Low battery! Charge your Jib True 2 soon.",
                            color = Color(0xFFFF8585),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "A low-battery warning appears here when it drops below 20%.",
                            color = Color(0xFF8D96B6),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
