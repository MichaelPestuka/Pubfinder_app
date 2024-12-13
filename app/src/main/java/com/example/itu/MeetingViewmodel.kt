package com.example.itu

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.itu.com.example.itu.CommonTimeInfo
import com.example.itu.com.example.itu.Meeting
import com.example.itu.com.example.itu.MeetingTime
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
import java.util.Locale.filter
import kotlin.math.min

data class MeetingState
    (
    var meetingID: Int = 0,
    var meetingData: Meeting = Meeting(),
    var pubData: Pub = Pub(),
    var ownerData: User = User(),
    var users: Array<User> = emptyArray<User>(),
    var participants: Array<User> = emptyArray<User>(),
    var meetingTime: MeetingTime = MeetingTime(0f, 0f, false, 0, 0, 0),
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

    fun changeTime(floatTime: Float, which: String = "start")
    {
        if(which == "start")
        {
            val current = LocalDateTime.parse(uiState.value.meetingData.begin)

            uiState.value.meetingData.begin =  ChangeTime(floatTime, current).format(DateTimeFormatter.ISO_DATE_TIME)

        }
        else
        {
            val current = LocalDateTime.parse(uiState.value.meetingData.end)

            uiState.value.meetingData.end =  ChangeTime(floatTime, current).format(DateTimeFormatter.ISO_DATE_TIME)
        }
        putAndFetch()
    }

    fun changeDate(value: Int, type: String)
    {
        uiState.value.meetingData.begin = ChangeDate(value, type, LocalDateTime.parse(uiState.value.meetingData.begin)).format(
            DateTimeFormatter.ISO_DATE_TIME)
        uiState.value.meetingData.end = ChangeDate(value, type, LocalDateTime.parse(uiState.value.meetingData.end)).format(
            DateTimeFormatter.ISO_DATE_TIME)
        putAndFetch()
    }

    fun isParticipant(user: User): Boolean
    {
        if(uiState.value.participants.filter { user.id == it.id }.isEmpty())
        {
            return false
        }
        return true
    }

    fun changeInviteState(user: User)
    {
        if(uiState.value.participants.filter { user.id == it.id }.isNotEmpty())
        {
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO)
                {
                    DeleteRequest("/meeting_participant/" + user.id + "/" + uiState.value.meetingID)
                }
            }
        }
        else
        {
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO)
                {
                    PostRequest("/meeting_participant", MeetingParticipant(user.id, uiState.value.meetingID.toString()))
                }
            }
        }
        fetchData()
    }

    override fun fetchData() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/meeting?id=" + uiState.value.meetingID, Meeting::class, "meeting")
            }
            val pubData = withContext(Dispatchers.IO)
            {
                getRequest("/pub?id=" + result.pub_id, Pub::class, "pub")
            }
            val ownerData = withContext(Dispatchers.IO)
            {
                getRequest("/user?id=" + result.owner_id, User::class, "user")
            }
            val participants = withContext(Dispatchers.IO)
            {
                getRequest("/meeting_participant?meeting_id=" + uiState.value.meetingID, Array<User>::class, "users")
            }
            val users = withContext(Dispatchers.IO)
            {
                getRequest("/user", Array<User>::class, "users")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    meetingData = result,
                    pubData = pubData,
                    ownerData = ownerData,
                    participants = participants,
                    users = users,
                    meetingTime = fullTimeFromIso(result.begin, result.end)
                )
            }
        }
    }

    fun putAndFetch() {
        viewModelScope.launch {
            val ownerData = withContext(Dispatchers.IO)
            {
                PutRequest(
                    "/meeting/" + uiState.value.meetingID,
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

    fun findTime()
    {
        viewModelScope.launch {
            val ownerData = withContext(Dispatchers.IO)
            {
                var ids = emptyArray<String>()
                for (user in _uiState.value.participants) {
                    Log.d("id", user.id)
                    ids += user.id
                }
                val date =
                    LocalDateTime.parse(_uiState.value.meetingData.begin).withHour(0).withMinute(0)
                        .withSecond(0)

                var info = CommonTimeInfo(
                    ids,
                    date.format(DateTimeFormatter.ISO_DATE_TIME),
                    _uiState.value.meetingData.begin
                )
                val time = PostRequest("/get_common_time", info)
//                Log.d("gotten", time)
            }
        }
    }
}