import ssl
import json
import threading
from enum import Enum
import paho.mqtt.client as mqtt
from flask import Flask, render_template
from flask_socketio import SocketIO, emit

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'
socketio = SocketIO(app, cors_allowed_origins="*")

mqttInterface = None
deviceManager = None

class DeviceState(Enum):
    ON = 1
    OFF = 2
    ERROR = 3
    DISCONNECTED = 4

class Device:
    def __init__(self, deviceID):
        self.deviceID = deviceID
        self.state = DeviceState.OFF
        self.type = "SingleUnit"

    def toggle(self):
        if self.state == DeviceState.OFF:
            self.state = DeviceState.ON
        elif self.state == DeviceState.ON:
            self.state = DeviceState.OFF

    def to_dict(self):
        return {
            "deviceID": self.deviceID,
            "state": self.state.value
        }

class DeviceManager:
    def __init__(self, mqttInterface: "MQTTInterface"):
        self.devices = []
        self.mqttInterface = mqttInterface

    def addNewDevice(self, jsonData):
        deviceID = jsonData["deviceID"]

        newDevice = Device(deviceID)
        self.devices.append(newDevice)

        payload = {"deviceID": self.devices[-1].deviceID}
        self.mqttInterface.client.publish("newDevice/simulator", json.dumps(payload))

        # Push realtime update to browser via WebSockets
        socketio.emit('device_added', newDevice.to_dict())

    def handleActionResponse(self, jsonData):
        deviceID = jsonData["deviceID"]
        action = jsonData.get("action")
        status = jsonData.get("status")

        device = next((d for d in self.devices if d.deviceID == deviceID), None)

        if device is None:
            print(f"Device with ID {deviceID} not found.")
            return

        if status == "success":
            if action == "toggle":
                device.toggle()
                print(f"Device {deviceID} toggled successfully.")
                # Push updated state to browser
                socketio.emit('device_updated', device.to_dict())
            else:
                print(f"Unknown action '{action}' for device {deviceID}.")
        else:
            print(f"Action '{action}' failed for device {deviceID}.")

class MQTTInterface:
    def __init__(self, host, port, username, password, subscriptions):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.subscriptions = subscriptions
        self.deviceManager = None

        self.client = mqtt.Client(callback_api_version=mqtt.CallbackAPIVersion.VERSION2)
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
                case "server/actionResponse":
                    self.deviceManager.handleActionResponse(jsonData)
                case "newDevice/server":
                    self.deviceManager.addNewDevice(jsonData)
                case _:
                    raise ValueError("Undefined Topic")
        except Exception as e:
            print(f"Error processing MQTT message: {e}")

    def start(self):
        self.client.username_pw_set(self.username, self.password)
        self.client.tls_set(tls_version=ssl.PROTOCOL_TLS_CLIENT)
        self.client.connect(self.host, self.port)
        self.client.loop_start()

@app.route('/')
def index():
    return render_template('index.html')

@socketio.on('connect')
def handle_connect():
    if deviceManager:
        device_list = [d.to_dict() for d in deviceManager.devices]
        emit('initial_devices', device_list)

def start_mqtt():
    global mqttInterface, deviceManager
    mqttInterface = MQTTInterface(
        host="04f84ddb10fe41eb88ca98faf3b4b9b0.s1.eu.hivemq.cloud",
        port=8883,
        username="hardwareSimulator",
        password="12345678",
        subscriptions=[
            "datasync/response",
            "server/actionResponse",
            "newDevice/server"
        ]
    )
    deviceManager = DeviceManager(mqttInterface)
    mqttInterface.setDeviceManager(deviceManager)
    mqttInterface.start()

if __name__ == "__main__":
    start_mqtt()
    socketio.run(app, host="127.0.0.1", port=5000, debug=True, allow_unsafe_werkzeug=True)