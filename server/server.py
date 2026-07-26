from mqttInterface import MQTTInterface
from deviceManager import DeviceManager
import time


mqttInterface = None
deviceManager = None


def main():
    global mqttInterface
    mqttInterface = MQTTInterface(
        host="9a09cc62f72a432a9a1dd98297bd3f1d.s1.eu.hivemq.cloud",
        port=8883,
        username="pythonServer",
        password="12345678",
        subscriptions=[
            "newDevice/user",
            "datasync/request",
            "deviceAction/user",
            "newRoom/user"
        ]
    )
    mqttInterface.start()

    global deviceManager
    deviceManager = DeviceManager(mqttInterface)
    mqttInterface.setDeviceManager(deviceManager)

    while True:
        backgroundLoop()


def backgroundLoop():
    # statusPayload = {
    #     "status": "active",
    #     "timestamp": time.time()
    # }
    # mqttInterface.client.publish("server/status", json.dumps(statusPayload))

    time.sleep(5)


if __name__ == "__main__":
    main()
