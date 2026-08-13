from database import DatabaseManager


class DeviceRepository:
    def __init__(self, dbManager: DatabaseManager):
        self.dbManager = dbManager

    def insertDevice(self, deviceData):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            if deviceData["deviceID"] is None:
                cursor.execute(
                    """
                    INSERT INTO device
                    (roomID, parentDeviceID, name, state, type, size, maxOnDuration, turnOnTime)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                    """,
                    (
                        deviceData["roomID"],
                        deviceData["parentDeviceID"],
                        deviceData["name"],
                        deviceData["state"],
                        deviceData["type"],
                        deviceData["size"],
                        deviceData["maxOnDuration"],
                    ),
                )
                return cursor.lastrowid
            else:
                cursor.execute(
                    """
                    INSERT INTO device
                    (deviceID, roomID, parentDeviceID, name, state, type, size, maxOnDuration, turnOnTime)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
                    """,
                    (
                        deviceData["deviceID"],
                        deviceData["roomID"],
                        deviceData["parentDeviceID"],
                        deviceData["name"],
                        deviceData["state"],
                        deviceData["type"],
                        deviceData["size"],
                        deviceData["maxOnDuration"],
                    ),
                )
                return deviceData["deviceID"]

    def fetchDevicebyID(self, deviceID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM device WHERE deviceID = ?", (deviceID,)
            )
            row = cursor.fetchone()
            return dict(row) if row else None

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
            else:
                conn.execute(
                    "UPDATE device SET state = ? WHERE deviceID = ?",
                    (newState, deviceID),
                )

    def fetchActiveSafetyDevices(self):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM device WHERE type = 'SafetyCritical' AND state = 'ON'"
            )
            return [dict(row) for row in cursor.fetchall()]

    def fetchAllRoutines(self):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM routine")
            return [dict(row) for row in cursor.fetchall()]

    def fetchEnabledRoutines(self):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM routine WHERE routineState = 'ENABLED'")
            return [dict(row) for row in cursor.fetchall()]

    def fetchRoutineDevices(self, routineID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM routineDevice WHERE routineID = ?", (routineID,)
            )
            return [dict(row) for row in cursor.fetchall()]

    def updateRoutineLastTrigger(self, routineID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "UPDATE routine SET lastTrigger = CURRENT_TIMESTAMP WHERE routineID = ?",
                (routineID,)
            )

    def insertRoutine(self, routineName, startTime, routineState):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO routine (name, startTime, routineState) VALUES (?, ?, ?)",
                (routineName, startTime)
            )
            return cursor.lastrowid

    def addDeviceToRoutine(self, routineID, deviceID, targetState):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO routineDevice (routineID, deviceID, targetState) VALUES (?, ?, ?)",
                (routineID, deviceID, targetState)
            )

    def removeDeviceFromRoutine(self, routineID, deviceID):
        with self.dbManager.getDBConnection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "DELETE FROM routineDevice WHERE routineID = ? AND deviceID = ?",
                (routineID, deviceID)
            )