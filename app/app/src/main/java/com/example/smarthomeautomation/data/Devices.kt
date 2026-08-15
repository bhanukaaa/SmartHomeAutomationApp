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
    var type: String = "",
    var power: Float = 0f,
    var onTimeMinutes: Int = 0 // Mocked for the report
) {
    open fun copy(
        deviceID: Int = this.deviceID,
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type,
        power: Float = this.power,
        onTimeMinutes: Int = this.onTimeMinutes
    ): Device {
        return Device(deviceID, state, name, type, power, onTimeMinutes)
    }
}

class SingleUnit(
    deviceID: Int = -1,
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = "",
    power: Float = 0f,
    onTimeMinutes: Int = 0
) : Device(deviceID, state, name, type, power, onTimeMinutes) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String,
        power: Float,
        onTimeMinutes: Int
    ): SingleUnit {
        return SingleUnit(deviceID, state, name, type, power, onTimeMinutes)
    }
}

class MultiUnit(
    deviceID: Int = -1,
    val size: Int,
    val subUnits: MutableList<Device> = mutableListOf(),
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = "",
    power: Float = 0f,
    onTimeMinutes: Int = 0
) : Device(deviceID, state, name, type, power, onTimeMinutes) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String,
        power: Float,
        onTimeMinutes: Int
    ): MultiUnit {
        return MultiUnit(deviceID, size, subUnits.toMutableList(), state, name, type, power, onTimeMinutes)
    }

    fun copy(
        deviceID: Int = this.deviceID,
        size: Int = this.size,
        subUnits: MutableList<Device> = this.subUnits.toMutableList(),
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type,
        power: Float = this.power,
        onTimeMinutes: Int = this.onTimeMinutes
    ): MultiUnit {
        return MultiUnit(deviceID, size, subUnits, state, name, type, power, onTimeMinutes)
    }
}

class SafetyCritical(
    deviceID: Int = -1,
    val maxOnDuration: Long,
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = "",
    power: Float = 0f,
    onTimeMinutes: Int = 0
) : Device(deviceID, state, name, type, power, onTimeMinutes) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String,
        power: Float,
        onTimeMinutes: Int
    ): SafetyCritical {
        return SafetyCritical(deviceID, maxOnDuration, state, name, type, power, onTimeMinutes)
    }

    fun copy(
        deviceID: Int = this.deviceID,
        maxOnDuration: Long = this.maxOnDuration,
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type,
        power: Float = this.power,
        onTimeMinutes: Int = this.onTimeMinutes
    ): SafetyCritical {
        return SafetyCritical(deviceID, maxOnDuration, state, name, type, power, onTimeMinutes)
    }
}
