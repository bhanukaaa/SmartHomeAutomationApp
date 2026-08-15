import os
import time
from pathlib import Path
from database import DatabaseManager
from deviceManager import DeviceManager
from mqttInterface import MQTTInterface

mqttInterface = None
deviceManager = None
dbManager = None


envPath = Path(__file__).resolve().parent.parent / ".env"

if envPath.exists():
    with open(envPath, "r", encoding="utf-8") as file:
        for line in file:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            os.environ.setdefault(key.strip(), value.strip().strip("'\""))


def main():
    global dbManager, mqttInterface, deviceManager

    dbPath = os.getenv("DB_PATH", "/data/app.db")
    dbManager = DatabaseManager(dbPath)

    mqttHost = os.getenv("HOST", os.getenv("HOST", "9a09cc62f72a432a9a1dd98297bd3f1d.s1.eu.hivemq.cloud"))
    mqttPort = int(os.getenv("PORT", os.getenv("PORT", 8883)))
    print(mqttHost)
    mqttInterface = MQTTInterface(
        host=mqttHost,
        port=mqttPort,
        username="pythonServer",
        password="12345678",
        subscriptions=[
            "action/user",
            "sync/request",
            "testing"
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
        deviceManager.checkRoutines()

    time.sleep(0.5)


if __name__ == "__main__":
    main()