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

data class HomeState
    (
    var currentUser: User = User(),
    var users: Array<User> = emptyArray<User>(),
    var meetings: Array<Meeting> = emptyArray<Meeting>(),
    var pubs: Array<Pub> = emptyArray<Pub>(),
    var last_meeting_id: Int = 0,
    var confirm_delete: String = ""
    )

class HomeViewModel : BaseViewmodel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
            fetchData()
        }

    override fun fetchData()
    {
        GetAllUsers()
        GetAllMeetings()
        GetAllPubs()
    }

    public fun GetAllUsers() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/user", Array<User>::class, "users")

            }
            _uiState.update { currentState ->
                currentState.copy(
                    users = result,
                )
            }
        }
    }

    public fun GetAllMeetings() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/meeting", Array<Meeting>::class, "meetings")
            }
            var last = 0
            if(result.isEmpty())
            {
                last = 0
            }
            else
            {
                last = result.last().id.toInt()
            }
            Log.d("len meet", result.size.toString())
            _uiState.update { currentState ->
                currentState.copy(
                    meetings = result,
                    last_meeting_id = last,
                    confirm_delete = "0"
                )
            }
        }
    }

    public fun GetAllPubs() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/pub", Array<Pub>::class, "pubs")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    pubs = result,
                )
            }
        }
    }

    public fun GetPubByID(id: String): Pub
    {
        val found_pubs = uiState.value.pubs.filter { it.id == id }
        if(found_pubs.isEmpty())
        {
            return Pub()
        }
        return found_pubs.first()
    }

    public fun GetUserByID(id: String): User
    {
        val found_users = uiState.value.users.filter { it.id == id }
        if(found_users.isEmpty())
        {
            return User()
        }
        return found_users.first()
    }

    public suspend fun NewMeeting()
    {
        var new = Meeting(
            owner_id = uiState.value.currentUser.id,
            begin = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            end = LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME)
        )
        postRequest("/meeting", new)
    }

    public fun ConfirmDeletion(id: String)
    {
        _uiState.update { currentState ->
            currentState.copy(
                confirm_delete = id
            )
        }
    }

    public  fun DeleteMeeting(id: String)
    {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                deleteRequest("/meeting/" + id)
            }
            fetchData()
        }
    }

}