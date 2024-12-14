package com.example.itu.com.example.itu

import java.time.LocalDateTime
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
    var end: Float,
    var editedTime: Boolean = false,
    var year: Int,
    var month: Int,
    var day: Int
)
{

}

class PubSelectorInfo(
    var ids: Array<String>,
    var tag_ids: Array<String>
)

class CommonTimeInfo(
    var ids: Array<String>,
    var start_date: String,
    var start_time: String
)