from datetime import datetime, timezone

from database import DatabaseManager


class DeviceRepository:
    def __init__(self, dbManager: DatabaseManager):
        self.dbManager = dbManager
        self.activeSafetyDevices = {}
        self.maxOnDurations = {}
        self.deviceRoutines = {}
        self.initializeDAOs()

    def insertDevice(self, deviceData):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            if deviceData["deviceID"] is None:
                cursor.execute(
                    """
                    INSERT INTO device
                    (roomID, parentDeviceID, name, state, type, power, size, maxOnDuration, turnOnTime)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
                    """,
                    (
                        deviceData["roomID"],
                        deviceData["parentDeviceID"],
                        deviceData["name"],
                        deviceData["state"],
                        deviceData["type"],
                        deviceData["power"],
                        deviceData["size"],
                        deviceData["maxOnDuration"],
                    ),
                )
                return cursor.lastrowid
            else:
                cursor.execute(
                    """
                    INSERT INTO device
                    (deviceID, roomID, parentDeviceID, name, state, type, power, size, maxOnDuration, turnOnTime)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                    """,
                    (
                        deviceData["deviceID"],
                        deviceData["roomID"],
                        deviceData["parentDeviceID"],
                        deviceData["name"],
                        deviceData["state"],
                        deviceData["type"],
                        deviceData["power"],
                        deviceData["size"],
                        deviceData["maxOnDuration"],
                    ),
                )
                return deviceData["deviceID"]

    def deleteDevice(self, deviceID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "DELETE FROM device WHERE deviceID = ?", (deviceID,)
            )
            for routine in self.deviceRoutines:
                if deviceID in self.deviceRoutines[routine].targetDevices:
                    self.deviceRoutines[routine].removeDevice(deviceID)

            if deviceID in self.activeSafetyDevices:
                del self.activeSafetyDevices[deviceID]

    def fetchDevicebyID(self, deviceID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM device WHERE deviceID = ?", (deviceID,)
            )
            row = cursor.fetchone()
            return dict(row) if row else None

    @staticmethod
    def parseLogTimestamp(timestampValue):
        if timestampValue is None:
            raise ValueError("device log timestamp is missing")

        if isinstance(timestampValue, (int, float)):
            return datetime.fromtimestamp(float(timestampValue), tz=timezone.utc)

        text = str(timestampValue).strip()
        if not text:
            raise ValueError("device log timestamp is empty")

        if text.endswith("Z"):
            text = text[:-1] + "+00:00"

        try:
            dt = datetime.fromisoformat(text)
        except ValueError:
            try:
                dt = datetime.fromtimestamp(float(text), tz=timezone.utc)
            except ValueError as exc:
                raise ValueError(f"Unsupported device log timestamp format: {timestampValue}") from exc

        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)

        return dt

    def fetchDeviceLifetimeOnTime(self, deviceID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT timestamp, state FROM deviceLog WHERE deviceID = ? ORDER BY timestamp ASC",
                (deviceID,),
            )
            rows = cursor.fetchall()

        totalSeconds = 0.0
        onStartTime = None

        for row in rows:
            logTime = self.parseLogTimestamp(row["timestamp"])
            state = row["state"]

            if state == "ON":
                onStartTime = logTime
            elif state in ("OFF", "ERROR", "DISCONNECTED") and onStartTime is not None:
                totalSeconds += (logTime - onStartTime).total_seconds()
                onStartTime = None

        return totalSeconds

    def generateUsageReport(self):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT deviceID, name FROM device")
            devices = cursor.fetchall()

        report = []
        for device in devices:
            deviceID = device["deviceID"]
            deviceName = device["name"]
            lifetimeOnTime = self.fetchDeviceLifetimeOnTime(deviceID)
            report.append({
                "deviceID": deviceID,
                "name": deviceName,
                "lifetimeOnTime": lifetimeOnTime
            })

        return report

    def fetchSubUnits(self, parentDeviceID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM device WHERE parentDeviceID = ?", (parentDeviceID,)
            )
            return [dict(row) for row in cursor.fetchall()]

    def fetchDevicesByRoomID(self, roomID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM device WHERE roomID = ?", (roomID,)
            )
            return [dict(row) for row in cursor.fetchall()]

    def insertRoom(self, roomName, floorName):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO room (name, floorName) VALUES (?, ?)",
                (roomName, floorName),
            )
            return cursor.lastrowid

    def fetchAllRooms(self):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM room")
            return [dict(row) for row in cursor.fetchall()]

    def updateDeviceState(self, deviceID, newState, turnOnTime=None):
        with self.dbManager.getDBConnection() as conn:
            if turnOnTime is not None:
                conn.execute(
                    "UPDATE device SET state = ?, turnOnTime = ? WHERE deviceID = ?",
                    (newState, turnOnTime, deviceID),
                )
                if newState == "ON":
                    self.activateSafetyDevice(deviceID, turnOnTime)
            else:
                conn.execute(
                    "UPDATE device SET state = ? WHERE deviceID = ?",
                    (newState, deviceID),
                )

    def activateSafetyDevice(self, deviceID, turnOnTime):
        if deviceID not in self.maxOnDurations:
            device = self.fetchDevicebyID(deviceID)
            self.maxOnDurations[deviceID] = device["maxOnDuration"]

        if deviceID in self.activeSafetyDevices:
            del self.activeSafetyDevices[deviceID]

        self.activeSafetyDevices[deviceID] = ActiveSafetyDevice(
            deviceID,
            self.maxOnDurations[deviceID],
            turnOnTime
        )

    def deactivateSafetyDevice(self, deviceID):
        if deviceID not in self.activeSafetyDevices:
            return
        del self.activeSafetyDevices[deviceID]

    def fetchActiveSafetyDevices(self):
        return list(self.activeSafetyDevices.values())

    def fetchAllRoutines(self):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM routine")
            return [dict(row) for row in cursor.fetchall()]

    def fetchEnabledRoutines(self):
        return list(self.deviceRoutines.values())

    def fetchEnabledRoutineByID(self, routineID):
        return self.deviceRoutines.get(routineID, None)

    def fetchRoutineDevices(self, routineID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM routineDevice WHERE routineID = ?", (routineID,)
            )
            return [dict(row) for row in cursor.fetchall()]

    def fetchRoutineByID(self, routineID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM routine WHERE routineID = ?", (routineID,)
            )
            row = cursor.fetchone()
            return dict(row) if row else None

    def updateRoutineLastTrigger(self, routineID, lastTrigger):
        self.deviceRoutines[routineID].lastTrigger = lastTrigger

    def insertRoutine(self, routineName, startTime, routineState):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO routine (name, startTime, routineState) VALUES (?, ?, ?)",
                (routineName, startTime, routineState)
            )
            routineID = cursor.lastrowid
            self.deviceRoutines[routineID] = DeviceRoutine(
                routineID, startTime)
            return routineID

    def addDeviceToRoutine(self, routineID, deviceID, targetState):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO routineDevice (routineID, deviceID, targetState) VALUES (?, ?, ?)",
                (routineID, deviceID, targetState)
            )
            self.deviceRoutines[routineID].addDevice(deviceID, targetState)

    def removeDeviceFromRoutine(self, routineID, deviceID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "DELETE FROM routineDevice WHERE routineID = ? AND deviceID = ?",
                (routineID, deviceID)
            )
            self.deviceRoutines[routineID].removeDevice(deviceID)

    def initializeDAOs(self):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM device WHERE maxOnDuration > 0")
            for row in cursor.fetchall():
                deviceID = row["deviceID"]
                maxOnDuration = row["maxOnDuration"]
                self.maxOnDurations[deviceID] = maxOnDuration

            cursor.execute("SELECT * FROM routine")
            for row in cursor.fetchall():
                routineID = row["routineID"]
                startTime = row["startTime"]
                self.deviceRoutines[routineID] = DeviceRoutine(
                    routineID, startTime)

            for routine in self.deviceRoutines.values():
                routineID = routine.routineID
                cursor.execute(
                    "SELECT * FROM routineDevice WHERE routineID = ?", (routineID,)
                )
                for row in cursor.fetchall():
                    deviceID = row["deviceID"]
                    targetState = row["targetState"]
                    routine.addDevice(deviceID, targetState)

    # TESTING ONLY =========================================================

    def runSQL(self, statement):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(statement)
            return [dict(row) for row in cursor.fetchall()]


class ActiveSafetyDevice:
    def __init__(self, deviceID, maxOnDuration, turnOnTime):
        self.deviceID = deviceID
        self.maxOnDuration = maxOnDuration
        self.turnOnTime = turnOnTime


class DeviceRoutine:
    def __init__(self, routineID, startTime):
        self.routineID = routineID
        self.startTime = startTime
        self.lastTrigger = None
        self.targetDevices = {}
        # self.enabled = True

    def addDevice(self, deviceID, targetState):
        self.targetDevices[deviceID] = targetState

    def removeDevice(self, deviceID):
        if deviceID in self.targetDevices:
            del self.targetDevices[deviceID]

    def setLastTrigger(self, lastTrigger):
        self.lastTrigger = lastTrigger
