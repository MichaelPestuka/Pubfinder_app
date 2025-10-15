/**
 * @author Michael Peštuka (xpestu01)
 */
package com.example.itu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.itu.ui.theme.ITUTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Activity that lists all meetings, is entry point on launch
 */
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
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
                        HorizontalDivider(Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.padding(bottom = 48.dp).padding(horizontal = 8.dp))
                        {
                            // Create new meeting, creates on server then edits
                            Button(
                                onClick = {
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
                            )
                            {
                                Text("New Meeting")
                            }

                            Spacer(Modifier.weight(1f))

                            // open calendar
                            Button(
                                onClick = {
                                    Intent(applicationContext, CalendarActivity::class.java).also {
                                        startActivity(it)
                                    }}
                            )
                            {
                                Text("Calendar")
                            }

                            Spacer(Modifier.weight(1f))

                            // Open beer rating
                            Button(
                                onClick = {
                                    Intent(applicationContext, BeerActivity::class.java).also {
                                        startActivity(it)
                                    }
                                }
                            )
                            {
                                Text("My Beers")
                            }
                        }
                    },
                    topBar = {
                        TopAppBar(
                        title = {Text("My meetings")})
                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // List of meetings
                    Column(modifier = Modifier.padding(innerPadding))
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

/**
 * Component that lists all meetings, opens them for editing when clicked and has a delete button
 */
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

                // Delete and confirm delete buttons
                if(uiState.value.confirm_delete == meeting.id)
                {
                    Button(onClick = { viewModel.DeleteMeeting(meeting.id) },
                        modifier = Modifier.padding(6.dp).width(96.dp).height(96.dp),
                        shape = RoundedCornerShape(10),
                        contentPadding = PaddingValues(8.dp)
                    )
                    {
                        Text("Confirm?")
                    }
                }
                else {
                    Button(onClick = { viewModel.ConfirmDeletion(meeting.id) },
                        modifier = Modifier.padding(6.dp).width(96.dp).height(96.dp),
                        shape = RoundedCornerShape(10),
                        contentPadding = PaddingValues(8.dp)
                    )
                    {
                        Text("Delete")
                    }
                }
            }
        }
    }
}




