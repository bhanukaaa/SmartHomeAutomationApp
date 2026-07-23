package com.example.smarthomeautomation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.ui.AddDevicePage
import com.example.smarthomeautomation.ui.HomePage
import com.example.smarthomeautomation.ui.RoomPage

enum class AppPages() {
    Home,
    AddDevice,
    Room
}

@Composable
fun AppController(
    navController: NavHostController = rememberNavController(),
    viewModel: AppViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = AppPages.Home.name,
        modifier = Modifier
    ) {
        composable(route = AppPages.Home.name) {
            HomePage(
                viewModel = viewModel,
                onAddDeviceButtonClick = {
                    navController.navigate(
                        AppPages.AddDevice.name
                    )
                }
            )
        }

        composable(route = AppPages.AddDevice.name) {
            AddDevicePage(
                viewModel = viewModel,
                onDeviceCreated = {
                    navController.navigate(AppPages.Home.name)
                }
            )
        }

        composable(route = AppPages.Room.name) {
            RoomPage(
                viewModel = viewModel
            )
        }
    }
}