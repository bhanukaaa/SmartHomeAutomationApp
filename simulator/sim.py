import ssl
import json
import paho.mqtt.client as mqtt
import time
from enum import Enum

mqttInterface = None
deviceManager = None

def main():
    global mqttInterface
    mqttInterface = MQTTInterface(
        host="9a09cc62f72a432a9a1dd98297bd3f1d.s1.eu.hivemq.cloud",
        port=8883,
        username="hardwareSimulator",
        password="12345678",
        subscriptions=[
            "datasync/response",
            "server/actionResponse"
        ]
    )
    mqttInterface.start()

    global deviceManager
    deviceManager = DeviceManager(mqttInterface)
    mqttInterface.setDeviceManager(deviceManager)

    while True:
        backgroundLoop()


def backgroundLoop():
    statusPayload = {
        "status": "active",
        "timestamp": time.time()
    }
    mqttInterface.client.publish("simulator/status", json.dumps(statusPayload))

    time.sleep(5)

class MQTTInterface:
    def __init__(self, host, port, username, password, subscriptions):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.subscriptions = subscriptions
        self.deviceManager = None

        self.client = mqtt.Client(
            callback_api_version=mqtt.CallbackAPIVersion.VERSION2
        )

        self.client.on_connect = self.onConnect
        self.client.on_message = self.onMessage
    
    def setDeviceManager(self, deviceManager):
        self.deviceManager = deviceManager

    def onConnect(self, client, userdata, flags, reasonCode, properties):
        if reasonCode == 0:
            for topic in self.subscriptions:
                self.client.subscribe(topic)

    def onMessage(self, client, userdata, msg):
            try:
                payload = msg.payload.decode()
                jsonData = json.loads(payload)
                match msg.topic:
                    case "datasync/response":
                        self.deviceManager.addNewDevice(jsonData)
                    case "server/actionResponse":
                        self.deviceManager.handleActionResponse(jsonData)
                    case _:  # default
                        raise ValueError("Undefined Topic")
            except Exception:
                        print(f"Error: {msg.payload.decode()} on {msg.topic}")
            

    def start(self):
            self.client.username_pw_set(self.username, self.password)
            self.client.tls_set(tls_version=ssl.PROTOCOL_TLS_CLIENT)
            self.client.connect(self.host, self.port)
            self.client.loop_start()
        
class DeviceManager:
    def __init__(self, mqttInterface: "MQTTInterface"):
        self.currID = 1
        self.devices = []
        self.mqttInterface = mqttInterface

    def addNewDevice(self, jsonData):
        tempID = jsonData["tempID"]

        newDevice = Device(self.currID)
        self.currID += 1

        self.devices.append(newDevice)

        payload = {
            "deviceID": self.devices[-1].deviceID, 
            "tempID": tempID
        }

        self.mqttInterface.client.publish(
            "newDevice/server",
            json.dumps(payload)
        )

    def handleActionResponse(self, jsonData):
        deviceID = jsonData["deviceID"]
        action = jsonData["action"]
        status = jsonData["status"]

        device = next((d for d in self.devices if d.deviceID == deviceID), None)

        if device is None:
            print(f"Device with ID {deviceID} not found.")
            return

        if status == "success":
            if action == "toggle":
                device.toggle()
                print(f"Device {deviceID} toggled successfully.")
            else:
                print(f"Unknown action '{action}' for device {deviceID}.")
        else:
            print(f"Action '{action}' failed for device {deviceID}.")

class DeviceState(Enum):
    ON = 1
    OFF = 2
    ERROR = 3
    DISCONNECTED = 4


class Device:
    def __init__(self, deviceID):
        self.deviceID = deviceID
        self.state = DeviceState.OFF
        self.type = ""

    def toggle(self):
        if self.state == DeviceState.OFF:
            self.state = DeviceState.ON
        elif self.state == DeviceState.ON:
            self.state = DeviceState.OFF


class SingleUnit(Device):
    def __init__(self, deviceID):
        super().__init__(deviceID)

        self.description = ""


class MultiUnit(Device):
    def __init__(self, deviceID, size):
        super().__init__(deviceID)
        self.size = size
        self.subUnits = []

    def toggleAll(self):
        for unit in self.subUnits:
            unit.toggle()


class SafetyCritical(Device):
    def __init__(self, deviceID, maxOnDuration):
        super().__init__(deviceID)

        self.maxOnDuration = maxOnDuration


if __name__ == "__main__":
    main()
