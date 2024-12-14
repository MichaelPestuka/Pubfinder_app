package com.example.itu

import com.example.itu.com.example.itu.MeetingTime
import kotlinx.coroutines.handleCoroutineException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date

public fun IsoToFloatTime(iso: String) : Float
{
    return (LocalDateTime.parse(iso).hour + LocalDateTime.parse(iso).minute.toFloat() / 60) / 24
}

public fun IsoTimeString(iso: String): String
{
    try {
//        return (LocalDateTime.parse(iso).hour.toString() + ":" + LocalDateTime.parse(iso).minute.toString())
        return String.format("%02d:%02d", LocalDateTime.parse(iso).hour, LocalDateTime.parse(iso).minute)
    }
    catch (e: DateTimeParseException)
    {
        return "00:00"
    }
}

public fun ChangeTime(floatTime: Float, currentTime: LocalDateTime) : LocalDateTime
{
    var minutes = (floatTime * 24 - (floatTime * 24).toInt()) * 60
    var hours = (floatTime * 24).toInt()
    if(floatTime * 24 >= 23.99f)
    {
        hours = 23
        minutes = 59f
    }
    return currentTime.withHour(hours).withMinute(minutes.toInt())

}

public fun ChangeDate(value: Int, type: String, currentDate: LocalDateTime) : LocalDateTime
{
    if(type == "year")
    {
        return currentDate.withYear(value)
    }
    else if(type == "month")
    {
        if(value > 12 || value < 1)
        {
            return currentDate
        }
        return currentDate.withMonth(value)
    }
    else
    {
        if(value > currentDate.month.length(false) || value < 1)
        {
            return currentDate
        }
        return currentDate.withDayOfMonth(value)
    }
}

public fun fullTimeFromIso(start: String, end: String): MeetingTime
{
    val floatStart = IsoToFloatTime(start)
    val floatEnd = IsoToFloatTime(end)
    val date = LocalDateTime.parse(start)
    return MeetingTime(floatStart, floatEnd, false, date.year, date.month.value, date.dayOfMonth)
}

public fun printISO(iso: String): String
{
    return LocalDateTime.parse(iso).format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
}
