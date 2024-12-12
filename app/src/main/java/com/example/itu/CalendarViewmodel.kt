package com.example.itu

import android.media.Rating
import android.util.Log
import androidx.collection.emptyLongSet
import androidx.compose.material3.TimePickerState
import androidx.core.text.buildSpannedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itu.com.example.itu.CalendarItem
import com.example.itu.com.example.itu.FormattedCalendarItem
import com.example.itu.com.example.itu.Meeting
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass


data class CalendarState
    (
    var currentUser: User = User(),
    var calendarItems: Array<CalendarItem> = emptyArray<CalendarItem>(),
            var formattedCalendarItems: Array<FormattedCalendarItem> = emptyArray()
    )

class CalendarViewModel : BaseViewmodel() {

    private val _uiState = MutableStateFlow(CalendarState())
    val uiState: StateFlow<CalendarState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    override fun fetchData()
    {
        GetCalendarItems()
    }

    public fun GetCalendarItems() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/timetable_item", Array<CalendarItem>::class, "timetable_items")
            }


            _uiState.update { currentState ->
                currentState.copy(
                    calendarItems = result,
                    formattedCalendarItems = FormatCalendar(result)
                )
            }
        }
    }

    public fun FormatCalendar(raw: Array<CalendarItem>) : Array<FormattedCalendarItem>
    {
        Log.d("Formatting", "Start")
        var formatted = emptyArray<FormattedCalendarItem>()
        for(item in raw)
        {
            val start = LocalDateTime.parse(item.begin).hour + LocalDateTime.parse(item.begin).minute.toFloat() / 60
            val end = LocalDateTime.parse(item.end).hour  + LocalDateTime.parse(item.end).minute.toFloat() / 60
            val day = LocalDateTime.parse(item.begin).dayOfMonth
            formatted += FormattedCalendarItem(item.id, day, start / 24f, end / 24f, 0f)
//            if(_uiState.value.formattedCalendarItems.filter { it.id == item.id }.isNotEmpty())
//            {
//                formatted.last().positionChanged = true
//            }
        }
        Log.d("Formatting", "End")
        return formatted
    }

    public fun GetDayItems(date: Int) : List<FormattedCalendarItem>
    {
        val dayItems = _uiState.value.formattedCalendarItems.filter { it.day == date }
        return dayItems
    }

    public fun UpdateDayItem(id: String, newStart: Float = 0f, newEnd: Float = 0f, updateEnd: Boolean = true, updateStart: Boolean = true)
    {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                val original = _uiState.value.calendarItems.first { it.id == id }


                if(updateStart) {

                    val startMinutes = (newStart * 24 - (newStart * 24).toInt()) * 60

                    var newStartTime = LocalDateTime.parse(original.begin)

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
                    var newEndTime = LocalDateTime.parse(original.end)

                    original.end = newEndTime.withHour((endHours).toInt())
                        .withMinute(endMinutes.toInt())
                        .format(DateTimeFormatter.ISO_DATE_TIME)
                }

                PutRequest("/timetable_item/" + id, original)
                GetCalendarItems()
            }
        }
    }

    public fun DeleteDayItem(id: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                DeleteRequest("/timetable_item/" + id)
                GetCalendarItems()
            }
        }
    }

    public fun CreateDayItem(month: Int = 0, day: Int, start: Float, end: Float)
    {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                Log.d("creating", start.toString() + " to " + end.toString())
                var startDateTime = LocalDateTime.of(2024, month, day, (start * 24).toInt(), 0).format(DateTimeFormatter.ISO_DATE_TIME)
                var endDateTime = LocalDateTime.of(2024, month, day, (end * 24).toInt(), 0).format(DateTimeFormatter.ISO_DATE_TIME)
                var calendarItem = CalendarItem(begin = startDateTime, end = endDateTime, user_id = _uiState.value.currentUser.id)
                PostRequest("/timetable_item", calendarItem)
                GetCalendarItems()
            }
        }
    }
}