package com.example.itu

import android.app.LauncherActivity.ListItem
import android.app.TimePickerDialog
import android.content.Intent
import android.util.Log
import android.content.res.Resources.getSystem
import android.graphics.drawable.Icon
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
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.itu.ui.theme.ITUTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


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
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).height(2000.dp)
    ) {
        DateSelect(modifier, viewModel)
        TimeSelector(modifier, viewModel)
        UserSelector(viewModel = viewModel)
        PubSelector(viewModel = viewModel)
    }
}

@Composable
fun UserSelector(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var nameFilter by remember { mutableStateOf("") }
    Card(modifier = modifier.fillMaxWidth(1f).height(500.dp))
    {
        TextField(value = nameFilter, onValueChange = {nameFilter = it}, label = {Text("Find User")}, modifier = Modifier.fillMaxWidth(1f))
        Text("Participants: ")
        LazyColumn()
        {
            var filteredUsers = uiState.value.users
            if(nameFilter != "")
            {
                filteredUsers = uiState.value.users.filter { it.username.toLowerCase().contains(nameFilter.toLowerCase()) }.toTypedArray()
            }
            items(filteredUsers)
            { user ->
                Card(Modifier.clickable {viewModel.changeInviteState(user)})
                {
                    Row()
                    {
                        Text(text = user.username, Modifier.padding(4.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = viewModel.isParticipant(user), onCheckedChange = {viewModel.changeInviteState(user)})
                    }
                }
            }

        }
    }
}

@Composable
fun TimeSelector(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = Modifier
        .height(96.dp)
        .fillMaxWidth(1f)
        .padding(horizontal = 32.dp))
    {
        Row()
        {
            repeat(24)
            {
                VerticalDivider()
                Spacer(modifier = Modifier.weight(1f))
            }
            VerticalDivider()
        }

        var rowWidth = 100
        Row(modifier = Modifier

            .onGloballyPositioned { rowWidth = it.size.width })
        {
            var offsetLeft by remember { mutableFloatStateOf(0f) }
            var offsetRight by remember { mutableFloatStateOf(500f) }
            var itemSize by remember { mutableFloatStateOf(0f) }
            Spacer(modifier = Modifier.width(offsetLeft.dp / getSystem().displayMetrics.density)
                .onGloballyPositioned {
                    if (!uiState.value.meetingTime.editedTime) {
                        offsetLeft = rowWidth * uiState.value.meetingTime.start
                        offsetRight = rowWidth * uiState.value.meetingTime.end
                        itemSize = offsetRight - offsetLeft
                    }

                })
            VerticalDivider(modifier
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetLeft = min(max(0f, offsetLeft + delta), offsetRight)
                        itemSize = offsetRight - offsetLeft
                        uiState.value.meetingTime.editedTime = true
                    },
                    onDragStopped = { viewModel.changeTime(offsetLeft / rowWidth, "start") }
                ), 6.dp, Color.Gray)
            Card(modifier = Modifier
                .width(abs(offsetRight - offsetLeft).dp / getSystem().displayMetrics.density)
                .fillMaxHeight(1f)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetLeft = min(max(0f, offsetLeft + delta), rowWidth.toFloat() - itemSize)
                        offsetRight =
                            min(max(0f + itemSize, offsetRight + delta), rowWidth.toFloat())
                        uiState.value.meetingTime.editedTime = true
                    },
                    onDragStopped = {
                        viewModel.changeTime(offsetLeft / rowWidth, "start")
                        viewModel.changeTime(offsetRight / rowWidth, "end")
                    }
                )
                .alpha(0.5f),
                shape = RectangleShape

            )
            {
//                Text(text = (ChangeTime((offsetLeft / 8) / rowWidth, LocalDateTime.now()).format(
//                    DateTimeFormatter.ofPattern("HH:mm"))).toString())
//                Text(text = (ChangeTime((offsetRight / 8) / rowWidth, LocalDateTime.now()).format(
//                    DateTimeFormatter.ofPattern("HH:mm"))).toString())
            }
            VerticalDivider(modifier
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetRight = min(max(offsetLeft, offsetRight + delta), rowWidth.toFloat())
                        itemSize = offsetRight - offsetLeft
                        uiState.value.meetingTime.editedTime = true
                    },
                    onDragStopped = {
                        viewModel.changeTime(offsetRight / rowWidth, "end")
                    }
                ), 6.dp, Color.Gray)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    Row()
    {
        Spacer(Modifier.weight(1f))

        Text(text = IsoTimeString(uiState.value.meetingData.begin), textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Button(onClick = { viewModel.findTime() }) { Text("Find Time") }
        Spacer(Modifier.weight(1f))
        Text(text = IsoTimeString(uiState.value.meetingData.end))
        Spacer(Modifier.weight(1f))

    }

}

@Composable
fun DateSelect(modifier: Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Row()
    {
        Button(onClick = {viewModel.changeDate(uiState.value.meetingTime.year - 1, "year")},
            modifier = Modifier.weight(1f)
        )
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Text(uiState.value.meetingTime.year.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Button(onClick = {viewModel.changeDate(uiState.value.meetingTime.year + 1, "year")},
            modifier = Modifier.weight(1f)
        )
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
    Row()
    {
        Button(onClick = {viewModel.changeDate(uiState.value.meetingTime.month - 1, "month")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Text(Month.of(uiState.value.meetingTime.month.toInt()).getDisplayName(TextStyle.FULL, Locale.US),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f))
        Button(onClick = {viewModel.changeDate(uiState.value.meetingTime.month + 1, "month")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
    Row()
    {
        Button(onClick = {viewModel.changeDate(uiState.value.meetingTime.day - 1, "day")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Text(uiState.value.meetingTime.day.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f))
        Button(onClick = {viewModel.changeDate(uiState.value.meetingTime.day + 1, "day")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun PubSelector(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Button(onClick = {viewModel.findPub()}) { Text("Find pub") }
    LazyRow(modifier = Modifier.height(500.dp))
    {
        items(uiState.value.pubData)
        {
            Card(modifier = Modifier.clickable { viewModel.changePub(it.id) }.height(500.dp).width(500.dp))
            {
                Text(text = it.name)
                Text(text = it.address)

                if(uiState.value.meetingData.pub_id == it.id)
                {
                    Text("Selected")
                }
            }
        }
    }

}