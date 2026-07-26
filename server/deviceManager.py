from mqttInterface import MQTTInterface
from devices import Device, SingleUnit, MultiUnit, SafetyCritical
import json


class DeviceManager:
    def __init__(self, mqttInterface: "MQTTInterface"):
        self.currID = 0
        self.devices = []
        self.mqttInterface = mqttInterface

    def parseIncomingDevice(self, jsonData):
        deviceType = jsonData.get("type", "")
        name = jsonData.get("name", "")

        self.currID += 1
        assignedID = self.currID

        match deviceType:
            case "SingleUnit":
                return SingleUnit(assignedID, name)
            case "MultiUnit":
                size = jsonData.get("size", 0)
                subUnitsData = jsonData.get("subUnits", [])
                parsedSubUnits = []
                for subData in subUnitsData:
                    parsedSubUnits.append(self.parseIncomingDevice(subData))
                return MultiUnit(assignedID, name, size, parsedSubUnits)
            case "SafetyCritical":
                maxOnDuration = jsonData.get("maxOnDuration", 0)
                return SafetyCritical(assignedID, name, maxOnDuration)
            case _:
                return Device(assignedID, name, deviceType)

    def serializeDevice(self, device):
        encoded = {
            "deviceID": device.deviceID,
            "state": device.state.name,
            "name": device.name,
            "type": device.type
        }

        match device.type:
            case "SafetyCritical":
                encoded["maxOnDuration"] = device.maxOnDuration
            case "MultiUnit":
                encoded["size"] = device.size
                encoded["subUnits"] = [self.serializeDevice(
                    sub) for sub in device.subUnits]

        return encoded

    def handleNewDevice(self, jsonData):
        tempID = jsonData["tempID"]
        newDevice = self.parseIncomingDevice(jsonData)

        self.devices.append(newDevice)

        payload = self.serializeDevice(newDevice)
        payload["tempID"] = tempID

        self.mqttInterface.client.publish(
            "newDevice/server",
            json.dumps(payload)
        )

    def handleDatasync(self, jsonData):
        requesterID = jsonData["requesterID"]

        payload = {
            "requesterID": requesterID,
            "numDevices": len(self.devices),
            "devices": [self.serializeDevice(dev) for dev in self.devices]
        }

        self.mqttInterface.client.publish(
            "datasync/response",
            json.dumps(payload)
        )

    def findAndToggle(self, devices, deviceID):
        for device in devices:
            if device.deviceID == deviceID:
                device.toggle()
                return device.state.name

            if device.type == "MultiUnit":
                res = self.findAndToggle(device.subUnits, deviceID)
                if res != None:
                    return res
        return None

    def handleDeviceAction(self, jsonData):
        deviceID = jsonData["deviceID"]
        action = jsonData["action"]

        payload = {
            "deviceID": deviceID,
            "status": "Error",
            "action": action
        }

        if action == "toggle":
            newState = self.findAndToggle(self.devices, deviceID)
            if newState != None:
                payload["state"] = newState
                payload["status"] = "success"

        self.mqttInterface.client.publish(
            "statusUpdate",
            json.dumps(payload)
        )