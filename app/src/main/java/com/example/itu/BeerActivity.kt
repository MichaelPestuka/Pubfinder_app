package com.example.itu

import android.app.LauncherActivity.ListItem
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RatingBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.example.itu.ui.theme.ITUTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

class BeerActivity : ComponentActivity() {

    val viewModel: BeerViewModel by viewModels()
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
                    Column(                            modifier = Modifier.padding(innerPadding),)
                    {
                        BeerRatingElement(viewModel = viewModel)

                    }
                    /*
                    LazyColumn {
                        items(people) {
                            ListItem(it)
                        }
                    }
                    */

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
    }


}

@Composable
fun BeerRatingElement(modifier: Modifier = Modifier, viewModel: BeerViewModel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(Modifier.padding((16.dp)))
    {
        items(uiState.value.beers)
        { beer ->
            Card(Modifier.padding(12.dp).fillParentMaxWidth(1.0f))
            {
                Row()
                {
                    Text(text = beer.name, Modifier.padding(4.dp))
                    Spacer(Modifier.weight(1f))
                    StarBar(
                        viewModel = viewModel,
                        beer_id = beer.id
                    )
                }
            }
        }
    }
}

@Composable
fun StarBar(modifier: Modifier = Modifier, viewModel: BeerViewModel, beer_id : String)
{
    val rating = viewModel.GetRating(beer_id)
    Row()
    {
        val filled = rating.roundToInt()
        val unfilled = 5 - filled
        var starValue = 0.0f
        repeat(filled)
        {
            starValue += 1
            val current_value = starValue
            Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = Color.Yellow, modifier = modifier.clickable { viewModel.SetRating(beer_id, current_value) })
        }
        repeat(unfilled)
        {
            starValue += 1
            val current_value = starValue
            Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = Color.Gray, modifier = modifier.clickable { viewModel.SetRating(beer_id, current_value)})
        }
    }
}





