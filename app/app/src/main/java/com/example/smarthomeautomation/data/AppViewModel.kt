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

        MqttProvider.manager.onConnected {
            sync()
        }
    }

    fun addDeviceHandler() {
        val tempID = Random.nextInt()
        val newDevice = Device(tempID)

        _uiState.update { currState ->
            currState.copy(
                devices = currState.devices + newDevice
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("tempID", tempID)
            }

            MqttProvider.manager.publish(
                "newDevice/user",
                payload
            )
        }
    }

    fun newDeviceCallback(jsonData: JSONObject) {
        val tempID = jsonData.optInt("tempID", -1)
        val newDeviceID = jsonData.optInt("deviceID", -1)
        if (tempID == -1 || newDeviceID == -1) return

        _uiState.update { currState ->
            val updatedList = currState.devices.map { device ->
                if (device.deviceID == tempID) {
                    Device(deviceID = newDeviceID)
                } else {
                    device
                }
            }
            currState.copy(devices = updatedList)
        }
    }

    fun dataSyncCallback(jsonData: JSONObject) {
        val requesterID = jsonData.getInt("requesterID")
        if (requesterID == sessionID) {
            _uiState.update { currState ->
                var deviceList: List<Device> = emptyList()

                val syncList = jsonData.getJSONArray("devices")
                for (i in 0 until syncList.length()) {
                    val syncDevice = syncList.getJSONObject(i)
                    deviceList += Device(
                        syncDevice.getInt("deviceID"),
                        DeviceState.valueOf(syncDevice.getString("state"))
                    )
                }

                currState.copy(
                    devices = deviceList
                )
            }
            MqttProvider.manager.unsubscribe("datasync/response")
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
