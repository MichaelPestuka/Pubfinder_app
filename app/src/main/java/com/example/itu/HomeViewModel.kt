package com.example.itu

import android.util.Log
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
            var pubs: Array<Pub> = emptyArray<Pub>()

    )

class HomeViewModel : ViewModel() {

    init {
            GetAllUsers()
        GetAllMeetings()
        GetAllPubs()
        }


    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

//    public fun GetUser(id: Int) {
//        viewModelScope.launch {
//            val result = withContext(Dispatchers.IO)
//            {
//                getRequest("http://192.168.0.177:5000/user?id=" + uiState.value.changed, User::class, "user")
//
//            }
//            _uiState.update { currentState ->
//                currentState.copy(
//                    user = result,
//                    changed = currentState.changed + 1
//                )
//            }
//        }
//    }

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

    private fun <T: Any> getRequest(url: String, resultClass: KClass<T>, dataName: String, getArray: Boolean = false): T {
        val connection = URL(url).openConnection() as HttpURLConnection

        if (connection.responseCode != 200) {
            Log.d("Response: ", connection.responseCode.toString())
        }
        val inputSystem = connection.inputStream
        val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

        val json = Gson().fromJson(inputStreamReader, JsonObject::class.java)

        Log.d("Jason: ", json.toString())
        val parsedData = Gson().fromJson(json.get(dataName), resultClass.java)

        Log.d("Gotten: ", parsedData.toString())
        inputSystem.close()
        inputSystem.close()

        return parsedData
    }
    private fun <T: Any> PostRequest(url: String, sentObject: T)
    {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
    }
}