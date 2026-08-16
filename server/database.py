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
                    power REAL NOT NULL DEFAULT 0,
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
                    routineState TEXT NOT NULL DEFAULT 'ENABLED' CHECK(routineState IN ('ENABLED', 'DISABLED'))
                );

                CREATE TABLE IF NOT EXISTS routineDevice (
                    routineID INTEGER NOT NULL,
                    deviceID INTEGER NOT NULL,
                    targetState TEXT NOT NULL CHECK(targetState IN ('ON', 'OFF')),
                    PRIMARY KEY (routineID, deviceID),
                    FOREIGN KEY (routineID) REFERENCES routine(routineID) ON DELETE CASCADE,
                    FOREIGN KEY (deviceID) REFERENCES device(deviceID) ON DELETE CASCADE
                );

                CREATE TABLE IF NOT EXISTS deviceLog (
                    logID INTEGER PRIMARY KEY AUTOINCREMENT,
                    deviceID INTEGER NOT NULL,
                    timestamp TEXT NOT NULL,
                    state TEXT NOT NULL CHECK(state IN ('ON', 'OFF', 'ERROR', 'DISCONNECTED')),
                    FOREIGN KEY (deviceID) REFERENCES device(deviceID) ON DELETE CASCADE
                );

                INSERT OR IGNORE INTO room (roomID, name, floorName) VALUES
                    (1, 'Kitchen', 'G'),
                    (2, 'Bathroom', 'G'),
                    (3, 'Study Room', 'G'),
                    (4, 'Garage', 'G'),
                    (5, 'Bedroom 1', '1'),
                    (6, 'Bedroom 2', '1'),
                    (7, 'Bathroom', '1'),
                    (8, 'Laundry Room', '2');

                INSERT OR IGNORE INTO device (deviceID, roomID, parentDeviceID, name, state, type, power, size, maxOnDuration, turnOnTime) VALUES
                    (1, 1, NULL, 'Kettle', 'OFF', 'SafetyCritical', 800, NULL, 15, 0),
                    (2, 1, NULL, 'Fridge', 'ON', 'SingleUnit', 400, NULL, NULL, 0),
                    (3, 1, NULL, 'Air Fryer', 'OFF', 'SafetyCritical', 800, NULL, 20, 0),
                    (4, 1, NULL, 'Light 1', 'OFF', 'SingleUnit', 15, NULL, NULL, 0),
                    (5, 1, NULL, 'Light 2', 'OFF', 'SingleUnit', 15, NULL, NULL, 0),
                    (6, 2, NULL, 'Light', 'OFF', 'SingleUnit', 15, NULL, NULL, 0),
                    (7, 2, NULL, 'Heater', 'OFF', 'SafetyCritical', 900, NULL, 30, 0),
                    (8, 2, NULL, 'Shaver', 'OFF', 'SingleUnit', 50, NULL, NULL, 0),
                    (9, 3, NULL, 'Printer', 'ON', 'SingleUnit', 300, NULL, NULL, 0),
                    (10, 3, NULL, 'PC', 'ON', 'SingleUnit', 750, NULL, NULL, 0),
                    (11, 3, NULL, 'Light Panel', 'OFF', 'MultiUnit', 45, 3, NULL, 0),
                    (12, 3, 11, 'Light 1', 'OFF', 'SingleUnit', 0, NULL, NULL, 0),
                    (13, 3, 11, 'Light 2', 'OFF', 'SingleUnit', 0, NULL, NULL, 0),
                    (14, 3, 11, 'Light 3', 'OFF', 'SingleUnit', 0, NULL, NULL, 0),
                    (15, 3, NULL, 'Fan', 'OFF', 'SingleUnit', 125, NULL, NULL, 0),
                    (16, 3, NULL, 'AC', 'ON', 'SingleUnit', 900, NULL, NULL, 0),
                    (17, 4, NULL, 'Light 1', 'ERROR', 'SingleUnit', 15, NULL, NULL, 0),
                    (18, 4, NULL, 'Light 2', 'DISCONNECTED', 'SingleUnit', 15, NULL, NULL, 0),
                    (19, 4, NULL, 'Car Charger', 'OFF', 'SingleUnit', 7500, NULL, NULL, 0),
                    (20, 5, NULL, 'Light 1', 'ON', 'SingleUnit', 15, NULL, NULL, 0),
                    (21, 5, NULL, 'Light 2', 'OFF', 'SingleUnit', 15, NULL, NULL, 0),
                    (22, 5, NULL, 'Fan', 'OFF', 'SingleUnit', 150, NULL, NULL, 0),
                    (23, 5, NULL, 'AC', 'ON', 'SingleUnit', 950, NULL, NULL, 0),
                    (24, 5, NULL, 'Charging Station', 'ON', 'MultiUnit', 100, 3, NULL, 0),
                    (25, 5, 24, 'Phone', 'OFF', 'SingleUnit', 0, NULL, NULL, 0),
                    (26, 5, 24, 'Watch', 'OFF', 'SingleUnit', 0, NULL, NULL, 0),
                    (27, 5, 24, 'Earbuds', 'OFF', 'SingleUnit', 0, NULL, NULL, 0),
                    (28, 6, NULL, 'Light 1', 'ON', 'SingleUnit', 15, NULL, NULL, 0),
                    (29, 6, NULL, 'Light 2', 'ON', 'SingleUnit', 15, NULL, NULL, 0),
                    (30, 6, NULL, 'Fan', 'OFF', 'SingleUnit', 150, NULL, NULL, 0),
                    (31, 6, NULL, 'AC', 'ON', 'SingleUnit', 875, NULL, NULL, 0),
                    (32, 7, NULL, 'Light 1', 'OFF', 'SingleUnit', 15, NULL, NULL, 0),
                    (33, 7, NULL, 'Light 2', 'OFF', 'SingleUnit', 15, NULL, NULL, 0),
                    (34, 7, NULL, 'Heater', 'OFF', 'SafetyCritical', 800, NULL, 30, 0),
                    (35, 8, NULL, 'Washing Machine', 'OFF', 'SingleUnit', 500, NULL, NULL, 0),
                    (36, 8, NULL, 'Dryer', 'OFF', 'SingleUnit', 300, NULL, NULL, 0),
                    (37, 8, NULL, 'Iron', 'OFF', 'SafetyCritical', 1000, NULL, 25, 0);

                INSERT OR IGNORE INTO routine (routineID, name, startTime, routineState) VALUES
                    (1, 'Leaving House', '00:00', 'ENABLED'),
                    (2, 'All Lights Off', '08:00', 'ENABLED'),
                    (3, 'All Lights On', '21:00', 'ENABLED');

                INSERT OR IGNORE INTO routineDevice (routineID, deviceID, targetState) VALUES
                    (1, 2, 'ON'), (1, 7, 'OFF'), (1, 15, 'OFF'), (1, 16, 'OFF'),
                    (1, 22, 'OFF'), (1, 23, 'OFF'), (1, 30, 'OFF'), (1, 31, 'OFF'),
                    (1, 35, 'OFF'), (1, 36, 'OFF'), (1, 34, 'OFF'), (1, 17, 'ON'),
                    (1, 20, 'ON'), (1, 28, 'ON'), (2, 4, 'OFF'), (2, 5, 'OFF'),
                    (2, 6, 'OFF'), (2, 17, 'OFF'), (2, 18, 'OFF'), (2, 20, 'OFF'),
                    (2, 21, 'OFF'), (2, 28, 'OFF'), (2, 29, 'OFF'), (2, 32, 'OFF'),
                    (2, 11, 'OFF'), (2, 33, 'OFF'), (3, 4, 'ON'), (3, 5, 'ON'),
                    (3, 6, 'ON'), (3, 11, 'ON'), (3, 17, 'ON'), (3, 18, 'ON'),
                    (3, 20, 'ON'), (3, 21, 'ON'), (3, 28, 'ON'), (3, 29, 'ON'),
                    (3, 32, 'ON'), (3, 33, 'ON');
            """)

            cursor.execute("SELECT COUNT(*) FROM deviceLog")
            if cursor.fetchone()[0] == 0:
                logEntries = []
                days = [
                    "2026-08-10", "2026-08-11", "2026-08-12",
                    "2026-08-13", "2026-08-14", "2026-08-15", "2026-08-16"
                ]

                for dayIndex, currentDay in enumerate(days):
                    logEntries.extend([
                        (1, f"{currentDay} 07:15:00", "ON"),
                        (1, f"{currentDay} 07:20:00", "OFF"),
                        (1, f"{currentDay} 17:30:00", "ON"),
                        (1, f"{currentDay} 17:35:00", "OFF"),
                        (4, f"{currentDay} 07:00:00", "ON"),
                        (4, f"{currentDay} 08:00:00", "OFF"),
                        (4, f"{currentDay} 18:30:00", "ON"),
                        (4, f"{currentDay} 22:00:00", "OFF"),
                        (5, f"{currentDay} 18:30:00", "ON"),
                        (5, f"{currentDay} 22:00:00", "OFF"),
                        (6, f"{currentDay} 06:45:00", "ON"),
                        (6, f"{currentDay} 07:15:00", "OFF"),
                        (6, f"{currentDay} 22:30:00", "ON"),
                        (6, f"{currentDay} 22:45:00", "OFF"),
                        (7, f"{currentDay} 06:50:00", "ON"),
                        (7, f"{currentDay} 07:10:00", "OFF"),
                        (8, f"{currentDay} 07:05:00", "ON"),
                        (8, f"{currentDay} 07:15:00", "OFF"),
                        (17, f"{currentDay} 08:00:00", "ON"),
                        (17, f"{currentDay} 08:15:00", "OFF"),
                        (18, f"{currentDay} 18:00:00", "ON"),
                        (18, f"{currentDay} 18:30:00", "OFF"),
                        (19, f"{currentDay} 01:00:00", "ON"),
                        (19, f"{currentDay} 06:00:00", "OFF"),
                        (20, f"{currentDay} 20:00:00", "ON"),
                        (20, f"{currentDay} 23:30:00", "OFF"),
                        (23, f"{currentDay} 00:00:00", "ON"),
                        (23, f"{currentDay} 06:30:00", "OFF"),
                        (24, f"{currentDay} 00:00:00", "ON"),
                        (24, f"{currentDay} 06:00:00", "OFF"),
                        (28, f"{currentDay} 20:30:00", "ON"),
                        (28, f"{currentDay} 23:00:00", "OFF"),
                        (31, f"{currentDay} 00:00:00", "ON"),
                        (31, f"{currentDay} 06:00:00", "OFF"),
                        (32, f"{currentDay} 07:30:00", "ON"),
                        (32, f"{currentDay} 08:00:00", "OFF"),
                        (34, f"{currentDay} 07:35:00", "ON"),
                        (34, f"{currentDay} 07:55:00", "OFF")
                    ])

                    if dayIndex < 5:
                        logEntries.extend([
                            (10, f"{currentDay} 09:00:00", "ON"),
                            (10, f"{currentDay} 18:00:00", "OFF"),
                            (11, f"{currentDay} 09:00:00", "ON"),
                            (11, f"{currentDay} 18:00:00", "OFF"),
                            (16, f"{currentDay} 11:00:00", "ON"),
                            (16, f"{currentDay} 17:00:00", "OFF")
                        ])
                    else:
                        logEntries.extend([
                            (10, f"{currentDay} 13:00:00", "ON"),
                            (10, f"{currentDay} 17:00:00", "OFF")
                        ])

                    if dayIndex in [0, 2, 4, 6]:
                        logEntries.extend([
                            (3, f"{currentDay} 19:00:00", "ON"),
                            (3, f"{currentDay} 19:20:00", "OFF")
                        ])

                    if dayIndex in [1, 3]:
                        logEntries.extend([
                            (9, f"{currentDay} 10:30:00", "ON"),
                            (9, f"{currentDay} 10:45:00", "OFF")
                        ])

                    if dayIndex in [2, 5]:
                        logEntries.extend([
                            (35, f"{currentDay} 09:00:00", "ON"),
                            (35, f"{currentDay} 10:30:00", "OFF"),
                            (36, f"{currentDay} 10:35:00", "ON"),
                            (36, f"{currentDay} 11:35:00", "OFF")
                        ])

                    if dayIndex == 5:
                        logEntries.extend([
                            (37, f"{currentDay} 11:45:00", "ON"),
                            (37, f"{currentDay} 12:15:00", "OFF")
                        ])

                cursor.executemany(
                    "INSERT INTO deviceLog (deviceID, timestamp, state) VALUES (?, ?, ?)",
                    logEntries
                )

            conn.commit()
            conn.close()