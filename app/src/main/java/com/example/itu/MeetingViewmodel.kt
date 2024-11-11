package com.example.itu

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.itu.com.example.itu.Meeting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Time
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

data class MeetingState
    (
    var meetingID: Int = 0,
    var meetingData: Meeting = Meeting(),
    var pubData: Pub = Pub(),
    var ownerData: User = User(),
    var users: Array<User> = emptyArray<User>(),
            var newTime: String = "0:0",
//    var meetings: Array<Meeting> = emptyArray<Meeting>(),
//    var pubs: Array<Pub> = emptyArray<Pub>()

)

class MeetingViewmodel : BaseViewmodel() {
    private val _uiState = MutableStateFlow(MeetingState())
    val uiState: StateFlow<MeetingState> = _uiState.asStateFlow()

    init
    {

    }

    fun changeTime(hour: Int, minute: Int, seconds: Int = 0, miliseconds: Int = 0, which: String = "start")
    {
        val timeIsoString = String.format("%02d:%02d:%02d.%06d", hour, minute, seconds, miliseconds)
        val dateIsoString = uiState.value.meetingData.begin.split("T")[0] + "T"
        if(which == "start")
        {

            uiState.value.meetingData.begin =  dateIsoString + timeIsoString

        }
        else
        {
            uiState.value.meetingData.end =  dateIsoString + timeIsoString
        }
        putAndFetch()
    }

    override fun fetchData() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("http://192.168.0.177:5000/meeting?id=" + uiState.value.meetingID, Meeting::class, "meeting")
            }
            val pubData = withContext(Dispatchers.IO)
            {
                getRequest("http://192.168.0.177:5000/pub?id=" + result.pub_id, Pub::class, "pub")
            }
            val ownerData = withContext(Dispatchers.IO)
            {
                getRequest("http://192.168.0.177:5000/user?id=" + result.owner_id, User::class, "user")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    meetingData = result,
                    pubData = pubData,
                    ownerData = ownerData
                )
            }
        }
    }

    fun putAndFetch() {
        viewModelScope.launch {
            val ownerData = withContext(Dispatchers.IO)
            {
                PutRequest(
                    "http://192.168.0.177:5000/meeting/" + uiState.value.meetingID,
                    uiState.value.meetingData
                )
                fetchData()
            }


        }
    }
    fun setMeetingID(id: Int)
    {
        uiState.value.meetingID = id
    }
}