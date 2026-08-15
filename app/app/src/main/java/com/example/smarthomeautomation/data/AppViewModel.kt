package com.example.smarthomeautomation.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomeautomation.MqttProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUIState())
    val uiState: StateFlow<AppUIState> = _uiState.asStateFlow()
    val sessionID = Random.nextInt()

    init {
        MqttProvider.manager.subscribe("action/server") { topic, jsonData ->
            viewModelScope.launch(Dispatchers.Default) {
                routeCallback(jsonData)
            }
        }

        MqttProvider.manager.subscribe("sync/response") { topic, jsonData ->
            viewModelScope.launch(Dispatchers.Default) {
                dataSyncCallback(jsonData)
            }
        }

        MqttProvider.manager.onConnected {
            sync()
        }
    }

    fun routeCallback(jsonData: JSONObject) {
        try {
            val action = jsonData.getString("action")
            when (action) {
                "newDevice" -> newDeviceCallback(jsonData)
                "newRoom" -> newRoomCallback(jsonData)
                "deviceStatusUpdate" -> statusUpdateCallback(jsonData)
                "newRoutine" -> newRoutineCallback(jsonData)
                "deleteDevice" -> deleteDeviceCallback(jsonData)
                else -> throw Exception("Invalid Callback Action")
            }

        } catch (e: Exception) {
            Log.d("CALLBACK_EXCEPTION", e.message ?: "")
        }
    }

    fun selectFloor(floorName: String) {
        _uiState.update { currState ->
            currState.copy(
                currentFloorName = floorName,
                currentRoomID = null
            )
        }
    }

    fun selectRoom(roomID: Int?) {
        _uiState.update { currState ->
            val selectedRoom = currState.rooms.find { it.roomID == roomID }
            currState.copy(
                currentRoomID = roomID,
                currentFloorName = selectedRoom?.floorName ?: currState.currentFloorName
            )
        }
    }

