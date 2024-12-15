/**
 * @author Michael Peštuka (xpestu01)
 */

package com.example.itu

/**
 * Class holding time block data
 */
class CalendarItem(
    var id: String = "0",
    var begin: String = "0",
    var end: String = "0",
    var name: String = "No name",
    var user_id: String = "0",

)

/**
 * TIme block formatted to be displayed (length in fraction of day) and date parsed from ISO
 */
class FormattedCalendarItem(
    var id: String,
    var day: Int,
    var month: Int,
    var year: Int,
    var start: Float,
    var end: Float,
    var positionChanged : Boolean = false
)