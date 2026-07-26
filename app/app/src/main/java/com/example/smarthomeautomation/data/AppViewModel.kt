package com.example.smarthomeautomation.data

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
        MqttProvider.manager.subscribe("datasync/response") { topic, jsonData ->
            viewModelScope.launch(Dispatchers.Default) {
                dataSyncCallback(jsonData)
            }
        }

        MqttProvider.manager.subscribe("newDevice/server") { topic, jsonData ->
            viewModelScope.launch(Dispatchers.Default) {
                newDeviceCallback(jsonData)
            }
        }

        MqttProvider.manager.subscribe("statusUpdate") { topic, jsonData ->
            viewModelScope.launch(Dispatchers.Default) {
                statusUpdateCallback(jsonData)
            }
        }

        MqttProvider.manager.onConnected {
            sync()
        }
    }

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
        val newDevice = assignTempIDs(device)

        _uiState.update { currState ->
            currState.copy(
                devices = currState.devices + newDevice
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            fun serializeDevice(dev: Device): Map<String, Any> {
                val map = mutableMapOf<String, Any>(
                    "tempID" to dev.deviceID,
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

            val payload = JSONObject(serializeDevice(newDevice))

            MqttProvider.manager.publish(
                "newDevice/user",
                payload
            )
        }
    }

    fun toggleDeviceHandler(deviceID: Int) {
        val payload = JSONObject().apply {
            put("deviceID", deviceID)
            put("action", "toggle")
        }

        MqttProvider.manager.publish(
            "deviceAction/user",
            payload
        )
    }

    fun newDeviceCallback(jsonData: JSONObject) {
        val tempID = jsonData.optInt("tempID", -1)
        val newDeviceID = jsonData.optInt("deviceID", -1)
        if (tempID == -1 || newDeviceID == -1) return

        fun updateDeviceID(device: Device): Device {
            val updatedDevice = if (device.deviceID == tempID) {
                device.copy(deviceID = newDeviceID)
            } else device

            return if (updatedDevice is MultiUnit) {
                val updatedSubUnits =
                    updatedDevice.subUnits.map { updateDeviceID(it) }.toMutableList()
                updatedDevice.copy(subUnits = updatedSubUnits)
            } else {
                updatedDevice
            }
        }

        _uiState.update { currState ->
            val updatedList = currState.devices.map { updateDeviceID(it) }
            currState.copy(devices = updatedList)
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

    fun dataSyncCallback(jsonData: JSONObject) {
        val requesterID = jsonData.getInt("requesterID")
        if (requesterID == sessionID) {
            _uiState.update { currState ->
                val deviceList = mutableListOf<Device>()
                val syncList = jsonData.getJSONArray("devices")
                for (i in 0 until syncList.length()) {
                    val syncDevice = syncList.getJSONObject(i)
                    deviceList.add(parseDevice(syncDevice))
                }

                currState.copy(
                    devices = deviceList
                )
            }
            MqttProvider.manager.unsubscribe("datasync/response")
        }
    }

    fun statusUpdateCallback(jsonData: JSONObject) {
        val deviceID = jsonData.getInt("deviceID")

        fun updateDeviceState(device: Device): Device {
            val updatedDevice = if (device.deviceID == deviceID) {
                device.copy(
                    state = DeviceState.valueOf(jsonData.getString("state"))
                )
            } else device

            return if (updatedDevice is MultiUnit) {
                val updatedSubUnits =
                    updatedDevice.subUnits.map { updateDeviceState(it) }.toMutableList()
                updatedDevice.copy(subUnits = updatedSubUnits)
            } else {
                updatedDevice
            }
        }

        _uiState.update { currState ->
            val updatedList = currState.devices.map { updateDeviceState(it) }
            currState.copy(devices = updatedList)
        }
    }

    fun sync() {
        val payload = JSONObject().apply {
            put("requesterID", sessionID)
        }
        MqttProvider.manager.publish(
            "datasync/request",
            payload
        )
    }
}
