package com.example.smarthomeautomation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthomeautomation.data.AppViewModel
import com.example.smarthomeautomation.ui.AddDevicePage
import com.example.smarthomeautomation.ui.AddRoomPage
import com.example.smarthomeautomation.ui.AddRoutinePage
import com.example.smarthomeautomation.ui.CameraPage
import com.example.smarthomeautomation.ui.HomePage
import com.example.smarthomeautomation.ui.RoomPage
import com.example.smarthomeautomation.ui.RoutinePage

enum class AppPages {
    Home,
    AddDevice,
    AddRoom,
    AddRoutine,
    Room,
    Routine,
    Cameras
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
                navController = navController,
                onAddRoomButtonClick = {
                    navController.navigate(AppPages.AddRoom.name)
                },
                onRoomClick = { roomID ->
                    viewModel.selectRoom(roomID)
                    navController.navigate(AppPages.Room.name)
                }
            )
        }

        composable(route = AppPages.Routine.name) {
            RoutinePage(
                viewModel = viewModel,
                navController = navController,
                onAddRoutineButtonClick = {
                    navController.navigate(AppPages.AddRoutine.name)
                }
            )
        }

        composable(route = AppPages.Room.name) {
            RoomPage(
                viewModel = viewModel,
                navController = navController,
                onAddDeviceButtonClick = {
                    navController.navigate(AppPages.AddDevice.name)
                }
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

        composable(route = AppPages.AddRoutine.name) {
            AddRoutinePage(
                viewModel = viewModel,
                onRoutineCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppPages.Cameras.name) {
            CameraPage(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}
