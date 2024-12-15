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

data class BeerState
    (
    var currentUser: User = User(),
    var beers: MutableList<Beer> = ArrayList(),
    var ratings: MutableList<FavBeer> = ArrayList(),
    )

class BeerViewModel : BaseViewmodel() {

    private val _uiState = MutableStateFlow(BeerState())
    val uiState: StateFlow<BeerState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    /**
     * Fetch all relevant data from server
     */
    override fun fetchData()
    {
        getAllBeers()
        getBeerRatings()
    }

    /**
     * Fetches list of all beers from server
     */
    private fun getAllBeers() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/beer", Array<Beer>::class, "beers")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    beers = result.toMutableList(),
                )
            }
        }
    }

    /**
     * Fetches list of all ratings from server
     */
    private fun getBeerRatings() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO)
            {
                getRequest("/fav_beer", Array<FavBeer>::class, "fav_beers")
            }
            _uiState.update { currentState ->
                currentState.copy(
                    ratings = result.toMutableList(),
                )
            }
        }
    }

    /**
     * Gets current user's rating of a specific beer
     * @param beerId id of beer
     */
    fun getRating(beerId : String) : Float
    {
        val foundRatings = _uiState.value.ratings.filter { it.beer_id == beerId }.filter { it.user_id == _uiState.value.currentUser.id }
        if(foundRatings.isEmpty())
        {
            return 0.0f
        }
        return foundRatings.first().rating.toFloat()
    }

    /**
     * Sets current user's rating of a specific beer
     * @param beerId id of beer
     * @param rating rating of 0 - 5
     */
    fun setRating(beerId: String, rating : Float)
    {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                val foundRatings = _uiState.value.ratings.filter { it.beer_id == beerId }
                    .filter { it.user_id == _uiState.value.currentUser.id }
                if (foundRatings.isEmpty()) {
                    postRequest(
                        "/fav_beer",
                        FavBeer(beer_id = beerId, user_id = _uiState.value.currentUser.id, rating = rating.toString())
                    )
                } else {
                    foundRatings.first().rating = rating.toString()
                    putRequest("/fav_beer/" + _uiState.value.currentUser.id + "/" + beerId, foundRatings.first())

                }
                getBeerRatings()
            }
        }
    }
}