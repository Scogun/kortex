package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ucasoft.kortex.entities.Calendar
import com.ucasoft.kortex.entities.CalendarEvent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
internal fun Calendars(calendars: List<Calendar>) {

    EntityList(calendars) {
        CalendarEvents(it, Modifier.weight(1f))
    }
}

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
                overlineContent = {
                    Text("${it.start} - ${it.end}")
                },
                headlineContent = {
                    Text(it.summary)
                },
                supportingContent = {
                    Text(it.location ?: "")
                }
            )
        }
    }
}