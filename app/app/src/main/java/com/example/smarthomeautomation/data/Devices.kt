package com.example.smarthomeautomation.data

enum class DeviceState {
    ON,
    OFF,
    ERROR,
    DISCONNECTED
}

open class Device(
    var deviceID: Int = -1,
    var state: DeviceState = DeviceState.OFF,
    var name: String = "",
    var type: String = ""
) {}

class SingleUnit(
    deviceID: Int = -1
) : Device(deviceID)

class MultiUnit(
    deviceID: Int = -1,
    val size: Int,
    val subUnits: MutableList<Device> = mutableListOf()
) : Device(deviceID)

class SafetyCritical(
    deviceID: Int = -1,
    val maxOnDuration: Long
) : Device(deviceID)