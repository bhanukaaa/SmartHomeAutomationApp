from enum import Enum


class DeviceState(Enum):
    ON = 1
    OFF = 2
    ERROR = 3
    DISCONNECTED = 4


class Device:
    def __init__(self, deviceID):
        self.deviceID = deviceID
        self.state = DeviceState.OFF
        self.name = ""
        self.type = ""

    def toggle(self):
        if self.state == DeviceState.OFF:
            self.state = DeviceState.ON
        elif self.state == DeviceState.ON:
            self.state = DeviceState.OFF


class SingleUnit(Device):
    def __init__(self, deviceID):
        super().__init__(deviceID)
        self.type = "SingleUnit"

        self.description = ""


class MultiUnit(Device):
    def __init__(self, deviceID, size):
        super().__init__(deviceID)
        self.type = "MultiUnit"
        self.size = size
        self.subUnits = []

    def toggleAll(self):
        for unit in self.subUnits:
            unit.toggle()


class SafetyCritical(Device):
    def __init__(self, deviceID, maxOnDuration):
        super().__init__(deviceID)
        self.type = "SafetyCritical"

        self.maxOnDuration = maxOnDuration
