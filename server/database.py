import os
import threading
import psycopg2
from psycopg2.extras import RealDictCursor

class DatabaseManager:
    def __init__(self):
        self.host = os.environ["DB_HOST"]
        self.port = os.environ["DB_PORT"]
        self.user = os.environ["DB_USER"]
        self.password = os.environ["DB_PASSWORD"]
        self.dbname = os.environ["DB_NAME"]
        
        self.lock = threading.Lock()
        self.initDB()

    def getDBConnection(self):
        conn = psycopg2.connect(
            host=self.host,
            port=self.port,
            user=self.user,
            password=self.password,
            dbname=self.dbname,
            cursor_factory=RealDictCursor
        )
        return conn

    def initDB(self):
        with self.lock:
            conn = self.getDBConnection()
            cursor = conn.cursor()
            
            cursor.execute("""
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
                    logID SERIAL PRIMARY KEY,
                    deviceID INTEGER NOT NULL,
                    timestamp TEXT NOT NULL,
                    state TEXT NOT NULL CHECK(state IN ('ON', 'OFF', 'ERROR', 'DISCONNECTED')),
                    FOREIGN KEY (deviceID) REFERENCES device(deviceID) ON DELETE CASCADE
                );

                INSERT INTO room (roomID, name, floorName) VALUES
                    (1, 'Kitchen', 'G'),
                    (2, 'Bathroom', 'G'),
                    (3, 'Study Room', 'G'),
                    (4, 'Garage', 'G'),
                    (5, 'Bedroom 1', '1'),
                    (6, 'Bedroom 2', '1'),
                    (7, 'Bathroom', '1'),
                    (8, 'Laundry Room', '2')
                ON CONFLICT (roomID) DO NOTHING;

                INSERT INTO device (deviceID, roomID, parentDeviceID, name, state, type, power, size, maxOnDuration, turnOnTime) VALUES
                    (1, 1, NULL, 'Kettle', 'OFF', 'SafetyCritical', 800, NULL, 15, 0),
                    (2, 1, NULL, 'Fridge', 'ON', 'SingleUnit', 400, NULL, NULL, 0)
                ON CONFLICT (deviceID) DO NOTHING;

                INSERT INTO routine (routineID, name, startTime, routineState) VALUES
                    (1, 'Leaving House', '00:00', 'ENABLED'),
                    (2, 'All Lights Off', '08:00', 'ENABLED'),
                    (3, 'All Lights On', '21:00', 'ENABLED')
                ON CONFLICT (routineID) DO NOTHING;

                INSERT INTO routineDevice (routineID, deviceID, targetState) VALUES
                    (1, 2, 'ON'), (1, 7, 'OFF'), (2, 4, 'OFF')
                ON CONFLICT (routineID, deviceID) DO NOTHING;
            """)

            cursor.execute("SELECT COUNT(*) FROM deviceLog")
            if cursor.fetchone()['count'] == 0:
                logEntries = []
                days = ["2026-08-10", "2026-08-11"]
                
                for dayIndex, currentDay in enumerate(days):
                    logEntries.extend([
                        (1, f"{currentDay} 07:15:00", "ON"),
                        (1, f"{currentDay} 07:20:00", "OFF")
                    ])

                cursor.executemany(
                    "INSERT INTO deviceLog (deviceID, timestamp, state) VALUES (%s, %s, %s)",
                    logEntries
                )

            conn.commit()
            cursor.close()
            conn.close()