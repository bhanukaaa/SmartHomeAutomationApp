package com.example.smarthomeautomation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.ui.AddDevicePage
import com.example.smarthomeautomation.ui.HomePage
import com.example.smarthomeautomation.ui.RoomPage

enum class AppPages {
    Home,
    AddDevice,
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
                onAddDeviceButtonClick = {
                    navController.navigate(AppPages.AddDevice.name)
                },
                onRoomClick = { roomName ->
                    navController.navigate("${AppPages.Room.name}/$roomName")
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

        composable(
            route = "${AppPages.Room.name}/{roomName}",
            arguments = listOf(navArgument("roomName") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomName = backStackEntry.arguments?.getString("roomName") ?: "Unknown"
            RoomPage(
                viewModel = viewModel,
                roomName = roomName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}