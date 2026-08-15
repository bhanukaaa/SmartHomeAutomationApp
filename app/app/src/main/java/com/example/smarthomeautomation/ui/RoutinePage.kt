package com.example.smarthomeautomation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.data.Routine
import com.example.smarthomeautomation.ui.components.NavBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinePage(
    viewModel: AppViewModel,
    navController: NavHostController,
    onAddRoutineButtonClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val hazeState = remember { HazeState() }

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
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                "Routines",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            OutlinedButton(
                                onClick = onAddRoutineButtonClick,
                                border = BorderStroke(
                                    0.5.dp,
                                    Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Add Routine", color = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                NavBar(navController, hazeState)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val routines = uiState.routines ?: emptyList()

                if (routines.isEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No Routines",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = onAddRoutineButtonClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Add Routine", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    RoutineList(viewModel, routines, hazeState)
                }
            }
        }
    }
}

@Composable
fun RoutineList(viewModel: AppViewModel, routines: List<Routine>, hazeState: HazeState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(routines, key = { it.routineID }) { routine ->
            RoutineCard(
                routine = routine,
                onToggle = { routineID ->
                    viewModel.startRoutineHandler(routineID)
                },
                hazeState = hazeState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
