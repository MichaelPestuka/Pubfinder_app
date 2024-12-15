/**
 * @author Michael Peštuka (xpestu01)
 */

package com.example.itu

import android.content.res.Resources.getSystem
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.itu.ui.theme.ITUTheme
import kotlinx.coroutines.launch
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Activity for editing meetings
 */
class MeetingActivity : ComponentActivity() {
    private val viewModel: MeetingViewmodel by viewModels()

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
        viewModel.uiState.value.meetingID = intent.getIntExtra("ExistingMeetingID", 0)
        viewModel.fetchData()
        super.onCreate(savedInstanceState)

        setContent {

            ITUTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Edit Meeting") })
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding))
                    {
                        MeetingEditor(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

/**
 * Meeting editor component
 */
@Composable
fun MeetingEditor(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).height(1400.dp)
    ) {
        Text("Time and Date", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        DateSelect(modifier, viewModel)
        TimeSelector(modifier, viewModel)

        HorizontalDivider(Modifier.padding(top = 16.dp))
        Text("Participants", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        UserSelector(viewModel = viewModel)

        HorizontalDivider(Modifier.padding(top = 16.dp))
        Text("Pub", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        PubSelector(viewModel = viewModel)
    }
}

/**
 * Component that lists users and allows filtering and adding/removing from meetings
 */
@Composable
fun UserSelector(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // Filtering search with name
    var nameFilter by remember { mutableStateOf("") }
    Card(modifier = modifier.fillMaxWidth(1f).height(500.dp))
    {
        TextField(value = nameFilter, onValueChange = {nameFilter = it}, label = {Text("Find User")}, modifier = Modifier.fillMaxWidth(1f))
        LazyColumn()
        {
            var filteredUsers = uiState.value.users
            if(nameFilter != "")
            {
                filteredUsers = uiState.value.users.filter { it.username.lowercase().contains(nameFilter.lowercase()) }.toMutableList()
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

/**
 * Sliding bar meeting time selector
 */
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

        // Sliding bar selector

        var rowWidth by remember { mutableFloatStateOf(100f) }



        Row(modifier = Modifier
            .onGloballyPositioned { rowWidth = it.size.width.toFloat() })
        {
            var offsetLeft by remember { mutableFloatStateOf(0f) }
            var offsetRight by remember { mutableFloatStateOf(500f) }
            var itemSize by remember { mutableFloatStateOf(0f) }

            if (!uiState.value.meetingTime.editedTime) {
                offsetLeft = rowWidth * uiState.value.meetingTime.start
                offsetRight = rowWidth * uiState.value.meetingTime.end
                itemSize = offsetRight - offsetLeft
            }
            Spacer(modifier = Modifier.width(offsetLeft.dp / getSystem().displayMetrics.density)
                .onGloballyPositioned {
                    if (!uiState.value.meetingTime.editedTime) {
                        offsetLeft = rowWidth * uiState.value.meetingTime.start
                        offsetRight = rowWidth * uiState.value.meetingTime.end
                        itemSize = offsetRight - offsetLeft
                    }
                })
            // Left sliding bar
            VerticalDivider(modifier
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetLeft = min(max(0f, offsetLeft + delta), offsetRight)
                        itemSize = offsetRight - offsetLeft
                        uiState.value.meetingTime.editedTime = true
                    },
                    onDragStopped = { viewModel.setNewTime(offsetLeft / rowWidth, "start") }
                ), 6.dp, Color.Gray)

            // Sliding bar body
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
                        viewModel.setNewTime(offsetLeft / rowWidth, "start")
                        viewModel.setNewTime(offsetRight / rowWidth, "end")
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
            // Left Sliding bar
            VerticalDivider(modifier
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetRight = min(max(offsetLeft, offsetRight + delta), rowWidth.toFloat())
                        itemSize = offsetRight - offsetLeft
                        uiState.value.meetingTime.editedTime = true
                    },
                    onDragStopped = {
                        viewModel.setNewTime(offsetRight / rowWidth, "end")
                    }
                ), 6.dp, Color.Gray)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    // Time display and find time button
    Row()
    {
        Spacer(Modifier.weight(1f))

        Text(text = isoTimeString(uiState.value.meetingData.begin), textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Button(onClick = { viewModel.findTime() }) { Text("Find Time") }
        Spacer(Modifier.weight(1f))
        Text(text = isoTimeString(uiState.value.meetingData.end))
        Spacer(Modifier.weight(1f))

    }

}

/**
 * Year, month and date time selector
 */
@Composable
fun DateSelect(modifier: Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // Year selector
    Row()
    {
        Button(onClick = {viewModel.setNewDate(uiState.value.meetingTime.year - 1, "year")},
            modifier = Modifier.weight(1f)
        )
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Text(uiState.value.meetingTime.year.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Button(onClick = {viewModel.setNewDate(uiState.value.meetingTime.year + 1, "year")},
            modifier = Modifier.weight(1f)
        )
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }

    // Month selector
    Row()
    {
        Button(onClick = {viewModel.setNewDate(uiState.value.meetingTime.month - 1, "month")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Text(Month.of(uiState.value.meetingTime.month).getDisplayName(TextStyle.FULL, Locale.US),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f))
        Button(onClick = {viewModel.setNewDate(uiState.value.meetingTime.month + 1, "month")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }

    // Day Selector
    Row()
    {
        Button(onClick = {viewModel.setNewDate(uiState.value.meetingTime.day - 1, "day")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Text(uiState.value.meetingTime.day.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f))
        Button(onClick = {viewModel.setNewDate(uiState.value.meetingTime.day + 1, "day")},
            modifier = Modifier.weight(1f))
        {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/**
 * List of pubs listing name, address, beers and recommended and selected states
 */
@Composable
fun PubSelector(modifier: Modifier = Modifier, viewModel: MeetingViewmodel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // Filter by name and address
    var nameFilter by remember { mutableStateOf("") }
    TextField(value = nameFilter, onValueChange = {nameFilter = it}, label = {Text("Search")}, modifier = Modifier.fillMaxWidth(1f))

    var bestPubs = viewModel.getPubsByID(uiState.value.bestPubs)
    var allPubs = uiState.value.pubData
    if(nameFilter != "")
    {
        bestPubs = bestPubs.filter { (it.name.lowercase() + it.address).contains(nameFilter.lowercase()) }.toMutableList()
        allPubs = allPubs.filter {  (it.name.lowercase() + it.address).contains(nameFilter.lowercase()) }.toMutableList()
    }

    // List pubs, best ones go first
    LazyRow(modifier = Modifier.height(500.dp))
    {
        items(bestPubs)
        {
            PubCard(modifier, viewModel, it, true)
        }

        items(allPubs)
        {
            if(it.id !in uiState.value.bestPubs)
            {
                PubCard(modifier, viewModel, it, false)
            }
        }
    }

}

/**
 * Card showing one pub info
 */
@Composable
fun PubCard(modifier: Modifier = Modifier, viewModel: MeetingViewmodel, pub: Pub, recommended: Boolean)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Card(modifier = Modifier.clickable { viewModel.changePub(pub.id) }.height(300.dp).width(300.dp).padding(16.dp))
    {
        Column(Modifier.padding(8.dp)) {
            Row()
            {
                Column()
                {
                    Text(text = pub.name)
                    Text(text = pub.address)
                }
                Spacer(Modifier.weight(1f))
                // Recommended ones get a star
                if (recommended) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }

            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            val onTap = viewModel.getBeers(pub.id)
            for (beer in onTap) {
                Text(beer.name + " " + beer.degree + "°")
            }

            // Selected pub gets a checkmark
            if (uiState.value.meetingData.pub_id == pub.id) {
                Spacer(Modifier.weight(1f))
                Row()
                {
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }
}