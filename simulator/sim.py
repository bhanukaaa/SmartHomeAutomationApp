import ssl
import json
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
    def __init__(self, deviceID, name="", deviceType=""):
        self.deviceID = deviceID
        self.state = DeviceState.OFF
        self.name = name
        self.type = deviceType

    def toggle(self):
        if self.state == DeviceState.OFF:
            self.state = DeviceState.ON
        elif self.state == DeviceState.ON:
            self.state = DeviceState.OFF

    def toDict(self):
        return {
            "deviceID": self.deviceID,
            "name": self.name,
            "type": self.type,
            "state": self.state.name
        }


class SingleUnit(Device):
    def __init__(self, deviceID, name=""):
        super().__init__(deviceID, name, "SingleUnit")


class MultiUnit(Device):
    def __init__(self, deviceID, name="", size=0, subUnits=None):
        super().__init__(deviceID, name, "MultiUnit")
        self.size = size
        self.subUnits = subUnits if subUnits is not None else []

    def toggleAll(self):
        for unit in self.subUnits:
            unit.toggle()

    def addSubUnit(self, deviceObj):
        self.subUnits.append(deviceObj)

    def toDict(self):
        data = super().toDict()
        data["size"] = self.size
        data["subUnits"] = [unit.toDict() for unit in self.subUnits]
        return data


class SafetyCritical(Device):
    def __init__(self, deviceID, name="", maxOnDuration=0):
        super().__init__(deviceID, name, "SafetyCritical")
        self.maxOnDuration = maxOnDuration

    def toDict(self):
        data = super().toDict()
        data["maxOnDuration"] = self.maxOnDuration
        return data


class DeviceManager:
    def __init__(self, mqttInterface: "MQTTInterface"):
        self.devices = []
        self.mqttInterface = mqttInterface

    def parseDeviceData(self, jsonData):
        deviceID = jsonData.get("deviceID")
        name = jsonData.get("name", "")
        deviceType = jsonData.get("type", "")
        stateStr = jsonData.get("state", "OFF")

        match deviceType:
            case "SingleUnit":
                dev = SingleUnit(deviceID, name)
            case "MultiUnit":
                size = jsonData.get("size", 0)
                subUnitsData = jsonData.get("subUnits", [])
                parsedSubUnits = [self.parseDeviceData(sub) for sub in subUnitsData]
                dev = MultiUnit(deviceID, name, size, parsedSubUnits)
            case "SafetyCritical":
                maxOnDuration = jsonData.get("maxOnDuration", 0)
                dev = SafetyCritical(deviceID, name, maxOnDuration)
            case _:
                dev = Device(deviceID, name, deviceType)

        if stateStr in DeviceState.__members__:
            dev.state = DeviceState[stateStr]

        return dev

    def addNewDevice(self, jsonData):
        newDevice = self.parseDeviceData(jsonData)
        self.devices.append(newDevice)
        socketio.emit('device_added', newDevice.toDict())

    def handleDatasyncResponse(self, jsonData):
        devicesData = jsonData.get("devices", [])
        self.devices = [self.parseDeviceData(d) for d in devicesData]
        deviceList = [d.toDict() for d in self.devices]
        socketio.emit('initial_devices', deviceList)

    def findAndSetState(self, devices, deviceID, newStateStr):
        for dev in devices:
            if dev.deviceID == deviceID:
                if newStateStr in DeviceState.__members__:
                    dev.state = DeviceState[newStateStr]
                return dev
            if isinstance(dev, MultiUnit):
                found = self.findAndSetState(dev.subUnits, deviceID, newStateStr)
                if found:
                    return found
        return None

    def handleActionResponse(self, jsonData):
        deviceID = jsonData.get("deviceID")
        action = jsonData.get("action")
        status = jsonData.get("status")
        newStateStr = jsonData.get("state")

        if status and status.lower() == "success":
            if action == "toggle":
                updatedDevice = self.findAndSetState(self.devices, deviceID, newStateStr)
                if updatedDevice:
                    socketio.emit('device_updated', updatedDevice.toDict())


class MQTTInterface:
    def __init__(self, host, port, username, password, subscriptions):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.subscriptions = subscriptions
        self.deviceManager = None

        self.client = mqtt.Client(
            callback_api_version=mqtt.CallbackAPIVersion.VERSION2)
        self.client.on_connect = self.onConnect
        self.client.on_message = self.onMessage

    def setDeviceManager(self, deviceManager):
        self.deviceManager = deviceManager

    def onConnect(self, client, userdata, flags, reasonCode, properties):
        if reasonCode == 0:
            for topic in self.subscriptions:
                self.client.subscribe(topic)
            
            payload = {"requesterID": "simulator"}
            self.client.publish("datasync/request", json.dumps(payload))

    def onMessage(self, client, userdata, msg):
        try:
            payload = msg.payload.decode()
            jsonData = json.loads(payload)
            match msg.topic:
                case "statusUpdate":
                    self.deviceManager.handleActionResponse(jsonData)
                case "newDevice/server":
                    self.deviceManager.addNewDevice(jsonData)
                case "datasync/response":
                    self.deviceManager.handleDatasyncResponse(jsonData)
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
def handleConnect():
    if deviceManager:
        deviceList = [d.toDict() for d in deviceManager.devices]
        emit('initial_devices', deviceList)


def startMqtt():
    global mqttInterface, deviceManager
    mqttInterface = MQTTInterface(
        host="9a09cc62f72a432a9a1dd98297bd3f1d.s1.eu.hivemq.cloud",
        port=8883,
        username="hardwareSimulator",
        password="12345678",
        subscriptions=[
            "datasync/response",
            "statusUpdate",
            "newDevice/server"
        ]
    )
    deviceManager = DeviceManager(mqttInterface)
    mqttInterface.setDeviceManager(deviceManager)
    mqttInterface.start()


if __name__ == "__main__":
    startMqtt()
    socketio.run(app, host="0.0.0.0", port=5000, allow_unsafe_werkzeug=True)