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