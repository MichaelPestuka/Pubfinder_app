package com.example.itu

import android.app.LauncherActivity.ListItem
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class MainActivity : ComponentActivity() {

    val viewModel: HomeViewModel by viewModels()
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column()
                    {
                        Greeting(
                            name = "aaa",
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel,
                            applicationContext = applicationContext
                        )
                        UserList(viewModel = viewModel)
                        Button(
                            onClick = {
                                Log.d("Clicked: ", "Yes...")
//                                val intent = Intent(applicationContext, MainActivity::class.java)
//                                startActivity(applicationContext, intent, null)
//                                Intent(Intent.ACTION_MAIN).also{
//                                    it.`package` = "com.google.android.youtube"
//                                    startActivity(it)
//                                }
                                Intent(applicationContext, MeetingActivity::class.java).also{
                                    startActivity(it)
                                }
                            }
                            )
                            {
                                Text("Move tto")
                            }
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
fun Greeting(name: String, modifier: Modifier = Modifier, viewModel: HomeViewModel, applicationContext: Context) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = Modifier.padding(16.dp)) {


        Button(
            onClick = {
                        viewModel.PostRequest("http://192.168.0.177:5000/meeting", uiState.value.meetings.first())
            }
        )
        {
            Text("Update")
        }

    }
}



