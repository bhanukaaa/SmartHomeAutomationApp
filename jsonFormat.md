# action/user

actions = [
    newDevice,
    newRoom,
    toggleDevice,

]

{
    "action" : "newDevice",
    "tempID" : ...,
    "roomID" : ...,
    "name" : ...,
    "type" : ...,
}

{
    "action" : "newRoutine",
    "tempRoutineID" : ...,
    "name" : ...,
    "startTime" : "14:30",
    "routineState" : "ENABLED",

    "numDevices" : 4,
    "devices" : [ device IDs ],
    "targetStates" : [ states ]
}



# action/server

actions = [
    newDevice,
    newRoom,
    deviceStatusUpdate
]

{
    "action" : "newDevice",
    "tempID" : ...,
    ...
}

{
    "action" : "newRoom",
}




# sync/request

{
    "requesterID" :...
}


# sync/response

{
    "requesterID" : ...
    "rooms" : [],
    "routines" : [],
}