/**
 * @author Michael Peštuka (xpestu01)
 */

package com.example.itu

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class CalendarState
    (
    var currentUser: User = User(),
    var calendarItems: MutableList<CalendarItem> = ArrayList(),
    var formattedCalendarItems: MutableList<FormattedCalendarItem> = ArrayList(),
    var displayedCalendarItems: MutableList<FormattedCalendarItem> = ArrayList(),
    var displayedDate: LocalDateTime = LocalDateTime.now()
    )

/**
 * Viewmodel for calendar activity
 */
class CalendarViewModel : BaseViewmodel() {

    private val _uiState = MutableStateFlow(CalendarState())
    val uiState: StateFlow<CalendarState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    override fun fetchData()
    {
        getCalendarItems()
    }

    private fun getCalendarItems() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/timetable_item", Array<CalendarItem>::class, "timetable_items")
            }


            _uiState.update { currentState ->
                currentState.copy(
                    calendarItems = result.toMutableList(),
                    formattedCalendarItems = formatCalendar(result.toMutableList())
                )
            }
            getVisibleItems()
        }
    }

    /**
     * Formats time block to be displayed by UI
     */
    private fun formatCalendar(raw: MutableList<CalendarItem>) : MutableList<FormattedCalendarItem>
    {
        Log.d("Formatting", "Start")
        val formatted = ArrayList<FormattedCalendarItem>()
        for(item in raw)
        {
            val start = LocalDateTime.parse(item.begin).hour.toFloat() + LocalDateTime.parse(item.begin).minute.toFloat() / 60f
            val end = LocalDateTime.parse(item.end).hour.toFloat()  + LocalDateTime.parse(item.end).minute.toFloat() / 60f
            Log.d("formatted to", start.toString())
            val day = LocalDateTime.parse(item.begin).dayOfMonth
            val month = LocalDateTime.parse(item.begin).monthValue
            val year = LocalDateTime.parse(item.begin).year
            formatted += FormattedCalendarItem(item.id, day, month, year, start / 24f, end / 24f, false)
        }
        Log.d("Formatting", "End")
        return formatted
    }

    /**
     * Filters time blocks according to selected date
     */
    private fun getVisibleItems()
    {
        val items = _uiState.value.formattedCalendarItems.filter { it.month == uiState.value.displayedDate.monthValue
                && it.year == uiState.value.displayedDate.year }
        _uiState.update { currentState ->
            currentState.copy(
                displayedCalendarItems = items.toMutableList()
            )
        }
    }

    /**
     * Changes displayed month by value
     */
    fun changeDisplayMonth(value: Int)
    {
        val newDate = uiState.value.displayedDate.plusMonths(value.toLong())
        _uiState.update { currentState ->
            currentState.copy(
                displayedDate = newDate
            )
        }
        getVisibleItems()
    }

    /**
     * Updates time block on server
     */
    fun updateDayItem(id: String, newStart: Float = 0f, newEnd: Float = 0f, updateEnd: Boolean = true, updateStart: Boolean = true)
    {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                val original = _uiState.value.calendarItems.first { it.id == id }


                if(updateStart) {

                    val startMinutes = (newStart * 24 - (newStart * 24).toInt()) * 60

                    val newStartTime = LocalDateTime.parse(original.begin)

                    original.begin = newStartTime.withHour((newStart * 24).toInt())
                        .withMinute(startMinutes.toInt())
                        .format(DateTimeFormatter.ISO_DATE_TIME)

                }
                if(updateEnd) {
                    var endMinutes = (newEnd * 24 - (newEnd * 24).toInt()) * 60
                    var endHours = newEnd * 24
                    if(endHours >= 23.9f)
                    {
                        endHours = 23f
                        endMinutes = 59f
                    }
                    val newEndTime = LocalDateTime.parse(original.end)

                    original.end = newEndTime.withHour((endHours).toInt())
                        .withMinute(endMinutes.toInt())
                        .format(DateTimeFormatter.ISO_DATE_TIME)
                }

                putRequest("/timetable_item/$id", original)
                getCalendarItems()
            }
        }
    }

    /**
     * Deletes time block on server
     */
    fun deleteDayItem(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                deleteRequest("/timetable_item/$id")
                getCalendarItems()
            }
        }
    }

    /**
     * Creates time block on server
     */
    fun createDayItem(day: Int, start: Float, end: Float)
    {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                Log.d("creating", "$start to $end")
                val startDateTime = uiState.value.displayedDate.withDayOfMonth(day).withHour((start * 24).toInt()).format(
                    DateTimeFormatter.ISO_DATE_TIME)
                val endDateTime = uiState.value.displayedDate.withDayOfMonth(day).withHour(1 +(start * 24).toInt()).format(
                    DateTimeFormatter.ISO_DATE_TIME)
                val calendarItem = CalendarItem(begin = startDateTime, end = endDateTime, user_id = _uiState.value.currentUser.id)
                postRequest("/timetable_item", calendarItem)
                getCalendarItems()
            }
        }
    }
}