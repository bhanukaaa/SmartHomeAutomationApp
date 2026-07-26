from mqttInterface import MQTTInterface
from devices import Device, SingleUnit, MultiUnit, SafetyCritical, Room
import json


class DeviceManager:
    def __init__(self, mqttInterface: "MQTTInterface"):
        self.currDeviceID = 0
        self.currRoomID = 0
        self.rooms = []
        self.mqttInterface = mqttInterface

    def parseIncomingDevice(self, jsonData):
        deviceType = jsonData.get("type", "")
        name = jsonData.get("name", "")
        tempID = jsonData.get("tempID")

        self.currDeviceID += 1
        assignedID = self.currDeviceID

        match deviceType:
            case "SingleUnit":
                dev = SingleUnit(assignedID, name)
            case "MultiUnit":
                size = jsonData.get("size", 0)
                subUnitsData = jsonData.get("subUnits", [])
                parsedSubUnits = []
                for subData in subUnitsData:
                    parsedSubUnits.append(self.parseIncomingDevice(subData))
                dev = MultiUnit(assignedID, name, size, parsedSubUnits)
            case "SafetyCritical":
                maxOnDuration = jsonData.get("maxOnDuration", 0)
                dev = SafetyCritical(assignedID, name, maxOnDuration)
            case _:
                dev = Device(assignedID, name, deviceType)

        if tempID is not None:
            dev.tempID = tempID

        return dev

    def serializeDevice(self, device):
        encoded = {
            "deviceID": device.deviceID,
            "state": device.state.name,
            "name": device.name,
            "type": device.type
        }

        if hasattr(device, "tempID"):
            encoded["tempID"] = device.tempID

        match device.type:
            case "SafetyCritical":
                encoded["maxOnDuration"] = device.maxOnDuration
            case "MultiUnit":
                encoded["size"] = device.size
                encoded["subUnits"] = [self.serializeDevice(sub) for sub in device.subUnits]

        return encoded

    def serializeRoom(self, room):
        return {
            "roomId": room.roomId,
            "name": room.name,
            "floorName": room.floorName,
            "devices": [self.serializeDevice(dev) for dev in room.devices]
        }

    def handleNewRoom(self, jsonData):
        tempRoomID = jsonData.get("tempRoomID")
        roomName = jsonData.get("name", "")
        floorName = jsonData.get("floorName", "G")

        self.currRoomID += 1
        newRoom = Room(
            roomId=self.currRoomID,
            name=roomName,
            floorName=floorName
        )
        self.rooms.append(newRoom)

        payload = {
            "tempRoomID": tempRoomID,
            "room": self.serializeRoom(newRoom)
        }

        self.mqttInterface.client.publish(
            "newRoom/server",
            json.dumps(payload)
        )

    def handleNewDevice(self, jsonData):
        tempID = jsonData["tempID"]
        targetRoomID = jsonData.get("roomId")
        newDevice = self.parseIncomingDevice(jsonData)

        targetRoom = next(
            (r for r in self.rooms if r.roomId == targetRoomID), None)
        if targetRoom:
            targetRoom.devices.append(newDevice)

        payload = self.serializeDevice(newDevice)
        payload["tempID"] = tempID
        payload["roomId"] = targetRoomID

        self.mqttInterface.client.publish(
            "newDevice/server",
            json.dumps(payload)
        )

    def handleDatasync(self, jsonData):
        requesterID = jsonData["requesterID"]

        payload = {
            "requesterID": requesterID,
            "rooms": [self.serializeRoom(r) for r in self.rooms]
        }

        self.mqttInterface.client.publish(
            "datasync/response",
            json.dumps(payload)
        )

    def findAndToggle(self, deviceID, devices=None):
        if devices is None:
            devices = [dev for room in self.rooms for dev in room.devices]

        for device in devices:
            if device.deviceID == deviceID:
                device.toggle()

                payload = {
                    "deviceID": device.deviceID,
                    "status": "success",
                    "action": "toggle",
                    "state": device.state.name
                }
                self.mqttInterface.client.publish(
                    "statusUpdate",
                    json.dumps(payload)
                )
                return True

            if device.type == "MultiUnit":
                if self.findAndToggle(deviceID, device.subUnits):
                    return True
        return False

    def handleDeviceAction(self, jsonData):
        deviceID = jsonData.get("deviceID")
        action = jsonData.get("action", "")

        found = False
        if action == "toggle":
            found = self.findAndToggle(deviceID)

        if not found:
            payload = {
                "deviceID": deviceID,
                "status": "error",
                "action": action
            }
            self.mqttInterface.client.publish(
                "statusUpdate",
                json.dumps(payload)
            )