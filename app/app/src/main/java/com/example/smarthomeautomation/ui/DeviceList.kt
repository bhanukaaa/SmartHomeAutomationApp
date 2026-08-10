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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.MultiUnit

private sealed class DeviceRow {
    data class FullWidth(val device: Device) : DeviceRow()
    data class Pair(val left: Device, val right: Device?) : DeviceRow()
}

private fun prepareDeviceRows(devices: List<Device>): List<DeviceRow> {
    val rows = mutableListOf<DeviceRow>()
    var i = 0
    while (i < devices.size) {
        val current = devices[i]
        if (current is MultiUnit) {
            rows.add(DeviceRow.FullWidth(current))
            i++
        } else {
            val next = devices.getOrNull(i + 1)
            if (next != null && next !is MultiUnit) {
                rows.add(DeviceRow.Pair(current, next))
                i += 2
            } else {
                rows.add(DeviceRow.Pair(current, null))
                i++
            }
        }
    }
    return rows
}

@Composable
fun DeviceList(viewModel: AppViewModel, devices: List<Device>) {
    val rowItems = remember(devices) { prepareDeviceRows(devices) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rowItems) { row ->
            when (row) {
                is DeviceRow.FullWidth -> {
                    DeviceCard(
                        device = row.device,
                        onToggle = { deviceID ->
                            viewModel.toggleDeviceHandler(deviceID)
                        },
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
                        DeviceCard(
                            device = row.left,
                            onToggle = { deviceID ->
                                viewModel.toggleDeviceHandler(deviceID)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        if (row.right != null) {
                            DeviceCard(
                                device = row.right,
                                onToggle = { deviceID ->
                                    viewModel.toggleDeviceHandler(deviceID)
                                },
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
