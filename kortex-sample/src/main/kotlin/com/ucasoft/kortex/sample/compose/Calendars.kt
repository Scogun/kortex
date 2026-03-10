package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ucasoft.kortex.entities.Calendar
import com.ucasoft.kortex.entities.CalendarEvent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Calendars(calendars: List<Calendar>) {

    EntityList(calendars) {
        CalendarEvents(it, Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CalendarEvents(calendar: Calendar?, modifier: Modifier = Modifier) {

    val events = remember { mutableStateListOf<CalendarEvent>() }

    LaunchedEffect(calendar) {
        calendar?.getEvents(
            end = Clock.System.now().plus(120.days)
        )?.apply {
            events.clear()
            events.addAll(this)
        }
    }

    LazyColumn(
        modifier = modifier
    ) {
        items(events) {
            ListItem(
                overlineText = {
                    Text("${it.start} - ${it.end}")
                },
                secondaryText = {
                    Text(it.location ?: "")
                }
            ) {
                Text(it.summary)
            }
        }
    }
}