package com.example.androidmqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object MqttProvider {
    lateinit var manager: HiveMqttManager
        private set

    fun initialize(host: String, port: Int, username: String, password: String) {
        if (!::manager.isInitialized) {
            manager = HiveMqttManager(host, port, username, password)
            manager.connect()
        }
    }
}

class HiveMqttManager(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String
) {
    private lateinit var client: Mqtt5AsyncClient
    private var connectionCallback: (() -> Unit)? = null
    private var isClientConnected = false

    fun connect() {
        client = MqttClient.builder()
            .useMqttVersion5()
            .identifier("androidClient_${System.currentTimeMillis()}")
            .serverHost(host)
            .serverPort(port)
            .sslWithDefaultConfig()
            .automaticReconnectWithDefaultConfig()
            .buildAsync()

        client.connectWith()
            .simpleAuth()
            .username(username)
            .password(password.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    isClientConnected = true
                    connectionCallback?.invoke()
                } else {
                    throwable.printStackTrace()
                }
            }
    }

    fun onConnected(callback: () -> Unit) {
        connectionCallback = callback
        if (isClientConnected) {
            callback()
        }
    }

    fun subscribe(topic: String, onMessageReceived: (topic: String, jsonData: JSONObject) -> Unit) {
        client.subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                try {
                    val payload = StandardCharsets.UTF_8.decode(publish.payload.get()).toString()
                    val jsonData = JSONObject(payload)
                    onMessageReceived(publish.topic.toString(), jsonData)
                } catch (e: Exception) {}
            }
            .send()
    }

    fun publish(topic: String, dataMap: JSONObject) {
        client.publishWith()
            .topic(topic)
            .payload(dataMap.toString().toByteArray())
            .send()
    }

    fun disconnect() {
        isClientConnected = false
        connectionCallback = null
        client.disconnect()
    }

    fun unsubscribe(topic: String) {
        client.unsubscribeWith()
            .topicFilter(topic)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    throwable.printStackTrace()
                }
            }
    }
}
