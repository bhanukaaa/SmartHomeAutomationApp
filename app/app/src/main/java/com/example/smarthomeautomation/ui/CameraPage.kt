package com.example.smarthomeautomation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.ui.components.NavBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPage(
    viewModel: AppViewModel,
    navController: NavHostController,
) {
    val uiState by viewModel.uiState.collectAsState()
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF388E9C),
                            Color(0xFF6F7F8C),
                            Color(0xFFAC6B53)
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
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                "Cameras",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            OutlinedButton(
                                onClick = {},
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
                                Text("Add Camera", color = Color.White)
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
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                for (camUrl in uiState.cameras) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        CamFeed(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                            streamUrl = camUrl
                        )
                    }
                }
            }
        }
    }
}
