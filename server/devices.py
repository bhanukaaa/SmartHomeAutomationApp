from enum import Enum


class DeviceState(Enum):
    ON = 1
    OFF = 2
    ERROR = 3
    DISCONNECTED = 4


class Device:
    def __init__(self, deviceID, name, deviceType=""):
        self.deviceID = deviceID
        self.state = DeviceState.OFF
        self.name = name
        self.type = deviceType

    def toggle(self):
        if self.state == DeviceState.OFF:
            self.state = DeviceState.ON
        elif self.state == DeviceState.ON:
            self.state = DeviceState.OFF


class SingleUnit(Device):
    def __init__(self, deviceID, name):
        super().__init__(deviceID, name, "SingleUnit")


class MultiUnit(Device):
    def __init__(self, deviceID, name, size, subUnits=[]):
        super().__init__(deviceID, name, "MultiUnit")
        self.size = size
        self.subUnits = subUnits

    def toggleAll(self):
        for unit in self.subUnits:
            unit.toggle()

    def addSubUnit(self, deviceObj):
        self.subUnits.append(deviceObj)


class SafetyCritical(Device):
    def __init__(self, deviceID, name, maxOnDuration):
        super().__init__(deviceID, name, "SafetyCritical")
        self.maxOnDuration = maxOnDuration


class Room:
    def __init__(self, roomId, name, floorName="G", devices=None):
        self.roomId = roomId
        self.name = name
        self.floorName = floorName
        self.devices = devices if devices is not None else []