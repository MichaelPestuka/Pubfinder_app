package com.example.itu

import android.app.LauncherActivity.ListItem
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Resources.getSystem
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.itu.ui.theme.ITUTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


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
                    Text(text = "Edit Meeting")
                    MeeetingEditor(viewModel = viewModel)
                }

            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MeeetingEditor(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
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
    var rowWidth = 1080
    Row(modifier = Modifier.height(96.dp).onGloballyPositioned { rowWidth = it.size.width })
    {
        var offsetLeft by remember { mutableFloatStateOf(0f) }
        var offsetRight by remember { mutableFloatStateOf(0f) }
        var itemSize by remember { mutableFloatStateOf(0f) }
        Spacer(modifier = Modifier.width(offsetLeft.dp / getSystem().displayMetrics.density))
        VerticalDivider(modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    offsetLeft = min(max(0f, offsetLeft + delta), offsetRight)
                    itemSize = offsetRight - offsetLeft
//                    tb.positionChanged = true
                },
                onDragStopped = {  }
            )
            , 6.dp, Color.Gray)
        Card() {  }
        VerticalDivider(modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    offsetRight = min(max(offsetLeft, offsetRight + delta), rowWidth.toFloat())
                    itemSize = offsetRight - offsetLeft
//                    tb.positionChanged = true
                },
                onDragStopped = {  }
            )
            , 6.dp, Color.Gray)
    }

    var timeState = rememberTimePickerState(0, 0, true)
    TimeInput(state = timeState)
    Button(onClick = {viewModel.changeTime(timeState.hour, timeState.minute)}) { Text(text = "Change Start") }
    Button(onClick = {viewModel.changeTime(timeState.hour, timeState.minute, which = "end")}) { Text(text = "Change End") }
    Userselector(viewModel = viewModel)
}

@Composable
fun Userselector(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Card(modifier = modifier)
    {
        Text("Participants: ")
        LazyColumn()
        {
            items(uiState.value.users)
            { user ->
                Card(Modifier.clickable {})
                {
                    Row()
                    {
                        Text(text = user.username, Modifier.padding(4.dp))
                        Checkbox(checked = viewModel.isParticipant(user), onCheckedChange = {viewModel.changeInviteState(user)})
                    }
                }
            }

        }
    }
}

