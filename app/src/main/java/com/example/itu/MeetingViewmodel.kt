/**
 * @author Michael Peštuka (xpestu01)
 */

package com.example.itu

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

data class MeetingState
    (
    var meetingID: Int = 0,
    var meetingData: Meeting = Meeting(),
    var pubData: MutableList<Pub> = ArrayList(),
    var ownerData: User = User(),
    var users: MutableList<User> = ArrayList(),
    var participants: MutableList<User> = ArrayList(),
    var meetingTime: MeetingTime = MeetingTime(0f, 0f, false, 2024, 1, 1),
    var tagData: MutableList<Tag> = ArrayList(),
    var newTime: String = "0:0",
    var bestPubs: MutableList<String> = ArrayList(),
    var beers: MutableList<Beer> = ArrayList(),
    var drinkList: MutableList<DrinkList> = ArrayList()
)

/**
 * Viewmodel for editing a meeting
 */
class MeetingViewmodel : BaseViewmodel() {
    private val _uiState = MutableStateFlow(MeetingState())
    val uiState: StateFlow<MeetingState> = _uiState.asStateFlow()

    /**
     * Changes time
     * @param floatTime Time in fraction of day
     * @param which change start or end
     */
    fun setNewTime(floatTime: Float, which: String = "start")
    {
        if(which == "start")
        {
            val current = LocalDateTime.parse(uiState.value.meetingData.begin)

            uiState.value.meetingData.begin =  changeTime(floatTime, current).format(DateTimeFormatter.ISO_DATE_TIME)

        }
        else
        {
            val current = LocalDateTime.parse(uiState.value.meetingData.end)

            uiState.value.meetingData.end =  changeTime(floatTime, current).format(DateTimeFormatter.ISO_DATE_TIME)
        }
        putAndFetch()
    }

    /**
     * Changes Year, day or month
     */
    fun setNewDate(value: Int, type: String)
    {
        uiState.value.meetingData.begin = changeDate(value, type, LocalDateTime.parse(uiState.value.meetingData.begin)).format(
            DateTimeFormatter.ISO_DATE_TIME)
        uiState.value.meetingData.end = changeDate(value, type, LocalDateTime.parse(uiState.value.meetingData.end)).format(
            DateTimeFormatter.ISO_DATE_TIME)
        putAndFetch()
    }

    /**
     * Checks if a user is participant of meeting
     */
    fun isParticipant(user: User): Boolean
    {
        return uiState.value.participants.any { user.id == it.id }
    }

    /**
     * Flips invite state of user
     */
    fun changeInviteState(user: User)
    {
        if(uiState.value.participants.any { user.id == it.id })
        {
            viewModelScope.launch {
                withContext(Dispatchers.IO)
                {
                    deleteRequest("/meeting_participant/" + user.id + "/" + uiState.value.meetingID)
                }
            }
        }
        else
        {
            viewModelScope.launch {
                withContext(Dispatchers.IO)
                {
                    postRequest("/meeting_participant", MeetingParticipant(user.id, uiState.value.meetingID.toString()))
                }
            }
        }
        fetchData()
    }

    /**
     * Gets all relevant data from server
     */
    override fun fetchData() {
        // Get all info
        val dataFetch = viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/meeting?id=" + uiState.value.meetingID, Meeting::class, "meeting")
            }
            val pubData = withContext(Dispatchers.IO)
            {
                getRequest("/pub", Array<Pub>::class, "pubs")
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
            val tags = withContext(Dispatchers.IO)
            {
                getRequest("/tag", Array<Tag>::class, "tags")
            }
            val beers = withContext(Dispatchers.IO)
            {
                getRequest("/beer", Array<Beer>::class, "beers")
            }
            val drinkList = withContext(Dispatchers.IO)
            {
                getRequest("/pub_drink_list", Array<DrinkList>::class, "pub drink lists")
            }


            _uiState.update { currentState ->
                currentState.copy(
                    meetingData = result,
                    pubData = pubData.toMutableList(),
                    ownerData = ownerData,
                    participants = participants.toMutableList(),
                    users = users.toMutableList(),
                    meetingTime = fullTimeFromIso(result.begin, result.end),
                    tagData = tags.toMutableList(),
                    beers = beers.toMutableList(),
                    drinkList = drinkList.toMutableList(),
                )
            }
        }
        // Get best pub
        viewModelScope.launch {
            dataFetch.join()
            val bestPubs = withContext(Dispatchers.IO)
            {
                var partArray = emptyArray<String>()
                for(id in uiState.value.participants) {
                    partArray += id.id
                }
                val json = postRequest("/pub_selector", PubSelectorInfo(partArray, emptyArray()), true)
                parseJson(json, Array<String>::class, "top 5 pubs")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    bestPubs = bestPubs.toMutableList()
                )
            }
        }
    }

    /**
     * Saves data to server, then fetches
     */
    private fun putAndFetch() {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                putRequest(
                    "/meeting/" + uiState.value.meetingID,
                    uiState.value.meetingData
                )
                fetchData()
            }


        }
    }

    /**
     * Returns beers
     */
    fun getBeers(pubId: String): MutableList<Beer>
    {
        val relevant = uiState.value.drinkList.filter { it.pub_id == pubId }
        val beers = ArrayList<Beer>()
        for(drink in relevant)
        {
            beers += uiState.value.beers.filter { it.id == drink.beer_id }
        }
        return beers
    }

    /**
     * Returns pubs
     */
    fun getPubsByID(ids: MutableList<String>): MutableList<Pub>
    {
        return uiState.value.pubData.filter { it.id in ids }.toMutableList()
    }

    /**
     * Changes pub id and refreshes
     */
    fun changePub(id: String)
    {
        uiState.value.meetingData.pub_id = id
        putAndFetch()
    }

    /**
     * Finds time that fits all users and sets it
     */
    fun findTime()
    {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                var ids = emptyArray<String>()
                for (user in _uiState.value.participants) {
                    ids += user.id
                }
                val date =
                    LocalDateTime.parse(_uiState.value.meetingData.begin).withHour(0).withMinute(0)
                        .withSecond(0)

                val info = CommonTimeInfo(
                    ids,
                    date.format(DateTimeFormatter.ISO_DATE_TIME),
                    _uiState.value.meetingData.begin
                )
                val time = postRequest("/get_common_time", info, true)
                uiState.value.meetingData.begin = parseJson(time, String::class, "start")
                uiState.value.meetingData.end = parseJson(time, String::class, "end")
                putAndFetch()
            }
        }
    }
}