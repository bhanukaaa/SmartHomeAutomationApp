package com.example.smarthomeautomation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smarthomeautomation.ui.theme.SmartHomeAutomationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MqttProvider.initialize(
            "04f84ddb10fe41eb88ca98faf3b4b9b0.s1.eu.hivemq.cloud",
            8883,
            "androidClient",
            "12345678"
        )
        enableEdgeToEdge()
        setContent {
            SmartHomeAutomationTheme {
                AppController()
            }
        }
    }
}
