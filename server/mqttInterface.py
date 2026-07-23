import ssl
import json
import paho.mqtt.client as mqtt


class MQTTInterface:
    def __init__(self, host, port, username, password, subscriptions):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.subscriptions = subscriptions
        self.deviceManager = None

        self.client = mqtt.Client(
            callback_api_version=mqtt.CallbackAPIVersion.VERSION2
        )

        self.client.on_connect = self.onConnect
        self.client.on_message = self.onMessage

    def setDeviceManager(self, deviceManager):
        self.deviceManager = deviceManager

    def onMessage(self, client, userdata, msg):
        try:
            payload = msg.payload.decode()
            jsonData = json.loads(payload)
            match msg.topic:
                case "newDevice/user":
                    self.deviceManager.handleNewDevice(jsonData)
                case "datasync/request":
                    self.deviceManager.handleDatasync(jsonData)
                case _:  # default
                    raise ValueError("Undefined Topic")

        except Exception:
            print(f"Error: {msg.payload.decode()} on {msg.topic}")

    def onConnect(self, client, userdata, flags, reasonCode, properties):
        if reasonCode == 0:
            for topic in self.subscriptions:
                self.client.subscribe(topic)

    def start(self):
        self.client.username_pw_set(self.username, self.password)
        self.client.tls_set(tls_version=ssl.PROTOCOL_TLS_CLIENT)
        self.client.connect(self.host, self.port)
        self.client.loop_start()
