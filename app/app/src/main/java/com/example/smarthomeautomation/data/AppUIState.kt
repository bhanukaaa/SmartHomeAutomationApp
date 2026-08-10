package com.example.smarthomeautomation.data

data class Room(
    val roomID: Int,
    val name: String,
    val floorName: String = "G",
    val devices: List<Device> = emptyList()
)

data class AppUIState(
    val rooms: List<Room> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val deviceRegistry: Map<Int, Int> = emptyMap(),
    val currentFloorName: String = "G",
    val currentRoomID: Int? = null,
    val currentRoutineID: Int? = null
)
