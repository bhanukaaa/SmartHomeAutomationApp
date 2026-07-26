package com.example.smarthomeautomation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.MultiUnit
import com.example.smarthomeautomation.data.SafetyCritical
import com.example.smarthomeautomation.data.SingleUnit

enum class DeviceCategory {
    SingleUnit,
    MultiUnit,
    SafetyCritical
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDevicePage(
    viewModel: AppViewModel,
    onDeviceCreated: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DeviceCategory.SingleUnit) }
    var sizeInput by remember { mutableStateOf("") }
    var maxOnDurationInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val subUnitNames = remember { mutableStateListOf<String>() }

    val scrollState = rememberScrollState()
    val maxSubUnits = sizeInput.toIntOrNull() ?: 0

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Device Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
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
                                            text = category.name,
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

                    when (selectedCategory) {
                        DeviceCategory.SingleUnit -> {}
                        DeviceCategory.MultiUnit -> {
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            subUnitNames.forEachIndexed { index, subUnitName ->
                                OutlinedTextField(
                                    value = subUnitName,
                                    onValueChange = { subUnitNames[index] = it },
                                    label = { Text("Sub Unit ${index + 1} Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (subUnitNames.size < maxSubUnits) {
                                OutlinedButton(
                                    onClick = { subUnitNames.add("") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(start = 4.dp))
                                    Text("Add Sub Unit (${subUnitNames.size}/$maxSubUnits)")
                                }
                            }
                        }
                        DeviceCategory.SafetyCritical -> {
                            OutlinedTextField(
                                value = maxOnDurationInput,
                                onValueChange = { maxOnDurationInput = it },
                                label = { Text("Max On Duration (s)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val typeName = selectedCategory.name
                    val newDevice: Device = when (selectedCategory) {
                        DeviceCategory.SingleUnit -> SingleUnit(
                            state = DeviceState.OFF,
                            name = name,
                            type = typeName
                        )
                        DeviceCategory.MultiUnit -> {
                            val subUnitsList = subUnitNames.map { subName ->
                                SingleUnit(
                                    state = DeviceState.OFF,
                                    name = subName,
                                    type = DeviceCategory.SingleUnit.name
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
                        DeviceCategory.SafetyCritical -> SafetyCritical(
                            maxOnDuration = maxOnDurationInput.toLongOrNull() ?: 0L,
                            state = DeviceState.OFF,
                            name = name,
                            type = typeName
                        )
                    }

                    viewModel.addDeviceHandler(newDevice)
                    onDeviceCreated()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Device")
            }
        }
    }
}
