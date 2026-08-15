package com.example.smarthomeautomation.ui

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.Routine
import com.example.smarthomeautomation.data.RoutineState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoutinePage(viewModel: AppViewModel, onRoutineCreated: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    var routineName by remember { mutableStateOf("") }
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )
    
    // Track selected devices by ID and their intended state for the routine
    var selectedDeviceStates by remember { mutableStateOf(emptyMap<Int, DeviceState>()) }

    val scrollState = rememberScrollState()
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFA6767A), Color(0xFF5D748A), Color(0xFF5A756C)),
                        start = Offset.Infinite,
                        end = Offset.Zero,
                    )
                )
                .haze(state = hazeState)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Add Routine",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onRoutineCreated) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = Color.White.copy(alpha = 0.7f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                )

                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Routine Name") },
                    singleLine = true,
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                val timePickerColors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = Color.White.copy(alpha = 0.2f),
                    timeSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.08f),
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                    periodSelectorSelectedContainerColor = Color.White.copy(alpha = 0.25f),
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                    periodSelectorBorderColor = Color.White.copy(alpha = 0.3f),
                )

                TimeInput(
                    state = timePickerState,
                    colors = timePickerColors,
                    modifier = Modifier.fillMaxWidth()
                )

                val rooms = uiState.rooms ?: emptyList()
                val allDevicesWithRoomInfo = rooms.flatMap { room -> 
                    (room.devices ?: emptyList()).map { device -> 
                        device to ("${room.floorName} - ${room.name}")
                    } 
                }
                val selectedDevices = allDevicesWithRoomInfo.filter { (device, _) -> selectedDeviceStates.containsKey(device.deviceID) }

                Text(
                    text = "Selected Devices",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (selectedDevices.isEmpty()) {
                    Text(
                        text = "No devices selected.",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((device, roomLabel) in selectedDevices) {
                            Column {
                                Text(
                                    text = roomLabel,
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                )
                                val displayDevice = device.copy().apply { state = selectedDeviceStates[device.deviceID]!! }
                                DeviceCard(
                                    device = displayDevice,
                                    enabled = true,
                                    onToggle = { deviceID ->
                                        val currentState = selectedDeviceStates[deviceID]
                                        val newState = if (currentState == DeviceState.ON) DeviceState.OFF else DeviceState.ON
                                        selectedDeviceStates = selectedDeviceStates + (deviceID to newState)
                                    },
                                    hazeState = hazeState,
                                    onBodyClick = { deviceID ->
                                        selectedDeviceStates = selectedDeviceStates - deviceID
                                    },
                                    small = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Device List Section ---
                Text(
                    text = "Available Devices",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (room in rooms) {
                        val devices = room.devices ?: emptyList()
                        val unselectedDevicesInRoom = devices.filter { !selectedDeviceStates.containsKey(it.deviceID) }
                        
                        if (unselectedDevicesInRoom.isNotEmpty()) {
                            Column {
                                Text(
                                    text = "${room.floorName} - ${room.name}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (device in unselectedDevicesInRoom) {
                                        DeviceCard(
                                            device = device,
                                            enabled = false,
                                            onToggle = {},
                                            hazeState = hazeState,
                                            onBodyClick = { deviceID ->
                                                selectedDeviceStates = selectedDeviceStates + (deviceID to DeviceState.ON)
                                            },
                                            small = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val timeString = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        val newRoutine = Routine(
                            name = routineName,
                            startTime = timeString,
                            routineState = RoutineState.ENABLED,
                            devices = selectedDeviceStates
                        )
                        viewModel.addRoutineHandler(newRoutine)
                        onRoutineCreated()
                    },
                    enabled = routineName.isNotBlank() && selectedDeviceStates.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.12f),
                        disabledContentColor = Color.White.copy(alpha = 0.38f),
                    ),
                ) {
                    Text(
                        "Save Routine",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
