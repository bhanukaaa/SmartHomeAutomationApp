import time
import ssl
import json
import paho.mqtt.client as mqtt

mqttInterface = None
deviceManager = None


def main():
    global deviceManager
    global mqttInterface

    deviceManager = DeviceManager()
    mqttInterface = HiveMQInterface(
        host="9a09cc62f72a432a9a1dd98297bd3f1d.s1.eu.hivemq.cloud",
        port=8883,
        username="pythonServer",
        password="12345678",
        subscriptions=[
            "user/deviceAction",
            "datasync/request",
            "general"
        ]
    )
    mqttInterface.start()

    while True:
        backgroundLoop()


def backgroundLoop():
    statusPayload = {
        "status": "active",
        "timestamp": time.time()
    }
    mqttInterface.client.publish("server/status", json.dumps(statusPayload))

    time.sleep(5)


class Device:
    def __init__(self, deviceID):
        self.deviceID = deviceID
        self.switchedOn = False


class DeviceManager:
    def __init__(self):
        self.currID = 1
        self.devices = []


    def handleDeviceAction(self, data):
        if data["action"] == "create":
            self.addDevice()
        elif data["action"] == "toggle":
            self.toggleDevice(data["deviceID"])


    def handleDataSync(self, data):
        payload = {
            "requesterID": data["requesterID"],
            "syncDevices": []
        }
        for d in self.devices:
            payload["syncDevices"].append(
                {"deviceID": d.deviceID, "switchedOn": d.switchedOn}
            )

        mqttInterface.client.publish(
            "datasync/response",
            json.dumps(payload)
        )


    def addDevice(self):
        self.devices.append(Device(self.currID))
        self.currID += 1

        payload = {
            "action": "create",
            "deviceID": self.devices[-1].deviceID
        }
        mqttInterface.client.publish(
            "server/actionResponse",
            json.dumps(payload)
        )


    def toggleDevice(self, deviceID):
        for device in self.devices:
            if device.deviceID == deviceID:
                device.switchedOn = not device.switchedOn
                payload = {
                    "action": "statusUpdate",
                    "deviceID": device.deviceID,
                    "state": device.switchedOn,
                    "timestamp": time.time()
                }
                mqttInterface.client.publish(
                    "server/actionResponse",
                    json.dumps(payload)
                )


class HiveMQInterface:
    def __init__(self, host, port, username, password, subscriptions):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.subscriptions = subscriptions

        self.client = mqtt.Client(
            callback_api_version=mqtt.CallbackAPIVersion.VERSION2)

        self.client.on_connect = self.onConnect
        self.client.on_message = self.onMessage


    def onMessage(self, client, userdata, msg):
        try:
            payload = msg.payload.decode()
            data = json.loads(payload)
            match msg.topic:
                case "user/deviceAction":
                    deviceManager.handleDeviceAction(data)
                case "datasync/request":
                    deviceManager.handleDataSync(data)

        except Exception:
            print(
                f"received non-JSON message: {msg.payload.decode()} on {msg.topic}")


    def onConnect(self, client, userdata, flags, reasonCode, properties):
        if reasonCode == 0:
            for topic in self.subscriptions:
                self.client.subscribe(topic)


    def start(self):
        self.client.username_pw_set(self.username, self.password)
        self.client.tls_set(tls_version=ssl.PROTOCOL_TLS_CLIENT)
        self.client.connect(self.host, self.port)
        self.client.loop_start()


if __name__ == "__main__":
    main()
