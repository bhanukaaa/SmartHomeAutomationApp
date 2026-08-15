package com.example.smarthomeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.DeviceState
import com.example.smarthomeautomation.data.MultiUnit
import com.example.smarthomeautomation.data.SafetyCritical
import com.example.smarthomeautomation.data.SingleUnit
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

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
    var powerInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DeviceCategory.SingleUnit) }
    var sizeInput by remember { mutableStateOf("") }
    var maxOnDurationInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val subUnitNames = remember { mutableStateListOf<String>() }

    val scrollState = rememberScrollState()
    val maxSubUnits = sizeInput.toIntOrNull() ?: 0
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Deep Sophisticated Diagonal Background for Glassmorphism
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFA6767A), // Top-Right: Muted Terracotta Brick
                            Color(0xFF5D748A), // Bottom-Left: Muted Slate Blue
                            Color(0xFF5A756C)  // Bottom-Left: Pale Soft Sage
                        ),
                        start = Offset.Infinite,
                        end = Offset.Zero
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
                            "Add Device",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDeviceCreated) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .hazeChild(
                            state = hazeState,
                            shape = RoundedCornerShape(24.dp),
                            style = HazeDefaults.style(
                                blurRadius = 24.dp,
                                backgroundColor = Color.Transparent,
                                tint = Color.White.copy(alpha = 0.05f)
                            )
                        )
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(
                            width = 0.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(0.2f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                            focusedLeadingIconColor = Color.White,
                            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.5f)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Device Name") },
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = powerInput,
                            onValueChange = { powerInput = it },
                            label = { Text("Power (W)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp),
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
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                                },
                                colors = textFieldColors,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2D2D2D)) // Darker background for dropdown
                            ) {
                                DeviceCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = category.name,
                                                color = Color.White
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
                                            subUnitNames.removeAt(subUnitNames.size - 1)
                                        }
                                    },
                                    label = { Text("Max Sub Units (Size)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = textFieldColors,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                subUnitNames.forEachIndexed { index, subUnitName ->
                                    OutlinedTextField(
                                        value = subUnitName,
                                        onValueChange = { subUnitNames[index] = it },
                                        label = { Text("Sub Unit ${index + 1} Name") },
                                        singleLine = true,
                                        colors = textFieldColors,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (subUnitNames.size < maxSubUnits) {
                                    OutlinedButton(
                                        onClick = { subUnitNames.add("") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            Color.White.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text("Add Sub Unit (${subUnitNames.size}/$maxSubUnits)", color = Color.White)
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
                                    colors = textFieldColors,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val typeName = selectedCategory.name
                        val power = powerInput.toFloatOrNull() ?: 0f
                        val newDevice: Device = when (selectedCategory) {
                            DeviceCategory.SingleUnit -> SingleUnit(
                                state = DeviceState.OFF,
                                name = name,
                                type = typeName,
                                power = power
                            )

                            DeviceCategory.MultiUnit -> {
                                val subUnitsList = subUnitNames.map { subName ->
                                    SingleUnit(
                                        state = DeviceState.OFF,
                                        name = subName,
                                        type = DeviceCategory.SingleUnit.name,
                                        power = 0f // Subunits usually don't have individual power in this model or defaults to 0
                                    )
                                }.toMutableList<Device>()

                                MultiUnit(
                                    size = maxSubUnits,
                                    subUnits = subUnitsList,
                                    state = DeviceState.OFF,
                                    name = name,
                                    type = typeName,
                                    power = power
                                )
                            }

                            DeviceCategory.SafetyCritical -> SafetyCritical(
                                maxOnDuration = maxOnDurationInput.toLongOrNull() ?: 0L,
                                state = DeviceState.OFF,
                                name = name,
                                type = typeName,
                                power = power
                            )
                        }

                        viewModel.addDeviceHandler(newDevice)
                        onDeviceCreated()
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981), // Mac Green to match switches
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.12f),
                        disabledContentColor = Color.White.copy(alpha = 0.38f)
                    )
                ) {
                    Text("Save Device", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
