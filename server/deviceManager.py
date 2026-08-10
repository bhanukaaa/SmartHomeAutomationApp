import json
import time
from database import DatabaseManager
from mqttInterface import MQTTInterface
from deviceRepository import DeviceRepository


class DeviceManager:
    def __init__(self, mqttInterface: MQTTInterface, dbManager: DatabaseManager):
        self.mqttInterface = mqttInterface
        self.repo = DeviceRepository(dbManager)

    def saveIncomingDevice(self, jsonData, roomID=None, parentDeviceID=None):
        deviceData = {
            "deviceID": jsonData.get("deviceID"),
            "type": jsonData.get("type", ""),
            "name": jsonData.get("name", ""),
            "state": jsonData.get("state", "OFF"),
            "size": jsonData.get("size"),
            "maxOnDuration": jsonData.get("maxOnDuration"),
            "roomID": roomID,
            "parentDeviceID": parentDeviceID,
        }

        deviceID = self.repo.insertDevice(deviceData)

        subUnitsData = jsonData.get("subUnits", [])
        parsedSubUnits = []
        for subData in subUnitsData:
            parsedSubUnits.append(
                self.saveIncomingDevice(
                    subData, roomID=roomID, parentDeviceID=deviceID
                )
            )

        devDict = self.repo.fetchDevicebyID(deviceID)
        if "tempID" in jsonData:
            devDict["tempID"] = jsonData["tempID"]
        devDict["subUnits"] = parsedSubUnits
        return devDict

    def serializeDevice(self, deviceRow):
        encoded = {
            "deviceID": deviceRow["deviceID"],
            "state": deviceRow["state"],
            "name": deviceRow["name"],
            "type": deviceRow["type"],
        }

        if "tempID" in deviceRow:
            encoded["tempID"] = deviceRow["tempID"]

        match deviceRow["type"]:
            case "SafetyCritical":
                encoded["maxOnDuration"] = deviceRow["maxOnDuration"]
            case "MultiUnit":
                encoded["size"] = deviceRow["size"]
                subUnits = deviceRow.get("subUnits")
                if subUnits is None:
                    subUnits = self.repo.fetchSubUnits(deviceRow["deviceID"])
                encoded["subUnits"] = [
                    self.serializeDevice(sub) for sub in subUnits
                ]

        return encoded

    def serializeRoom(self, roomRow):
        devices = self.repo.fetchDevicesByRoomID(roomRow["roomID"])
        rootDevices = [d for d in devices if d["parentDeviceID"] is None]
        return {
            "roomID": roomRow["roomID"],
            "name": roomRow["name"],
            "floorName": roomRow["floorName"],
            "devices": [
                self.serializeDevice(dev) for dev in rootDevices
            ]
        }

    def serializeRoutine(self, routineRow):
        devices = self.repo.fetchDevicesForRoutine(routineRow["routineID"])
        return {
            "routineID": routineRow["routineID"],
            "name": routineRow["name"],
            "daysOfWeek": routineRow["daysOfWeek"],
            "startTime": routineRow["startTime"],
            "endTime": routineRow["endTime"],
            "devices": [
                {
                    "deviceID": dev["deviceID"],
                    "targetState": dev["targetState"]
                }
                for dev in devices
            ]
        }

    def handleNewRoom(self, jsonData):
        tempRoomID = jsonData.get("tempRoomID")
        roomName = jsonData.get("name", "")
        floorName = jsonData.get("floorName", "G")

        roomID = self.repo.insertRoom(roomName, floorName)

        roomRow = {
            "roomID": roomID,
            "name": roomName,
            "floorName": floorName
        }
        payload = {
            "tempRoomID": tempRoomID,
            "room": self.serializeRoom(roomRow),
        }

        self.mqttInterface.client.publish(
            "newRoom/server", json.dumps(payload)
        )

    def handleNewDevice(self, jsonData):
        tempID = jsonData["tempID"]
        targetRoomID = jsonData.get("roomID")

        newDeviceRow = self.saveIncomingDevice(jsonData, roomID=targetRoomID)

        payload = self.serializeDevice(newDeviceRow)
        payload["tempID"] = tempID
        payload["roomID"] = targetRoomID

        self.mqttInterface.client.publish(
            "newDevice/server", json.dumps(payload)
        )

    def handleNewRoutine(self, jsonData):
        tempRoutineID = jsonData.get("tempRoutineID")
        routineName = jsonData.get("name", "")
        daysOfWeek = jsonData.get("daysOfWeek", "")
        startTime = jsonData.get("startTime", "")
        endTime = jsonData.get("endTime", "NONE")

        routineID = self.repo.insertRoutine(routineName, daysOfWeek, startTime, endTime)
        routineRow = self.repo.fetchRoutineByID(routineID)

        payload = {
            "tempRoutineID": tempRoutineID,
            "routine": self.serializeRoutine(routineRow)
        }
        self.mqttInterface.client.publish(
            "newRoutine/server", json.dumps(payload)
        )

    def handleRoutineUpdate(self, jsonData):
        routineID = jsonData.get("routineID")
        deviceID = jsonData.get("deviceID")
        targetState = jsonData.get("targetState", "ON")

        self.repo.addDeviceToRoutine(routineID, deviceID, targetState)
        routineRow = self.repo.fetchRoutineByID(routineID)

        payload = self.serializeRoutine(routineRow)
        self.mqttInterface.client.publish(
            "routineUpdate/server", json.dumps(payload)
        )

    def handleDatasync(self, jsonData):
        requesterID = jsonData["requesterID"]

        rooms = self.repo.fetchAllRooms()
        routines = self.repo.fetchAllRoutines()

        payload = {
            "requesterID": requesterID,
            "rooms": [self.serializeRoom(r) for r in rooms],
            "routines": [self.serializeRoutine(rt) for rt in routines]
        }

        self.mqttInterface.client.publish(
            "datasync/response", json.dumps(payload)
        )

    def findAndToggle(self, deviceID):
        device = self.repo.fetchDevicebyID(deviceID)
        if not device:
            return False

        newState = "ON" if device["state"] == "OFF" else "OFF"

        if device["type"] == "SafetyCritical" and newState == "ON":
            self.repo.updateDeviceState(
                deviceID, newState, turnOnTime=time.time())
        else:
            self.repo.updateDeviceState(deviceID, newState)

        payload = {
            "deviceID": deviceID,
            "status": "success",
            "action": "toggle",
            "state": newState,
        }
        self.mqttInterface.client.publish("statusUpdate", json.dumps(payload))
        return True

    def setDeviceState(self, deviceID, targetState):
        device = self.repo.fetchDevicebyID(deviceID)
        if not device or device["state"] == targetState:
            return False

        if device["type"] == "SafetyCritical" and targetState == "ON":
            self.repo.updateDeviceState(deviceID, targetState, turnOnTime=time.time())
        else:
            self.repo.updateDeviceState(deviceID, targetState)

        payload = {
            "deviceID": deviceID,
            "status": "success",
            "action": "setState",
            "state": targetState,
        }
        self.mqttInterface.client.publish("statusUpdate", json.dumps(payload))
        return True

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
                "action": action,
            }
            self.mqttInterface.client.publish(
                "statusUpdate", json.dumps(payload)
            )

    def checkSafetyDevices(self):
        now = time.time()
        safetyDevices = self.repo.fetchActiveSafetyDevices()

        for device in safetyDevices:
            turnOnTime = device.get("turnOnTime") or 0
            maxOnDuration = device.get("maxOnDuration") or 0
            if turnOnTime > 0 and (now - turnOnTime) >= maxOnDuration:
                self.findAndToggle(device["deviceID"])

    def checkRoutines(self):
        currentDay = time.strftime("%a").upper()
        currentTime = time.strftime("%H:%M")

        activeRoutines = self.repo.fetchActiveRoutines(currentDay, currentTime)
        for item in activeRoutines:
            self.setDeviceState(item["deviceID"], item["targetState"])