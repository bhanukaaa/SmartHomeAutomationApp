package com.example.smarthomeautomation.data

data class AppUIState(
    val devices: List<Device> = emptyList(),

    val sampleDevices: List<Device> = listOf(
        SingleUnit().apply {
            deviceID = 15
            name = "Smart Bulb"
            type = "SingleUnit"
            state = DeviceState.ON
        },
        SingleUnit().apply {
            deviceID = 22
            name = "Front Door Lock"
            type = "SingleUnit"
            state = DeviceState.DISCONNECTED
        },
        SingleUnit().apply {
            deviceID = 45
            name = "Smart Bulb"
            type = "SingleUnit"
            state = DeviceState.ON
        },
        SingleUnit().apply {
            deviceID = 275
            name = "Back Door Lock"
            type = "SingleUnit"
            state = DeviceState.OFF
        },
        SingleUnit().apply {
            deviceID = 12
            name = "TV"
            type = "SingleUnit"
            state = DeviceState.ON
        },
        SingleUnit().apply {
            deviceID = 27
            name = "Ceiling Lamp"
            type = "SingleUnit"
            state = DeviceState.ERROR
        },
        SafetyCritical(maxOnDuration = 1800L).apply {
            deviceID = 34
            name = "Kettle"
            type = "SafetyCritical"
            state = DeviceState.OFF
        },
        SafetyCritical(maxOnDuration = 3600L).apply {
            deviceID = 48
            name = "Oven"
            type = "SafetyCritical"
            state = DeviceState.ON
        }
    )
)
