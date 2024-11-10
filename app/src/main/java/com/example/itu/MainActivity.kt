package com.example.itu

import android.app.LauncherActivity.ListItem
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.itu.ui.theme.ITUTheme
import com.google.gson.Gson
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel: HomeViewModel by viewModels()
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "aaa",
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                    UserList(viewModel = viewModel)
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
}

@Composable
fun UserList(modifier: Modifier = Modifier, viewModel: HomeViewModel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(Modifier.padding((16.dp)))
    {
        items(uiState.value.meetings)
        {
            meeting -> Card(Modifier.padding(12.dp))
            {

                Text(text = meeting.id, Modifier.padding(4.dp))
                Text(text = "Organizer: " + viewModel.GetUserByID(meeting.owner_id).username, modifier.padding(4.dp))
                Text(text = "Where: " + viewModel.GetPubByID(meeting.pub_id).name, modifier.padding(4.dp))
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, viewModel: HomeViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = Modifier.padding(16.dp)) {


        Text(
            text = uiState.value.user.toString(),
            modifier = modifier
        )
        Button(
            onClick = {
                        viewModel.GetUser(1)}
        )
        {
            Text("Update")
        }
        Button(
            onClick = {
                viewModel.GetAllUsers()}
        )
        {
            Text("Get All")
        }
    }
}



