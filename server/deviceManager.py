from mqttInterface import MQTTInterface
from devices import Device
import json
import time

class DeviceManager:
    def __init__(self, mqttInterface: "MQTTInterface"):
        self.currID = 1
        self.devices = []
        self.mqttInterface = mqttInterface

    def handleNewDevice(self, jsonData):
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
