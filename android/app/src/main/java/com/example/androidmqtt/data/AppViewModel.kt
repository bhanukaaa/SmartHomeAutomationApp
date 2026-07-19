package com.example.androidmqtt.data

import androidx.lifecycle.ViewModel
import com.example.androidmqtt.MqttProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import kotlin.random.Random

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUIState())
    val uiState: StateFlow<AppUIState> = _uiState.asStateFlow()
    val sessionID = Random.nextInt()

    init {
        MqttProvider.manager.subscribe("datasync/response") { topic, jsonData ->
            val requesterID = jsonData.getInt("requesterID")
            if (requesterID == sessionID) {
                _uiState.update { currState ->
                    var deviceList: List<Device> = emptyList()


                    var syncList = jsonData.getJSONArray("syncDevices")
                    for (i in 0 until syncList.length()) {
                        val syncDevice = syncList.getJSONObject(i)
                        deviceList += Device(
                            syncDevice.getInt("deviceID"),
                            syncDevice.getBoolean("switchedOn")
                        )
                    }

                    currState.copy(
                        devices = deviceList
                    )
                }
                MqttProvider.manager.unsubscribe("datasync/response")
            }
        }

        MqttProvider.manager.subscribe("server/actionResponse") { topic, jsonData ->
            val action = jsonData.getString("action")

            if (action == "create") {
                val deviceID = jsonData.getInt("deviceID")

                _uiState.update { currState ->
                    var deviceList = currState.devices
                    deviceList += Device(deviceID)
                    currState.copy(
                        devices = deviceList
                    )
                }
            } else if (action == "statusUpdate") {
                val deviceID = jsonData.getInt("deviceID")
                val newState = jsonData.getBoolean("state")

                _uiState.update { currState ->
                    val updatedDevices = currState.devices.map { device ->
                        if (device.deviceID == deviceID) {
                            device.copy(switchedOn = newState)
                        } else device

                    }
                    currState.copy(devices = updatedDevices)
                }
            }
        }

        MqttProvider.manager.onConnected {
            sync()
        }
    }

    fun newDeviceHandler() {
        val payload = JSONObject().apply {
            put("action", "create")
            put("type", "default")
        }
        MqttProvider.manager.publish(
            "user/deviceAction",
            payload
        )
    }

    fun switchDeviceHandler(deviceID: Int) {
        val payload = JSONObject().apply {
            put("action", "toggle")
            put("deviceID", deviceID)
        }
        MqttProvider.manager.publish(
            "user/deviceAction",
            payload
        )
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