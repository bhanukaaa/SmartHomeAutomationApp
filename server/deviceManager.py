from mqttInterface import MQTTInterface
from devices import Device
import json


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

    def handleDatasync(self, jsonData):
        requesterID = jsonData["requesterID"]

        payload = {
            "requesterID": requesterID,
            "numDevices": len(self.devices),
            "devices": []
        }

        for device in self.devices:
            encoded = {}
            encoded["deviceID"] = device.deviceID
            encoded["state"] = device.state.name
            encoded["name"] = device.name

            match device.type:
                case "SafetyCritical":
                    encoded["maxOnDuration"] = device.maxOnDuration

            payload["devices"].append(encoded)

        self.mqttInterface.client.publish(
            "datasync/response",
            json.dumps(payload)
        )
