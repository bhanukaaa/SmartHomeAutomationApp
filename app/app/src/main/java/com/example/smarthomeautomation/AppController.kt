package com.example.smarthomeautomation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthomeautomation.data.AppViewModel

enum class AppPages {
    Home,
    AddDevice,
    AddRoom,
    Room
}

@Composable
fun AppController(
    navController: NavHostController = rememberNavController(),
    viewModel: AppViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppPages.Home.name,
        modifier = Modifier
    ) {
        composable(route = AppPages.Home.name) {
            HomePage(
                viewModel = viewModel,
                onAddRoomButtonClick = {
                    navController.navigate(AppPages.AddRoom.name)
                },
                onRoomClick = { roomID ->
                    viewModel.selectRoom(roomID)
                    navController.navigate(AppPages.Room.name)
                }
            )
        }

        composable(route = AppPages.Room.name) {
            RoomPage(
                viewModel = viewModel,
                onAddDeviceButtonClick = {
                    navController.navigate(AppPages.AddDevice.name)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = AppPages.AddDevice.name) {
            AddDevicePage(
                viewModel = viewModel,
                onDeviceCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppPages.AddRoom.name) {
            AddRoomPage(
                viewModel = viewModel,
                onRoomCreated = {
                    navController.popBackStack()
                }
            )
        }
    }
}
