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
    var onTimeMinutes: Int = 0,
    var lifetimeOnTimeMinutes: Int = 0
) {
    open fun copy(
        deviceID: Int = this.deviceID,
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type,
        power: Float = this.power,
        onTimeMinutes: Int = this.onTimeMinutes,
        lifetimeOnTimeMinutes: Int = this.lifetimeOnTimeMinutes
    ): Device {
        return Device(deviceID, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes)
    }
}

class SingleUnit(
    deviceID: Int = -1,
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = "",
    power: Float = 0f,
    onTimeMinutes: Int = 0,
    lifetimeOnTimeMinutes: Int = 0
) : Device(deviceID, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String,
        power: Float,
        onTimeMinutes: Int,
        lifetimeOnTimeMinutes: Int
    ): SingleUnit {
        return SingleUnit(deviceID, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes)
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
    onTimeMinutes: Int = 0,
    lifetimeOnTimeMinutes: Int = 0
) : Device(deviceID, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String,
        power: Float,
        onTimeMinutes: Int,
        lifetimeOnTimeMinutes: Int
    ): MultiUnit {
        return MultiUnit(deviceID, size, subUnits.toMutableList(), state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes)
    }

    fun copy(
        deviceID: Int = this.deviceID,
        size: Int = this.size,
        subUnits: MutableList<Device> = this.subUnits.toMutableList(),
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type,
        power: Float = this.power,
        onTimeMinutes: Int = this.onTimeMinutes,
        lifetimeOnTimeMinutes: Int = this.lifetimeOnTimeMinutes
    ): MultiUnit {
        return MultiUnit(deviceID, size, subUnits, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes)
    }
}

class SafetyCritical(
    deviceID: Int = -1,
    val maxOnDuration: Long,
    state: DeviceState = DeviceState.OFF,
    name: String = "",
    type: String = "",
    power: Float = 0f,
    onTimeMinutes: Int = 0,
    lifetimeOnTimeMinutes: Int = 0
) : Device(deviceID, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes) {
    override fun copy(
        deviceID: Int,
        state: DeviceState,
        name: String,
        type: String,
        power: Float,
        onTimeMinutes: Int,
        lifetimeOnTimeMinutes: Int
    ): SafetyCritical {
        return SafetyCritical(deviceID, maxOnDuration, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes)
    }

    fun copy(
        deviceID: Int = this.deviceID,
        maxOnDuration: Long = this.maxOnDuration,
        state: DeviceState = this.state,
        name: String = this.name,
        type: String = this.type,
        power: Float = this.power,
        onTimeMinutes: Int = this.onTimeMinutes,
        lifetimeOnTimeMinutes: Int = this.lifetimeOnTimeMinutes
    ): SafetyCritical {
        return SafetyCritical(deviceID, maxOnDuration, state, name, type, power, onTimeMinutes, lifetimeOnTimeMinutes)
    }
}
