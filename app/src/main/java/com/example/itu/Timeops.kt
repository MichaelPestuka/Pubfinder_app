/**
 * @author Michael Peštuka (xpestu01)
 */
package com.example.itu

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Set of functions for converting time between formats and editing their values
 */

/**
 * Converts ISO to fraction of day i.e. 12:00 -> 0.5f
 */
fun isoToFloatTime(iso: String) : Float
{
    return (LocalDateTime.parse(iso).hour + LocalDateTime.parse(iso).minute.toFloat() / 60) / 24
}

/**
 * Converts ISO to hh:mm
 */
fun isoTimeString(iso: String): String
{
    return try {
        String.format(Locale.ENGLISH,"%02d:%02d", LocalDateTime.parse(iso).hour, LocalDateTime.parse(iso).minute)
    }
    catch (e: DateTimeParseException)
    {
        "00:00"
    }
}

/**
 * returns fraction of day as hh:mm
 */
fun printFloatTime(time: Float): String
{
    val convertedTime = changeTime(time, LocalDateTime.now())
    return String.format(Locale.ENGLISH,"%02d:%02d", convertedTime.hour, convertedTime.minute)

}

/**
 * returns currentTime with hours and minutes changed according to day fraction
 */
fun changeTime(floatTime: Float, currentTime: LocalDateTime) : LocalDateTime
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

/**
 * Changes date of currentDate
 */
fun changeDate(value: Int, type: String, currentDate: LocalDateTime) : LocalDateTime
{
    when (type) {
        "year" -> {
            return currentDate.withYear(value)
        }
        "month" -> {
            if(value > 12 || value < 1) {
                return currentDate
            }
            return currentDate.withMonth(value)
        }
        else -> {
            if(value > currentDate.month.length(false) || value < 1) {
                return currentDate
            }
            return currentDate.withDayOfMonth(value)
        }
    }
}

/**
 * Converts ISO to MeetingTime class format
 */
fun fullTimeFromIso(start: String, end: String): MeetingTime
{
    val floatStart = isoToFloatTime(start)
    val floatEnd = isoToFloatTime(end)
    val date = LocalDateTime.parse(start)
    return MeetingTime(floatStart, floatEnd, false, date.year, date.month.value, date.dayOfMonth)
}

/**
 * Formats ISO to good looking string
 */
fun printISO(iso: String): String
{
    return LocalDateTime.parse(iso).format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
}
