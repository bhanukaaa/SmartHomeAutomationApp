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
    val currentRoutineID: Int? = null,
    val cameras: List<String> = listOf(
        "https://live.143b.ch/cam/flux/ts:abr.m3u8",
        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
        "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"
    )
)
