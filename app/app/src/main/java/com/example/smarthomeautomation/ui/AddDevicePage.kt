package com.example.smarthomeautomation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.smarthomeautomation.data.AppViewModel

@Composable
fun AddDevicePage(
    viewModel: AppViewModel,
    onDeviceCreated: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {
                viewModel.addDeviceHandler()
                onDeviceCreated()
            }
        ) {
            Text("Add Basic Device")
        }

    }

}