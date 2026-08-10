import os
import time
from database import DatabaseManager
from deviceManager import DeviceManager
from mqttInterface import MQTTInterface

mqttInterface = None
deviceManager = None
dbManager = None


def main():
    global dbManager, mqttInterface, deviceManager

    dbPath = os.getenv("DB_PATH", "/data/app.db")
    dbManager = DatabaseManager(dbPath)

    mqttInterface = MQTTInterface(
        host="04f84ddb10fe41eb88ca98faf3b4b9b0.s1.eu.hivemq.cloud",
        port=8883,
        username="pythonServer",
        password="12345678",
        subscriptions=[
            "newDevice/user",
            "datasync/request",
            "deviceAction/user",
            "newRoom/user",
        ],
    )
    mqttInterface.start()

    deviceManager = DeviceManager(mqttInterface, dbManager)
    mqttInterface.setDeviceManager(deviceManager)

    while True:
        backgroundLoop()


def backgroundLoop():
    if deviceManager:
        deviceManager.checkSafetyDevices()

    time.sleep(0.5)


if __name__ == "__main__":
    main()