package com.example.smarthomeautomation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.smarthomeautomation.data.AppViewModel

@Composable
fun RoomPage(
    viewModel: AppViewModel
) {
    val uiState = viewModel.uiState.collectAsState()

}