package com.example.smarthomeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.SafetyCritical

@Composable
fun DeviceCard(
    device: Device,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOn = device.state == DeviceState.ON

    val (gradientColors, contentColor, switchCheckedColor) = when {
        // Smart Bulb / Lamp when ON → warm yellow
        isOn && (device.name.contains("Bulb", ignoreCase = true) ||
                device.name.contains("Lamp", ignoreCase = true)) -> {
            Triple(
                listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)),
                Color(0xFF5D4037),
                Color(0xFFFFB300)
            )
        }

        // Any other device ON → soft green
        isOn -> {
            Triple(
                listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)),
                Color(0xFF1B5E20),
                Color(0xFF43A047)
            )
        }

        // OFF
        device.state == DeviceState.OFF -> {
            Triple(
                listOf(Color(0xFFFAFAFA), Color(0xFFF5F5F5)),
                Color(0xFF424242),
                Color(0xFF9E9E9E)
            )
        }

        // ERROR
        device.state == DeviceState.ERROR -> {
            Triple(
                listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)),
                Color(0xFFB71C1C),
                Color(0xFFE53935)
            )
        }

        // DISCONNECTED
        else -> {
            Triple(
                listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                Color(0xFFE65100),
                Color(0xFFFF9800)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradientColors))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name.ifBlank { "Unnamed Device" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Type: ${device.type.ifBlank { "Unknown" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.75f)
                    )

                    if (device is SafetyCritical) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Max ON: ${device.maxOnDuration / 60} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.75f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = device.state.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isOn,
                    onCheckedChange = { onToggle(device.deviceID) },
                    enabled = device.state != DeviceState.ERROR &&
                            device.state != DeviceState.DISCONNECTED,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = switchCheckedColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD)
                    )
                )
            }
        }
    }
}