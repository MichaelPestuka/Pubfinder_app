package com.example.itu

import android.media.Rating
import android.util.Log
import androidx.collection.emptyLongSet
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

data class BeerState
    (
    var currentUser: User = User(),
    var beers: Array<Beer> = emptyArray<Beer>(),
    var ratings: Array<FavBeer> = emptyArray<FavBeer>(),
    )

class BeerViewModel : BaseViewmodel() {

    private val _uiState = MutableStateFlow(BeerState())
    val uiState: StateFlow<BeerState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    override fun fetchData()
    {
        GetAllBeers()
        GetBeerRatings(_uiState.value.currentUser)
    }

    public fun GetAllBeers() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/beer", Array<Beer>::class, "beers")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    beers = result,
                )
            }
        }
    }

    public fun GetBeerRatings(user: User) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/fav_beer", Array<FavBeer>::class, "fav beers")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    ratings = result,
                )
            }
        }
    }

    // TODO
    public fun SortRatings(method: String)
    {
        if(method == "rating") {
            _uiState.value.ratings.sortBy { it.rating }
        }
        if(method == "name") {
            _uiState.value.beers.sortBy { it.name }
        }
        if(method == "rating") {
            _uiState.value.ratings.sortBy { it.rating }
        }
    }

    public fun GetRating(beer_id : String) : Float
    {
        val foundRatings = _uiState.value.ratings.filter { it.beer_id == beer_id }.filter { it.user_id == _uiState.value.currentUser.id }
        if(foundRatings.isEmpty())
        {
            return 0.0f
        }
        return foundRatings.first().rating.toFloat()
    }

    public fun SetRating(beer_id: String, rating : Float)
    {
        viewModelScope.launch {
            val ownerData = withContext(Dispatchers.IO)
            {
                val foundRatings = _uiState.value.ratings.filter { it.beer_id == beer_id }
                    .filter { it.user_id == _uiState.value.currentUser.id }
                if (foundRatings.isEmpty()) {
                    PostRequest(
                        "/fav_beer",
                        FavBeer(beer_id = beer_id, user_id = _uiState.value.currentUser.id, rating = rating.toString())
                    )
                } else {
                    foundRatings.first().rating = rating.toString()
                    PutRequest("/fav_beer/" + _uiState.value.currentUser.id + "/" + beer_id, foundRatings.first())

                }
                GetBeerRatings(_uiState.value.currentUser)
            }
        }
//        fetchData()
    }
}