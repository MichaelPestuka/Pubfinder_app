package com.example.itu

import java.text.SimpleDateFormat
import java.util.Date

public fun IsoToTime(iso: String) : Date
{
    val formatter = SimpleDateFormat("yyyy-MM-ddTHH:mm:ss")
    return formatter.parse(iso)
}

public fun TimeToIso(date: Date) : String
{
    val formatter = SimpleDateFormat("yyyy-MM-ddTHH:mm:ss")
    return formatter.format(date)
}
