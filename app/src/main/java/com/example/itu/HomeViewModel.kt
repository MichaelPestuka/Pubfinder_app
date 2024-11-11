package com.example.itu

import android.util.Log
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlin.reflect.KClass

data class HomeState
    (
        var currentUser: User = User(),
            var users: Array<User> = emptyArray<User>(),
            var meetings: Array<Meeting> = emptyArray<Meeting>(),
            var pubs: Array<Pub> = emptyArray<Pub>(),


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
                getRequest("http://192.168.0.177:5000/user", Array<User>::class, "users")

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
                getRequest("http://192.168.0.177:5000/meeting", Array<Meeting>::class, "meetings")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    meetings = result,
                )
            }
        }
    }

    public fun GetAllPubs() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("http://192.168.0.177:5000/pub", Array<Pub>::class, "pubs")
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


}