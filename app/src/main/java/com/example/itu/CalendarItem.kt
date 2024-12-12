package com.example.itu.com.example.itu

import java.util.Date

class CalendarItem(
    var id: String = "0",
    var begin: String = "0",
    var end: String = "0",
    var name: String = "No name",
    var user_id: String = "0",
//    var begin_parsed : Date,
//    var end_parsed : Date
)
{
}

class FormattedCalendarItem(
    var id: String,
    var day: Int,
    var start: Float,
    var end: Float,
    var length: Float,
    var positionChanged : Boolean = false
)