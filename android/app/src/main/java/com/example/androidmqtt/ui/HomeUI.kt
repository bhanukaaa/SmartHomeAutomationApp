package com.example.androidmqtt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidmqtt.data.AppViewModel

@Composable
fun HomeUI(
    viewModel: AppViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.collectAsState()

    Column(
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            viewModel.newDeviceHandler()
        }) {
            Text("New Device")
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (device in uiState.value.devices) {
                DeviceCard(
                    deviceId = device.deviceID,
                    switchedOn = device.switchedOn,
                    onSwitchChange = { id ->
                        viewModel.switchDeviceHandler(id)
                    }
                )
            }
        }
    }
}

@Composable
fun DeviceCard(
    deviceId: Int,
    switchedOn: Boolean,
    onSwitchChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.width(160.dp)
        ) {
            Text("Device ID: $deviceId")

            Switch(
                checked = switchedOn,
                onCheckedChange = {
                    onSwitchChange(deviceId)
                }
            )
        }
    }
}
