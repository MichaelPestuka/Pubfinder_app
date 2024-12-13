package com.example.itu.com.example.itu

import java.util.Date

class Meeting(
    var id: String = "0",
    var pub_id: String = "0",
    var owner_id: String = "0",
    var begin: String = "0",
    var end: String = "0",
    var user: Array<String> = arrayOf<String>(),
    var pub_name: String = "No name"
)

class MeetingTime(
    var start: Float,
    var end: Float
)