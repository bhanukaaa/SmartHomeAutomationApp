import os
import sqlite3
import threading

dbPath = os.getenv("DB_PATH", "/data/app.db")
os.makedirs(os.path.dirname(dbPath), exist_ok=True)


class DatabaseManager:
    def __init__(self, dbPath):
        self.dbPath = dbPath
        self.lock = threading.Lock()
        self.initDB()

    def getDBConnection(self):
        conn = sqlite3.connect(self.dbPath)
        conn.row_factory = sqlite3.Row
        return conn

    def initDB(self):
        with self.lock:
            conn = self.getDBConnection()
            cursor = conn.cursor()
            cursor.executescript("""
                CREATE TABLE IF NOT EXISTS room (
                    roomID INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    floorName TEXT NOT NULL DEFAULT 'G'
                );

                CREATE TABLE IF NOT EXISTS device (
                    deviceID INTEGER PRIMARY KEY,
                    roomID INTEGER,
                    parentDeviceID INTEGER,
                    name TEXT NOT NULL,
                    state TEXT NOT NULL DEFAULT 'OFF' CHECK(state IN ('ON', 'OFF', 'ERROR', 'DISCONNECTED')),
                    type TEXT NOT NULL CHECK(type IN ('SingleUnit', 'MultiUnit', 'SafetyCritical')),
                    size INTEGER,
                    maxOnDuration INTEGER,
                    turnOnTime REAL DEFAULT 0,
                    FOREIGN KEY (roomID) REFERENCES room(roomID) ON DELETE SET NULL,
                    FOREIGN KEY (parentDeviceID) REFERENCES device(deviceID) ON DELETE CASCADE
                );

                CREATE TABLE IF NOT EXISTS routine (
                    routineID INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    startTime TEXT NOT NULL,
                    endTime TEXT NOT NULL,
                    daysOfWeek TEXT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS routineDevice (
                    routineID INTEGER NOT NULL,
                    deviceID INTEGER NOT NULL,
                    targetState TEXT NOT NULL CHECK(targetState IN ('ON', 'OFF')),
                    PRIMARY KEY (routineID, deviceID),
                    FOREIGN KEY (routineID) REFERENCES routine(routineID) ON DELETE CASCADE,
                    FOREIGN KEY (deviceID) REFERENCES device(deviceID) ON DELETE CASCADE
                );
            """)
            conn.commit()
            conn.close()