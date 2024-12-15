/**
 * @author Michael Peštuka (xpestu01)
 */

package com.example.itu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.itu.ui.theme.ITUTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * Activity where user rates beers with a 5 star system
 */
class BeerActivity : ComponentActivity() {

    private val viewModel: BeerViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.uiState.collect()
                {
                }
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ITUTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {Text("Beer Ratings")})
                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(                            modifier = Modifier.padding(innerPadding))
                    {
                        BeerRatingElement(viewModel = viewModel)

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
    }


}

/**
 * List of beers and their ratings
 */
@Composable
fun BeerRatingElement(modifier: Modifier = Modifier, viewModel: BeerViewModel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // Search by name bar
    var nameFilter by remember { mutableStateOf("") }
    TextField(value = nameFilter, onValueChange = {nameFilter = it}, label = {Text("Find Beer")}, modifier = Modifier.fillMaxWidth(1f))
    var filteredBeers = uiState.value.beers
    if(nameFilter != "")
    {
        filteredBeers = uiState.value.beers.filter { (it.name.lowercase() + it.degree).contains(nameFilter.lowercase()) }.toMutableList()
    }

    // List of beers
    LazyColumn(Modifier.padding((16.dp)))
    {
        items(filteredBeers)
        { beer ->
            Card(Modifier.padding(6.dp).fillParentMaxWidth(1.0f))
            {
                Row()
                {
                    Text(text = beer.name + " " + beer.degree + "°", Modifier.padding(4.dp))
                    Spacer(Modifier.weight(1f))
                    StarBar(
                        viewModel = viewModel,
                        beerId = beer.id
                    )
                }
            }
        }
    }
}

/**
 * Five star bar that displays and changes ratings
 */
@Composable
fun StarBar(modifier: Modifier = Modifier, viewModel: BeerViewModel, beerId : String)
{
    val rating = viewModel.getRating(beerId)
    Row()
    {
        // Get and display stars using icons
        val filled = rating.roundToInt()
        val unfilled = 5 - filled
        var starValue = 0.0f
        repeat(filled)
        {
            starValue += 1
            val currentValue = starValue
            Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = Color.Yellow, modifier = modifier.clickable { viewModel.setRating(beerId, currentValue) })
        }
        repeat(unfilled)
        {
            starValue += 1
            val currentValue = starValue
            Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = Color.Gray, modifier = modifier.clickable { viewModel.setRating(beerId, currentValue)})
        }
    }
}





