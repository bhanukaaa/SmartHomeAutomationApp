package com.example.smarthomeautomation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Room
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: AppViewModel,
    onTempButtonClick: () -> Unit,
    onAddRoomButtonClick: () -> Unit,
    onRoomClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }

    val floors = remember(uiState.rooms) {
        val extractedFloors = uiState.rooms.map { it.floorName }.distinct()
        if (extractedFloors.isEmpty()) listOf("G") else extractedFloors
    }

    val pagerState = rememberPagerState(initialPage = 0) { floors.size }

    LaunchedEffect(uiState.currentFloorName, floors) {
        val targetIndex =
            floors.indexOfFirst { it.equals(uiState.currentFloorName, ignoreCase = true) }
        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page < floors.size) {
                val selectedFloor = floors[page]
                if (!selectedFloor.equals(uiState.currentFloorName, ignoreCase = true)) {
                    viewModel.selectFloor(selectedFloor)
                }
            }
        }
    }

    val currentFloorName = floors.getOrNull(pagerState.currentPage) ?: "G"

    Box(modifier = Modifier.fillMaxSize()) {
        // Deep Sophisticated Diagonal Background for Glassmorphism
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFA6767A), // 1. Your Muted Terracotta
                            Color(0xFF5D748A), // 2. Your Muted Slate Blue
                            Color(0xFF425363), // NEW: Deep Steel Shadow (Improves front-end contrast)
                            Color(0xFF5A756C) // Bottom-Left: Pale Soft Sage
                        ),
                        start = Offset.Infinite,
                        end = Offset.Zero
                    )
                )
                .haze(state = hazeState)
        )

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onTempButtonClick() },
                    content = { Icon(Icons.Default.Add, contentDescription = null) },
                    containerColor = Color(0xFFC2185B),
                    contentColor = Color.White
                )
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(floors, key = { it }) { floorName ->
                        FilterChip(
                            selected = floorName.equals(currentFloorName, ignoreCase = true),
                            onClick = {
                                val targetIndex =
                                    floors.indexOfFirst { it.equals(floorName, ignoreCase = true) }
                                if (targetIndex >= 0) {
                                    viewModel.selectFloor(floorName)
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(targetIndex)
                                    }
                                }
                            },
                            label = { Text(floorName, color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White.copy(alpha = 0.2f),
                                containerColor = Color.Transparent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.White.copy(alpha = 0.2f),
                                selectedBorderColor = Color.White.copy(alpha = 0.5f),
                                borderWidth = 0.5.dp,
                                enabled = true,
                                selected = floorName.equals(currentFloorName, ignoreCase = true)
                            )
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val floorName = floors[page]
                    val roomsOnFloor =
                        uiState.rooms.filter { it.floorName.equals(floorName, ignoreCase = true) }

                    if (roomsOnFloor.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No rooms on Floor $floorName", color = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddRoomButtonClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(text = "Add Room", color = Color.White)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(span = { GridItemSpan(2) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rooms",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    OutlinedButton(
                                        onClick = onAddRoomButtonClick,
                                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text("Add Room", color = Color.White)
                                    }
                                }
                            }

                            items(roomsOnFloor, key = { it.roomID }) { room ->
                                RoomCard(
                                    room = room,
                                    hazeState = hazeState,
                                    onClick = {
                                        viewModel.selectRoom(room.roomID)
                                        onRoomClick(room.roomID)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomCard(
    room: Room,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .hazeChild(state = hazeState, shape = RoundedCornerShape(24.dp), style = HazeDefaults.style(
                blurRadius = 24.dp,
                backgroundColor = Color.Transparent,
                tint = Color.White.copy(alpha = 0.05f)
            ))
            .background(Color.White.copy(alpha = 0.12f)) // simple background for testing
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(0.2f), Color.Transparent)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    )

    {
        Column(horizontalAlignment = Alignment.Start) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = room.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${room.devices.size} devices",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}