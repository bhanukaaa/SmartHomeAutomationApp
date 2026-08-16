package com.example.smarthomeautomation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.MultiUnit
import dev.chrisbanes.haze.HazeState

private sealed class DeviceRow {
    data class FullWidth(val device: Device) : DeviceRow()
    data class Pair(val left: Device, val right: Device?) : DeviceRow()
}

private fun prepareDeviceRows(devices: List<Device>): List<DeviceRow> {
    val rows = mutableListOf<DeviceRow>()
    val remaining = devices.toMutableList()
    var trailingSingle: Device? = null

    while (remaining.isNotEmpty()) {
        val current = remaining.removeAt(0)
        if (current is MultiUnit) {
            rows.add(DeviceRow.FullWidth(current))
        } else {
            val nextIndex = remaining.indexOfFirst { it !is MultiUnit }
            if (nextIndex != -1) {
                val pairDevice = remaining.removeAt(nextIndex)
                rows.add(DeviceRow.Pair(current, pairDevice))
            } else {
                trailingSingle = current
                break
            }
        }
    }

    while (remaining.isNotEmpty()) {
        val device = remaining.removeAt(0)
        if (device is MultiUnit) {
            rows.add(DeviceRow.FullWidth(device))
        }
    }

    if (trailingSingle != null) {
        rows.add(DeviceRow.Pair(trailingSingle, null))
    }

    return rows
}


@Composable
fun DeviceList(
    viewModel: AppViewModel,
    devices: List<Device>,
    hazeState: HazeState,
    showRoomLabel: Boolean = false
) {
    val rowItems = remember(devices) { prepareDeviceRows(devices) }
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rowItems) { row ->
            when (row) {
                is DeviceRow.FullWidth -> {
                    val roomID = uiState.deviceRegistry.getValue(row.device.deviceID)
                    DeviceCard(
                        device = row.device,
                        onToggle = { deviceID ->
                            viewModel.toggleDeviceHandler(deviceID)
                        },
                        onDelete = { deviceID ->
                            viewModel.deleteDeviceHandler(deviceID)
                        },
                        deleteable = true,
                        labelText = if (showRoomLabel) {
                            uiState.roomLabelRegistry.getOrDefault(roomID, "")
                        } else "",
                        hazeState = hazeState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is DeviceRow.Pair -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val device1 = row.left
                        val roomID1 = uiState.deviceRegistry.getValue(device1.deviceID)

                        DeviceCard(
                            device = row.left,
                            onToggle = { deviceID ->
                                viewModel.toggleDeviceHandler(deviceID)
                            },
                            onDelete = { deviceID ->
                                viewModel.deleteDeviceHandler(deviceID)
                            },
                            deleteable = true,
                            labelText = if (showRoomLabel) {
                                uiState.roomLabelRegistry.getOrDefault(roomID1, "")
                            } else "",
                            hazeState = hazeState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        if (row.right != null) {
                            val device2 = row.right
                            val roomID2 = uiState.deviceRegistry.getValue(device2.deviceID)
                            DeviceCard(
                                device = row.right,
                                onToggle = { deviceID ->
                                    viewModel.toggleDeviceHandler(deviceID)
                                },
                                onDelete = { deviceID ->
                                    viewModel.deleteDeviceHandler(deviceID)
                                },
                                deleteable = true,
                                labelText = if (showRoomLabel) {
                                    uiState.roomLabelRegistry.getOrDefault(roomID2, "")
                                } else "",
                                hazeState = hazeState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
