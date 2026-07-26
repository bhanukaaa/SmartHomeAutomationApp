package com.example.smarthomeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.MultiUnit
import com.example.smarthomeautomation.data.SafetyCritical
import com.example.smarthomeautomation.data.SingleUnit

enum class DeviceCategory {
    SINGLE_UNIT,
    MULTI_UNIT,
    SAFETY_CRITICAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDevicePage(
    viewModel: AppViewModel,
    onDeviceCreated: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DeviceCategory.SINGLE_UNIT) }
    var sizeInput by remember { mutableStateOf("") }
    var maxOnDurationInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val subUnitNames = remember { mutableStateListOf<String>() }

    val scrollState = rememberScrollState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    val maxSubUnits = sizeInput.toIntOrNull() ?: 0

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Device Name") },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory.name.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                colors = textFieldColors,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                DeviceCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = category.name.replace("_", " "),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            selectedCategory = category
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedCategory) {
            DeviceCategory.SINGLE_UNIT -> {}
            DeviceCategory.MULTI_UNIT -> {
                OutlinedTextField(
                    value = sizeInput,
                    onValueChange = { input ->
                        sizeInput = input
                        val newSize = input.toIntOrNull() ?: 0
                        while (subUnitNames.size > newSize) {
                            subUnitNames.removeLast()
                        }
                    },
                    label = { Text("Max Sub Units (Size)") },
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                subUnitNames.forEachIndexed { index, subUnitName ->
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subUnitName,
                        onValueChange = { subUnitNames[index] = it },
                        label = { Text("Sub Unit ${index + 1} Name") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (subUnitNames.size < maxSubUnits) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { subUnitNames.add("") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Sub Unit (${subUnitNames.size}/$maxSubUnits)")
                    }
                }
            }
            DeviceCategory.SAFETY_CRITICAL -> {
                OutlinedTextField(
                    value = maxOnDurationInput,
                    onValueChange = { maxOnDurationInput = it },
                    label = { Text("Max On Duration (ms)") },
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val typeName = selectedCategory.name
                val newDevice: Device = when (selectedCategory) {
                    DeviceCategory.SINGLE_UNIT -> SingleUnit(
                        state = DeviceState.OFF,
                        name = name,
                        type = typeName
                    )
                    DeviceCategory.MULTI_UNIT -> {
                        val subUnitsList = subUnitNames.map { subName ->
                            SingleUnit(
                                state = DeviceState.OFF,
                                name = subName,
                                type = DeviceCategory.SINGLE_UNIT.name
                            )
                        }.toMutableList<Device>()

                        MultiUnit(
                            size = maxSubUnits,
                            subUnits = subUnitsList,
                            state = DeviceState.OFF,
                            name = name,
                            type = typeName
                        )
                    }
                    DeviceCategory.SAFETY_CRITICAL -> SafetyCritical(
                        maxOnDuration = maxOnDurationInput.toLongOrNull() ?: 0L,
                        state = DeviceState.OFF,
                        name = name,
                        type = typeName
                    )
                }

                viewModel.addDeviceHandler(newDevice)
                onDeviceCreated()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Device")
        }
    }
}
