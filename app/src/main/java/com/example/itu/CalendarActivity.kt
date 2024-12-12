package com.example.itu

import android.app.LauncherActivity.ListItem
import android.content.Context
import android.content.Intent
import android.content.res.Resources.getSystem
import android.os.Bundle
import android.util.Log
import android.widget.RatingBar
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class CalendarActivity : ComponentActivity() {

    val viewModel: CalendarViewModel by viewModels()
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
                            title = {Text("Calendar")})
                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(                            modifier = Modifier.padding(innerPadding),)
                    {
                        MonthSelector(viewModel = viewModel)
                        LazyRow()
                        {
                            items(30)
                            { day ->
                                Column()
                                {
                                    Text(day.toString())
                                    CalendarColumn(viewModel = viewModel, day = day.toString())
                                }
                            }
                        }
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

@Composable
fun MonthSelector(modifier: Modifier = Modifier, viewModel: CalendarViewModel)
{
    Row() {
        Button(onClick = {}) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("December")
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {}) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun CalendarColumn(modifier: Modifier = Modifier, viewModel: CalendarViewModel, day: String)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()



    Box(modifier = Modifier.width(128.dp))
    {
        var columnHeight = 1670
        Column(Modifier
            .padding((16.dp))
            .fillMaxSize(1f)
            .onGloballyPositioned {
                columnHeight = it.size.height
            }
        )
        {
            repeat(23)
            { number ->
                HorizontalDivider(modifier, 1.dp, Color.Gray)
                Spacer(Modifier
                    .weight(1f)
                    .fillMaxSize(1f)
                    .clickable {
                        val newName = ""
                        viewModel.CreateDayItem(1, day.toInt(), number / 24f, (number + 1) / 24f)
                    }
                )

            }
            HorizontalDivider(modifier, 1.dp, Color.Gray)
            Spacer(Modifier.weight(1f))

        }
        for (tb in uiState.value.formattedCalendarItems.filter { it.day == day.toInt() }) {
            Column (Modifier.padding((16.dp)).fillMaxSize(1f))
            {
                var offsetTop by remember { mutableFloatStateOf(columnHeight * tb.start) }
                var offsetBottom by remember { mutableFloatStateOf(columnHeight * tb.end) }
                var itemSize by remember { mutableFloatStateOf((tb.end - tb.start) * columnHeight) }
                Spacer(modifier = Modifier
                    .height(offsetTop.dp / getSystem().displayMetrics.density)
                    .onGloballyPositioned { if(!tb.positionChanged)
                        {
                            offsetTop = columnHeight * tb.start
                            offsetBottom = columnHeight * tb.end
                        }
                    }
                )
                HorizontalDivider(modifier
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            offsetTop = min(max(0f, offsetTop + delta), offsetBottom)
                            itemSize = offsetBottom - offsetTop
                            tb.positionChanged = true
                        },
                        onDragStopped = { viewModel.UpdateDayItem(tb.id, newStart = offsetTop / columnHeight, updateEnd = false) }
                    )
                        , 6.dp, Color.Gray)
                Card(modifier = Modifier
                    .height(abs(offsetBottom - offsetTop).dp / getSystem().displayMetrics.density)
                    .fillMaxWidth(1f)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            offsetTop = min(max(0f, offsetTop + delta), columnHeight.toFloat() - itemSize)
                            offsetBottom = min(max(0f + itemSize, offsetBottom + delta), columnHeight.toFloat())

                            tb.positionChanged = true
                        },
                        onDragStopped = { viewModel.UpdateDayItem(tb.id, newStart = offsetTop / columnHeight, newEnd = offsetBottom / columnHeight) }
                    )
                    .clickable {
                        viewModel.DeleteDayItem(tb.id)
                    }
                    .alpha(0.5f),
                    shape = RectangleShape

                )
                {  }
                HorizontalDivider(modifier.draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        offsetBottom = min(max(offsetTop, offsetBottom + delta), columnHeight.toFloat())
                        itemSize = offsetBottom - offsetTop
                        tb.positionChanged = true
                    },
                    onDragStopped = { viewModel.UpdateDayItem(tb.id, newEnd = offsetBottom / columnHeight, updateStart = false) }
                )
                , 6.dp, Color.Red)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameDialogue(
    title: String = "Name this time slot",
    setName: (name: String) -> Unit

)
{
    BasicAlertDialog(
        onDismissRequest = {setName("Unnamed")}
    )
    {
        val text = remember { mutableStateOf("")}
        Text(title)
        TextField(value = text.value,
            onValueChange = {
                if(it.length < 20) {
                    text.value = it
                }
            }
        )
    }
}





