package com.example.smarthomeautomation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.smarthomeautomation.data.AppUIState
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.SafetyCritical

@Composable
fun HomePage(
    viewModel: AppViewModel,
    onAddDeviceButtonClick: () -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState()
    Column(
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = onAddDeviceButtonClick
        ) {
            Text("Add Device")
        }

        Column() {
            for (device in uiState.value.devices) {
                Button(onClick = {
                    viewModel.toggleDeviceHandler(device.deviceID)
                }) {
                    Text(device.deviceID.toString() + " " + device.state.name + " " + device.name)
                    if (device is SafetyCritical) {
                        Text(device.maxOnDuration.toString())
                    }
                }
            }
        }
    }
}