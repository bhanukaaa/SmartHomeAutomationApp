package com.example.smarthomeautomation.data

enum class RoutineState {
    ON,
    OFF
}
class Routine(
    var routineID: Int = -1,
    var state: RoutineState = RoutineState.OFF,
    var name: String = ""
) {
    fun copy(
        routineID: Int = this.routineID,
        state: RoutineState = this.state,
        name: String = this.name
    ): Routine {
        return Routine(routineID, state, name)
    }
}