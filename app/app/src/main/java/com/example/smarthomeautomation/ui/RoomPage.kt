package com.example.smarthomeautomation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.DeviceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPage(
    viewModel: AppViewModel,
    roomName: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

     val allDevices = uiState.devices          // real devices from server
//    val allDevices = uiState.sampleDevices       // sample devices


    // Hard-coded mapping for sample devices
    val livingRoomIds = listOf(15, 12, 27)
    val kitchenIds    = listOf(34, 48)
    val bedroomIds    = listOf(45, 22)
    val entranceIds   = listOf(275)

    // All IDs that are already mapped to some room
    val allMappedIds = livingRoomIds + kitchenIds + bedroomIds + entranceIds

    // Decide which devices belong to the current room
    val deviceIdsInRoom: List<Int> = when (roomName) {
        "Living Room" -> livingRoomIds
        "Kitchen"     -> kitchenIds
        "Entrance"    -> entranceIds

        // Bedroom = original bedroom devices + any unmapped devices
        "Bedroom"     -> {
            val unmappedIds = allDevices
                .map { it.deviceID }
                .filter { it !in allMappedIds }
            bedroomIds + unmappedIds
        }

        else -> emptyList()
    }

    var devicesInRoom by remember(allDevices, roomName) {
        mutableStateOf(
            allDevices.filter { it.deviceID in deviceIdsInRoom }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = roomName,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (devicesInRoom.isEmpty()) {
                Text(
                    text = "No devices in this room",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(devicesInRoom, key = { it.deviceID }) { device ->
                        DeviceCard(
                            device = device,
                            onToggle = { deviceID ->
                                // Update local list so UI reacts immediately
                                devicesInRoom = devicesInRoom.map { d ->
                                    if (d.deviceID == deviceID) {
                                        val newState = if (d.state == DeviceState.ON)
                                            DeviceState.OFF
                                        else
                                            DeviceState.ON
                                        d.copy(state = newState)
                                    } else {
                                        d
                                    }
                                }
                                // Also notify the real ViewModel / server
                                viewModel.toggleDeviceHandler(deviceID)
                            }
                        )
                    }
                }
            }
        }
    }
}