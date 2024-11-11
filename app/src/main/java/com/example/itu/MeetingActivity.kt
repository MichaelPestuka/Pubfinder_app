package com.example.itu

import android.app.LauncherActivity.ListItem
import android.app.TimePickerDialog
import android.content.Intent
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.itu.ui.theme.ITUTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch


class MeetingActivity : ComponentActivity() {
    val viewModel: MeetingViewmodel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.uiState.collect()
                {

                }
            }
        }
        viewModel.uiState.value.meetingID = intent.getIntExtra("ExistingMeetingID", 0)
        viewModel.fetchData()
        super.onCreate(savedInstanceState)

        setContent {
            ITUTheme {
                Column()
                {
                    Text(text = "HOYA HEYA")
                    meeetingEditor(viewModel = viewModel)
                }

            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun meeetingEditor(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Card()
    {
        Text(text = "ID: " + uiState.value.meetingData.id)
        Text(text = "Owner: " + uiState.value.ownerData.username)
        Text(text = "Pub: " + uiState.value.pubData.name)
        Text(text = "From: " + uiState.value.meetingData.begin)
        Text(text = "To: " + uiState.value.meetingData.end)
        Text(text = "New: " + uiState.value.newTime)
    }
    TextField(
        value = uiState.value.meetingData.owner_id,
        onValueChange = {uiState.value.meetingData.owner_id = it
                        viewModel.putAndFetch()
        }
    )
    var timeState = rememberTimePickerState(0, 0, true)
    TimeInput(state = timeState)
    Button(onClick = {viewModel.changeTime(timeState.hour, timeState.minute)}) { Text(text = "Change Start") }
    Button(onClick = {viewModel.changeTime(timeState.hour, timeState.minute, which = "end")}) { Text(text = "Change End") }
}

