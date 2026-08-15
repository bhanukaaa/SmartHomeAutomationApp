package com.example.smarthomeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Device
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
        uiState.rooms.flatMap { room ->
            room.devices.map { it to room.name }
        }.sortedByDescending { it.first.onTimeMinutes }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            "Device Usage Report",
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "Total Usage Time (Minutes)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(allDevices) { (device, roomName) ->
                    ReportCard(device = device, roomName = roomName, hazeState = hazeState)
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    device: Device,
    roomName: String,
    hazeState: HazeState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(16.dp),
                style = HazeDefaults.style(
                    blurRadius = 24.dp,
                    backgroundColor = Color.Transparent,
                    tint = Color.White.copy(alpha = 0.05f)
                )
            )
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${device.onTimeMinutes} min",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF10B981), // Success green
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "on time",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
