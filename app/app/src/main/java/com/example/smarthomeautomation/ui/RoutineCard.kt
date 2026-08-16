package com.example.smarthomeautomation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.smarthomeautomation.data.AppUIState
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.MultiUnit
import com.example.smarthomeautomation.data.Room
import com.example.smarthomeautomation.data.Routine
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun RoutineCard(
    routine: Routine,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    viewModel: AppViewModel
) {
    val uiState = viewModel.uiState.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .hazeChild(state = hazeState),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = routine.name.ifBlank { "Unnamed Routine" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (routine.startTime.isNotBlank()) {
                        Text(
                            text = routine.startTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = { onToggle(routine.routineID) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Routine",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column() {
                        routine.devices.forEach { (deviceID, targetState) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val deviceName = findDeviceInfo(uiState.value.rooms, deviceID)
                                Text(
                                    text = deviceName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = targetState.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (targetState == DeviceState.ON) Color(0xFF51FDC7) else Color.White.copy(
                                        alpha = 0.6f
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun findDeviceInfo(rooms: List<Room>, targetId: Int): String {
    fun findInList(devices: List<Device>): Device? {
        for (device in devices) {
            if (device.deviceID == targetId) return device
            if (device is MultiUnit) {
                findInList(device.subUnits)?.let { return it }
            }
        }
        return null
    }

    for (room in rooms) {
        val found = findInList(room.devices)
        if (found != null) {
            val deviceName = found.name.ifBlank { "Unnamed" }
            return "$deviceName - ${room.floorName} - ${room.name}"
        }
    }
    return "Device #$targetId"
}