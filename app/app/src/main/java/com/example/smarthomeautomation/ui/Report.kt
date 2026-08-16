package com.example.smarthomeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
import com.example.smarthomeautomation.data.MultiUnit
import com.example.smarthomeautomation.ui.components.NavBar
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Report(
    viewModel: AppViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        viewModel.fetchUsageReport()
    }

    val allDevices = remember(uiState.rooms) {
        val flattenedList = mutableListOf<Device>()
        
        fun addDeviceRecursively(device: Device) {
            flattenedList.add(device)
            if (device is MultiUnit) {
                device.subUnits.forEach { sub ->
                    addDeviceRecursively(sub)
                }
            }
        }

        uiState.rooms.forEach { room ->
            room.devices.forEach { device ->
                addDeviceRecursively(device)
            }
        }
        flattenedList
    }

    val totalOnTime = allDevices.sumOf { it.onTimeMinutes }
    val totalEnergyKwh = allDevices.sumOf { (it.power.toDouble() * it.onTimeMinutes / 60.0) / 1000.0 }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background consistent with other pages
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFA6767A), Color(0xFF5D748A), Color(0xFF5A756C)),
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
                            "Usage Analytics",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                NavBar(navController = navController, hazeState = hazeState)
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Summary Cards Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            label = "Energy Used",
                            value = "%.2f".format(totalEnergyKwh),
                            unit = "kWh",
                            color = Color(0xFF22F6AE),
                            modifier = Modifier.weight(1f),
                            hazeState = hazeState
                        )
                        SummaryCard(
                            label = "Active Time",
                            value = "%.2f".format(totalOnTime / 60f),
                            unit = "hours",
                            color = Color(0xFF1FF8B0),
                            modifier = Modifier.weight(1f),
                            hazeState = hazeState
                        )
                    }
                }

                item {
                    UsageTable(
                        title = "Energy Consumption",
                        headers = listOf("Device", "Power(W)", "kWh"),
                        data = allDevices.sortedByDescending { it.power * it.onTimeMinutes }.map {
                            listOf(
                                it.name,
                                it.power.toInt().toString(),
                                "%.3f".format((it.power.toDouble() * it.onTimeMinutes / 60.0) / 1000.0)
                            )
                        },
                        hazeState = hazeState
                    )
                }

                item {
                    UsageTable(
                        title = "Lifetime Usage",
                        headers = listOf("Device", "Total Hours"),
                        data = allDevices.sortedByDescending { it.lifetimeOnTimeMinutes }.map {
                            listOf(it.name, "%.1f hrs".format(it.lifetimeOnTimeMinutes / 60.0))
                        },
                        hazeState = hazeState
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    hazeState: HazeState
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(20.dp),
                style = HazeDefaults.style(
                    blurRadius = 24.dp,
                    backgroundColor = Color.Transparent,
                    tint = Color.White.copy(alpha = 0.05f)
                )
            )
            .background(Color.White.copy(alpha = 0.1f))
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    color = color,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(4.dp))
                Text(unit, color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun UsageTable(
    title: String,
    headers: List<String>,
    data: List<List<String>>,
    hazeState: HazeState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(20.dp),
                style = HazeDefaults.style(blurRadius = 24.dp, backgroundColor = Color.Transparent, tint = Color.White.copy(alpha = 0.05f))
            )
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            headers.forEachIndexed { index, header ->
                Text(
                    text = header,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(if (index == 0) 1.5f else 1f),
                    textAlign = if (index == 0) TextAlign.Start else TextAlign.End
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

        data.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEachIndexed { index, cell ->
                    Text(
                        text = cell,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(if (index == 0) 1.5f else 1f),
                        textAlign = if (index == 0) TextAlign.Start else TextAlign.End
                    )
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
        }
        
        if (data.isEmpty()) {
            Text(
                "No data available",
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}
