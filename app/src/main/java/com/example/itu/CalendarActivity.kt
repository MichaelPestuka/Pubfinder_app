/**
 * @author Michael Peštuka (xpestu01)
 */

package com.example.itu

import android.content.res.Resources.getSystem
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.itu.ui.theme.Typography
import kotlinx.coroutines.launch
import java.time.Month
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Activity where user manages time blocks on specific dates
 */
class CalendarActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels()
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
                            title = {Text("Calendar - Times when I'm busy")})
                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding))
                    {
                        MonthSelector(modifier = Modifier.height(32.dp), viewModel = viewModel)

                        LazyRow(modifier = Modifier.padding(start = 16.dp))
                        {
                            // Display columns depending on month days
                            items(viewModel.uiState.value.displayedDate.month.length(false))
                            { day ->
                                Column()
                                {
                                    // Day number
                                    Text((day + 1).toString() + ".", textAlign = TextAlign.Center, modifier = Modifier.width(128.dp).height(24.dp))
                                    CalendarColumn(viewModel = viewModel, day = (day + 1).toString())
                                }
                            }
                        }
                    }

                    TimeSidebar(innerPadding)
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
    }
}

/**
 * Component which displays current month and year and has buttons to change it
 */
@Composable
fun MonthSelector(modifier: Modifier = Modifier, viewModel: CalendarViewModel)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Row(modifier = modifier) {
        // subtract month
        Button( onClick = { viewModel.changeDisplayMonth(-1) }) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
        Spacer(modifier = Modifier.weight(1f))
        // display text
        Text(text = Month.of(uiState.value.displayedDate.monthValue).getDisplayName(java.time.format.TextStyle.FULL, Locale.US) + " " + uiState.value.displayedDate.year.toString(),
            textAlign = TextAlign.Center,
            style = Typography.headlineMedium)
        Spacer(modifier = Modifier.weight(1f))
        // ad month
        Button( onClick = {viewModel.changeDisplayMonth(1)}) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/**
 * Column representing one day in calendar and displayed time blocks
 * Start, end and whole block are movable, with time being updated once user lets go
 */
@Composable
fun CalendarColumn(modifier: Modifier = Modifier, viewModel: CalendarViewModel, day: String)
{
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.width(128.dp))
    {
        var columnHeight by remember { mutableIntStateOf(1714) }
        Column(Modifier
            .padding((16.dp))
            .fillMaxSize(1f)
            .onGloballyPositioned {
                columnHeight = it.size.height
            }
        )
        {
            // evenly spaced 24 dividers for 24 hours
            repeat(23)
            { number ->
                HorizontalDivider(modifier, 1.dp, Color.Gray)
                Spacer(Modifier
                    .weight(1f)
                    .fillMaxSize(1f)
                    .clickable {
                        viewModel.createDayItem(1, day.toInt(), number / 24f, (number + 1) / 24f)
                    }
                )

            }
            HorizontalDivider(modifier, 1.dp, Color.Gray)
            Spacer(Modifier.weight(1f))

        }
        // Display all time blocks for the day
        for (tb in uiState.value.displayedCalendarItems.filter { it.day == day.toInt() }) {
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
                // Movable start bar
                HorizontalDivider(modifier
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            offsetTop = min(max(0f, offsetTop + delta), offsetBottom)
                            itemSize = offsetBottom - offsetTop
                            tb.positionChanged = true
                        },
                        onDragStopped = { viewModel.updateDayItem(tb.id, newStart = offsetTop / columnHeight, updateEnd = false) }
                    )
                        , 6.dp, Color.Gray)

                // Movable time block body
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
                        onDragStopped = { viewModel.updateDayItem(tb.id, newStart = offsetTop / columnHeight, newEnd = offsetBottom / columnHeight) }
                    )
                    .clickable {
                        viewModel.deleteDayItem(tb.id)
                    }
                    .alpha(0.5f),
                    shape = RectangleShape

                )
                {
                    Text(printFloatTime(offsetTop / columnHeight), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(1f))
                    Spacer(Modifier.weight(1f))
                    Text(printFloatTime(offsetBottom / columnHeight), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(1f))
                }

                // Movable end bar
                HorizontalDivider(modifier.draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        offsetBottom = min(max(offsetTop, offsetBottom + delta), columnHeight.toFloat())
                        itemSize = offsetBottom - offsetTop
                        tb.positionChanged = true
                    },
                    onDragStopped = { viewModel.updateDayItem(tb.id, newEnd = offsetBottom / columnHeight, updateStart = false) }
                )
                , 6.dp, Color.Red)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Sidebar displaying hours to better see when something is
 */
@Composable
fun TimeSidebar(innerPadding: PaddingValues)
{
    Column(modifier = Modifier.width(32.dp).padding(innerPadding).padding(top = 60.dp, bottom = 28.dp))
    {
        repeat(24)
        { hour ->
            Text(text = hour.toString())
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}



