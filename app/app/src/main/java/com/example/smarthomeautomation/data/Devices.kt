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
) {
    open fun copy(
        deviceID: Int = this.deviceID,
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type
    ): Device {
        return Device(deviceID, state, name, type)
    }
}

class SingleUnit(
    deviceID: Int = -1,
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = ""
) : Device(deviceID, state, name, type) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String
    ): SingleUnit {
        return SingleUnit(deviceID, state, name, type)
    }
}

class MultiUnit(
    deviceID: Int = -1,
    val size: Int,
    val subUnits: MutableList<Device> = mutableListOf(),
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = ""
) : Device(deviceID, state, name, type) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String
    ): MultiUnit {
        return MultiUnit(deviceID, size, subUnits.toMutableList(), state, name, type)
    }

    fun copy(
        deviceID: Int = this.deviceID,
        size: Int = this.size,
        subUnits: MutableList<Device> = this.subUnits.toMutableList(),
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type
    ): MultiUnit {
        return MultiUnit(deviceID, size, subUnits, state, name, type)
    }
}

class SafetyCritical(
    deviceID: Int = -1,
    val maxOnDuration: Long,
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = ""
) : Device(deviceID, state, name, type) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String
    ): SafetyCritical {
        return SafetyCritical(deviceID, maxOnDuration, state, name, type)
    }

    fun copy(
        deviceID: Int = this.deviceID,
        maxOnDuration: Long = this.maxOnDuration,
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type
    ): SafetyCritical {
        return SafetyCritical(deviceID, maxOnDuration, state, name, type)
    }
}
