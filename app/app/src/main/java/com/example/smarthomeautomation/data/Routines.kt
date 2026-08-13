package com.example.smarthomeautomation.data

import androidx.collection.IntIntMap
import androidx.collection.intIntMapOf

enum class RoutineState {
    ENABLED,
    DISABLED
}
class Routine(
    var routineID: Int = -1,
    var name: String = "",
    var startTime: String = "",
    var routineState: RoutineState = RoutineState.ENABLED,
    var devices: Map<Int, DeviceState> = emptyMap()

    ) {
    fun copy(
        routineID: Int = this.routineID,
        name: String = this.name,
        startTime: String = this.startTime,
        routineState: RoutineState = this.routineState,
        devices: Map<Int, DeviceState> = this.devices,

    ): Routine {
        return Routine(routineID, name, startTime, routineState, devices)
    }
}