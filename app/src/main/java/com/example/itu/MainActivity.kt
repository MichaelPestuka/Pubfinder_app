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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.example.itu.ui.theme.ITUTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    val viewModel: HomeViewModel by viewModels()
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
                    bottomBar = {
                        Row(modifier = Modifier.padding(bottom = 20.dp))
                        {
                            Button(
                                onClick = {
                                    Log.d("Clicked: ", "Yes...")
                                    val job = GlobalScope.launch(Dispatchers.IO)
                                    {
                                        viewModel.NewMeeting()
                                    }
                                    job.invokeOnCompletion {
                                        Intent(applicationContext, MeetingActivity::class.java).also {
                                            it.putExtra("ExistingMeetingID", viewModel.uiState.value.last_meeting_id + 1)

                                            startActivity(it)
                                        }
                                    }

                                },
                                modifier = Modifier.weight(1f)
                            )
                            {
                                Text("New Meeting", color = Color.Black)
                            }

                            Button(
                                onClick = {Log.d("Clicked: ", "Yes...")
                                    Intent(applicationContext, CalendarActivity::class.java).also {
                                        startActivity(it)
                                    }},
                                modifier = Modifier.weight(1f)
                            )
                            {
                                Text("Calendar", color = Color.Black)
                            }

                            Button(
                                onClick = {

                                    Log.d("Clicked: ", "Yes...")
                                    Intent(applicationContext, BeerActivity::class.java).also {
//                                    it.putExtra("ExistingMeetingID", 1)
                                        startActivity(it)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            {
                                Text("My Beers", color = Color.Black)
                            }
                        }
                    },
                    topBar = {
                        TopAppBar(
                        title = {Text("My meetings")})
                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(                            modifier = Modifier.padding(innerPadding),)
                    {

                        MeetingList(viewModel = viewModel, editMeetingFun = ::editMeeting)

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
    }

    fun editMeeting(id: Int)
    {
        Intent(applicationContext, MeetingActivity::class.java).also {
            it.putExtra("ExistingMeetingID", id)
            startActivity(it)
        }
    }
}

@Composable
fun MeetingList(modifier: Modifier = Modifier, viewModel: HomeViewModel, editMeetingFun: (Int) -> Unit)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(Modifier.padding((16.dp)))
    {
        items(uiState.value.meetings)
        { meeting ->
            Row()
            {
                Card(Modifier.padding(6.dp).clickable {
                    editMeetingFun(meeting.id.toInt())
                }.weight(1f).height(96.dp))
                {
                    Text(text = printISO(meeting.begin), Modifier.padding(4.dp))
                    Text(
                        text = "Organizer: " + viewModel.GetUserByID(meeting.owner_id).username,
                        modifier.padding(4.dp)
                    )
                    Text(
                        text = "Where: " + viewModel.GetPubByID(meeting.pub_id).name,
                        modifier.padding(4.dp)
                    )
                }
                if(uiState.value.confirm_delete == meeting.id)
                {
                    Button(onClick = { viewModel.DeleteMeeting(meeting.id) },
                        modifier = Modifier.padding(6.dp).width(96.dp).height(96.dp),
                        shape = RoundedCornerShape(10)
                    )
                    {
                        Text("Confirm?")
                    }
                }
                else {
                    Button(onClick = { viewModel.ConfirmDeletion(meeting.id) },
                        modifier = Modifier.padding(6.dp).width(96.dp).height(96.dp),
                        shape = RoundedCornerShape(10))
                    {
                        Text("Delete")
                    }
                }
            }
        }
    }
}




