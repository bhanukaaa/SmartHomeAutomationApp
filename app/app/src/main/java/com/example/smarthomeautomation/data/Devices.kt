package com.example.smarthomeautomation.data

enum class DeviceState {
    ON,
    OFF,
    ERROR,
    DISCONNECTED
}

open class Device(
    var deviceID: Int,
    var state: DeviceState = DeviceState.OFF,
    var type: String = ""
) {
    fun setDeviceID(deviceID: Int) {
        this.deviceID = deviceID
    }
}

class SingleUnit(
    deviceID: Int,
    var description: String = ""
) : Device(deviceID)

class MultiUnit(
    deviceID: Int,
    val size: Int,
    val subUnits: MutableList<Device> = mutableListOf()
) : Device(deviceID)

class SafetyCritical(
    deviceID: Int,
    val maxOnDuration: Long
) : Device(deviceID)