import json
import time
from mqttInterface import MQTTInterface
from devices import Device


class DeviceManager:
    def __init__(self, mqttInterface: "MQTTInterface"):
        self.currID = 1
        self.devices = []
        self.mqttInterface = mqttInterface

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

        self.mqttInterface.client.publish(
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
        self.mqttInterface.client.publish(
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
                self.mqttInterface.client.publish(
                    "server/actionResponse",
                    json.dumps(payload)
                )