//    fun selectRoutine(routineID: Int?) {
//        _uiState.update { currState ->
//            val selectedRoutine = currState.routines.find { it.routineID == routineID }
//            currState.copy(
//                currentRoutineID = routineID
//            )
//        }
//    }

    private fun assignTempIDs(device: Device): Device {
        val tempID = Random.nextInt()
        return when (device) {
            is MultiUnit -> {
                val updatedSubUnits = device.subUnits.map { assignTempIDs(it) }.toMutableList()
                device.copy(deviceID = tempID, subUnits = updatedSubUnits)
            }

            is SafetyCritical -> device.copy(deviceID = tempID)
            is SingleUnit -> device.copy(deviceID = tempID)
            else -> device.copy(deviceID = tempID)
        }
    }

    fun addDeviceHandler(device: Device) {
        val targetRoomID = _uiState.value.currentRoomID ?: return
        val newDevice = assignTempIDs(device)

        _uiState.update { currState ->
            val updatedRooms = currState.rooms.map { room ->
                if (room.roomID == targetRoomID) {
                    room.copy(devices = room.devices + newDevice)
                } else room
            }

            val updatedRegistry = currState.deviceRegistry.toMutableMap().apply {
                putAll(registerDeviceIDs(newDevice, targetRoomID))
            }

            currState.copy(rooms = updatedRooms, deviceRegistry = updatedRegistry)
        }

        viewModelScope.launch(Dispatchers.IO) {
            fun serializeDevice(dev: Device): Map<String, Any> {
                val map = mutableMapOf<String, Any>(
                    "tempID" to dev.deviceID,
                    "roomID" to targetRoomID,
                    "name" to dev.name,
                    "type" to dev.type
                )

                when (dev) {
                    is SafetyCritical -> map["maxOnDuration"] = dev.maxOnDuration
                    is MultiUnit -> {
                        map["size"] = dev.size
                        map["subUnits"] = dev.subUnits.map { serializeDevice(it) }
                    }

                    else -> {}
                }
                return map
            }

            val payload = JSONObject(serializeDevice(newDevice)).apply {
                put("action", "newDevice")
            }

            MqttProvider.manager.publish(
                "action/user",
                payload
            )
        }
    }

    fun deleteDeviceHandler(deviceID: Int) {
        val payload = JSONObject().apply {
            put("deviceID", deviceID)
            put("action", "deleteDevice")
        }

        MqttProvider.manager.publish(
            "action/user",
            payload
        )
    }

    fun addRoomHandler(name: String, floorName: String) {
        val tempRoomID = Random.nextInt()
        val finalFloorName = floorName.ifBlank { "G" }

        val newRoom = Room(
            roomID = tempRoomID,
            name = name,
            floorName = finalFloorName,
            devices = emptyList()
        )

        _uiState.update { currState ->
            currState.copy(
                rooms = currState.rooms + newRoom,
                currentFloorName = finalFloorName,
                currentRoomID = tempRoomID
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("tempRoomID", tempRoomID)
                put("name", name)
                put("floorName", finalFloorName)
                put("action", "newRoom")
            }

            MqttProvider.manager.publish(
                "action/user",
                payload
            )
        }
    }

    fun addRoutineHandler(routine: Routine) {
        val tempRoutineID = Random.nextInt()
        var newRoutine = routine.copy(routineID = tempRoutineID)

        _uiState.update { currState ->
            currState.copy(
                routines = currState.routines + newRoutine,
                currentRoutineID = tempRoutineID
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val deviceIDArray = JSONArray()
            val targetStateArray = JSONArray()

            routine.devices.forEach { (deviceId, targetState) ->
                deviceIDArray.put(deviceId)
                targetStateArray.put(targetState.name)
            }

            val payload = JSONObject().apply {
                put("tempRoutineID", tempRoutineID)
                put("name", routine.name)
                put("startTime", routine.startTime)
                put("routineState", routine.routineState.name)
                put("numDevices", routine.devices.size)
                put("deviceIDs", deviceIDArray)
                put("targetStates", targetStateArray)
                put("action", "newRoutine")
            }

            MqttProvider.manager.publish(
                "action/user",
                payload
            )
        }

    }

    fun toggleDeviceHandler(deviceID: Int) {
        val payload = JSONObject().apply {
            put("deviceID", deviceID)
            put("action", "toggleDevice")
        }

        MqttProvider.manager.publish(
            "action/user",
            payload
        )
    }

    fun startRoutineHandler(routineID: Int) {
        val payload = JSONObject().apply {
            put("routineID", routineID)
            put("action", "startRoutine")
        }

        MqttProvider.manager.publish(
            "action/user",
            payload
        )
    }

    fun newDeviceCallback(jsonData: JSONObject) {
        _uiState.update { currState ->
            val updatedMap = currState.deviceRegistry.toMutableMap()

            fun processDeviceMapping(json: JSONObject) {
                val tempID = json.optInt("tempID", -1)
                val newDeviceID = json.optInt("deviceID", -1)

                if (tempID != -1 && newDeviceID != -1) {
                    val targetRoomID = currState.deviceRegistry[tempID]
                    if (targetRoomID != null) {
                        updatedMap.remove(tempID)
                        updatedMap[newDeviceID] = targetRoomID
                    }
                }

                val subUnitsArray = json.optJSONArray("subUnits") ?: JSONArray()
                for (i in 0 until subUnitsArray.length()) {
                    processDeviceMapping(subUnitsArray.getJSONObject(i))
                }
            }

            fun updateDeviceID(device: Device, json: JSONObject): Device {
                val jsonTempID = json.optInt("tempID", -1)
                val jsonDeviceID = json.optInt("deviceID", -1)

                val updatedDevice = if (device.deviceID == jsonTempID && jsonDeviceID != -1) {
                    device.copy(deviceID = jsonDeviceID)
                } else device

                return if (updatedDevice is MultiUnit) {
                    val subUnitsArray = json.optJSONArray("subUnits") ?: JSONArray()
                    val updatedSubUnits = updatedDevice.subUnits.mapIndexed { index, subDevice ->
                        val subJson =
                            if (index < subUnitsArray.length()) subUnitsArray.getJSONObject(index) else JSONObject()
                        updateDeviceID(subDevice, subJson)
                    }.toMutableList()
                    updatedDevice.copy(subUnits = updatedSubUnits)
                } else {
                    updatedDevice
                }
            }

            processDeviceMapping(jsonData)

            val topTempID = jsonData.optInt("tempID", -1)
            val targetRoomID = currState.deviceRegistry[topTempID] ?: return@update currState

            val updatedRooms = currState.rooms.map { room ->
                if (room.roomID != targetRoomID) room
                else room.copy(devices = room.devices.map { updateDeviceID(it, jsonData) })
            }

            currState.copy(rooms = updatedRooms, deviceRegistry = updatedMap)
        }
    }

    private fun parseDevice(json: JSONObject): Device {
        val deviceID = json.getInt("deviceID")
        val name = json.optString("name", "")
        val type = json.optString("type", "")
        val stateStr = json.optString("state", "OFF")
        val state = try {
            DeviceState.valueOf(stateStr)
        } catch (e: Exception) {
            DeviceState.OFF
        }

        return when (type) {
            "SafetyCritical" -> {
                val maxOnDuration = json.optLong("maxOnDuration", 0L)
                SafetyCritical(deviceID, maxOnDuration, state, name, type)
            }

            "MultiUnit" -> {
                val size = json.optInt("size", 0)
                val subUnitsArray = json.optJSONArray("subUnits") ?: JSONArray()
                val subUnits = mutableListOf<Device>()
                for (i in 0 until subUnitsArray.length()) {
                    subUnits.add(parseDevice(subUnitsArray.getJSONObject(i)))
                }
                MultiUnit(deviceID, size, subUnits, state, name, type)
            }

            "SingleUnit" -> {
                SingleUnit(deviceID, state, name, type)
            }

            else -> {
                Device(deviceID, state, name, type)
            }
        }
    }

    private fun registerDeviceIDs(device: Device, roomID: Int): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>()
        map[device.deviceID] = roomID
        if (device is MultiUnit) {
            device.subUnits.forEach { sub ->
                map.putAll(registerDeviceIDs(sub, roomID))
            }
        }
        return map
    }

    fun dataSyncCallback(jsonData: JSONObject) {
        val requesterID = jsonData.getInt("requesterID")
        if (requesterID == sessionID) {
            _uiState.update { currState ->
                val roomList = mutableListOf<Room>()
                val registry = mutableMapOf<Int, Int>()

                val syncRooms = jsonData.optJSONArray("rooms") ?: JSONArray()
                for (i in 0 until syncRooms.length()) {
                    val roomJson = syncRooms.getJSONObject(i)
                    val roomID = roomJson.getInt("roomID")
                    val name = roomJson.optString("name", "")
                    val floorName = roomJson.optString("floorName", "G")

                    val devicesArray = roomJson.optJSONArray("devices") ?: JSONArray()
                    val deviceList = mutableListOf<Device>()

                    for (j in 0 until devicesArray.length()) {
                        val parsedDevice = parseDevice(devicesArray.getJSONObject(j))
                        deviceList.add(parsedDevice)
                        registry.putAll(registerDeviceIDs(parsedDevice, roomID))
                    }

                    roomList.add(Room(roomID, name, floorName, deviceList))
                }

                val routineList = mutableListOf<Routine>()
                val syncRoutines = jsonData.optJSONArray("routines") ?: JSONArray()
                for (i in 0 until syncRoutines.length()) {
                    val routineJson = syncRoutines.getJSONObject(i)

                    val routineID = routineJson.getInt("routineID")
                    val routineName = routineJson.getString("name")
                    val startTime = routineJson.getString("startTime")
                    val routineStateStr = routineJson.getString("routineState")
                    val routineState = try {
                        RoutineState.valueOf(routineStateStr)
                    } catch (e: Exception) {
                        RoutineState.ENABLED
                    }
                    val numDevices = routineJson.getInt("numDevices")

                    val deviceIDArr = routineJson.getJSONArray("devices") ?: JSONArray()
                    val targetStateArr = routineJson.getJSONArray("targetStates") ?: JSONArray()
                    if (deviceIDArr.length() != targetStateArr.length()) {
                        throw Error("Routine Device and Target State arrays do not match")
                    }
                    var routineDeviceMap = mutableMapOf<Int, DeviceState>()
                    for (j in 0 until numDevices) {
                        routineDeviceMap[deviceIDArr.getInt(j)] =
                            DeviceState.valueOf(targetStateArr.getString(j))
                    }

                    routineList.add(
                        Routine(
                            routineID = routineID,
                            name = routineName,
                            startTime = startTime,
                            routineState = routineState,
                            devices = routineDeviceMap
                        )
                    )
                }

                val initialFloorName = roomList.firstOrNull()?.floorName ?: "G"
                val initialRoomID = roomList.firstOrNull()?.roomID

                currState.copy(
                    rooms = roomList,
                    routines = routineList,
                    deviceRegistry = registry,
                    currentFloorName = initialFloorName,
                    currentRoomID = initialRoomID
                )
            }
            MqttProvider.manager.unsubscribe("sync/response")
        }
    }

    fun newRoomCallback(jsonData: JSONObject) {
        val tempRoomID = jsonData.optInt("tempRoomID", -1)
        val roomObj = jsonData.optJSONObject("room") ?: return
        val newRoomID = roomObj.optInt("roomID", -1)

        if (tempRoomID == -1 || newRoomID == -1) return

        _uiState.update { currState ->
            val updatedRooms = currState.rooms.map { room ->
                if (room.roomID == tempRoomID) {
                    room.copy(roomID = newRoomID)
                } else room
            }

            val updatedRegistry = currState.deviceRegistry.mapValues { (_, roomID) ->
                if (roomID == tempRoomID) newRoomID else roomID
            }

            val updatedCurrentRoomID = if (currState.currentRoomID == tempRoomID) {
                newRoomID
            } else currState.currentRoomID

            currState.copy(
                rooms = updatedRooms,
                deviceRegistry = updatedRegistry,
                currentRoomID = updatedCurrentRoomID
            )
        }
    }

    fun newRoutineCallback(jsonData: JSONObject) {
        val tempRoutineID = jsonData.optInt("tempRoutineID", -1)
        val newRoutineID = jsonData.optInt("routineID", -1)

        if (tempRoutineID == -1 || newRoutineID == -1) return

        _uiState.update { currState ->
            val updatedRoutines = currState.routines.map { routine ->
                if (routine.routineID == tempRoutineID) {
                    routine.copy(routineID = newRoutineID)
                } else routine
            }

            val updatedCurrentRoutineID = if (currState.currentRoutineID == tempRoutineID) {
                newRoutineID
            } else currState.currentRoutineID

            currState.copy(
                routines = updatedRoutines,
                currentRoomID = updatedCurrentRoutineID
            )
        }
    }

    fun statusUpdateCallback(jsonData: JSONObject) {
        val deviceID = jsonData.optInt("deviceID", -1)
        if (deviceID == -1) return

        _uiState.update { currState ->
            val targetRoomID = currState.deviceRegistry[deviceID] ?: return@update currState

            fun updateDeviceState(device: Device): Device {
                val updatedDevice = if (device.deviceID == deviceID) {
                    val newStateStr = jsonData.optString("state", "")
                    if (newStateStr.isNotEmpty()) {
                        device.copy(
                            state = try {
                                DeviceState.valueOf(newStateStr)
                            } catch (e: Exception) {
                                device.state
                            }
                        )
                    } else {
                        device
                    }
                } else device

                return if (updatedDevice is MultiUnit) {
                    val updatedSubUnits =
                        updatedDevice.subUnits.map { updateDeviceState(it) }.toMutableList()
                    updatedDevice.copy(subUnits = updatedSubUnits)
                } else {
                    updatedDevice
                }
            }

            val updatedRooms = currState.rooms.map { room ->
                if (room.roomID != targetRoomID) room
                else room.copy(devices = room.devices.map { updateDeviceState(it) })
            }

            currState.copy(rooms = updatedRooms)
        }
    }

    fun deleteDeviceCallback(jsonData: JSONObject) {
        val deviceID = jsonData.optInt("deviceID", -1)
        if (deviceID == -1) return

        _uiState.update { currState ->
            val targetRoomID = currState.deviceRegistry[deviceID] ?: return

            val updatedRooms = currState.rooms.map { room ->
                if (room.roomID != targetRoomID) room
                else room.copy(devices = room.devices.filter { it.deviceID != deviceID })
            }

            val updatedRegistry = currState.deviceRegistry.toMutableMap().apply {
                remove(deviceID)
            }

            currState.copy(rooms = updatedRooms, deviceRegistry = updatedRegistry)
        }
    }

    fun sync() {
        val payload = JSONObject().apply {
            put("requesterID", sessionID)
        }
        MqttProvider.manager.publish(
            "sync/request",
            payload
        )
    }
}
