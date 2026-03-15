package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ucasoft.kortex.compose.KortexApplicationState

private enum class HomeTab(val title: String) {
    BINARY_SENSORS("Binary Sensors"),
    BUTTONS("Buttons"),
    CALENDARS("Calendars"),
    CLIMATES("Climates"),
    COVERS("Covers"),
    DEVICES("Devices"),
    FANS("Fans"),
    IMAGES("Images"),
    LIGHTS("Lights"),
    MEDIA_PLAYERS("Media Players"),
    NUMBERS("Numbers"),
    PERSONS("Persons"),
    SELECTS("Selects"),
    SENSOR("Sensor"),
    SWITCH("Switch"),
    TIMER("Timer"),
    VACUUMS("Vacuums"),
    ZONE("Zone")
}

@Composable
internal fun HomeAssistant(state: KortexApplicationState) {

    var selectedTab by remember { mutableStateOf(HomeTab.BINARY_SENSORS) }
    val selectedIndex = HomeTab.entries.indexOf(selectedTab)

    Column {
        BoxWithConstraints {
            val wideLayout = maxWidth >= 1400.dp
            if (wideLayout) {
                PrimaryTabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = selectedIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                ) {
                    TabItems(selectedTab) {
                        selectedTab = it
                    }
                }
            } else {
                PrimaryScrollableTabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = selectedIndex,
                    edgePadding = 8.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                ) {
                    TabItems(selectedTab) {
                        selectedTab = it
                    }
                }
            }
        }
        Box {
            when (selectedTab) {
                HomeTab.BINARY_SENSORS -> EntityList(state.entities!!.binarySensors)
                HomeTab.BUTTONS -> ButtonList(state.entities!!.buttons)
                HomeTab.CALENDARS -> Calendars(state.entities!!.calendars)
                HomeTab.CLIMATES -> ToggleableEntityList(state.entities!!.climates)
                HomeTab.COVERS -> EntityList(state.entities!!.covers)
                HomeTab.DEVICES -> EntityList(state.entities!!.deviceTrackers)
                HomeTab.FANS -> ToggleableEntityList(state.entities!!.fans)
                HomeTab.IMAGES -> Images(state.entities!!.images)
                HomeTab.LIGHTS -> ToggleableEntityList(state.entities!!.lights)
                HomeTab.MEDIA_PLAYERS -> MediaPlayers(state.entities!!.mediaPlayers)
                HomeTab.NUMBERS -> EntityList(state.entities!!.numbers)
                HomeTab.PERSONS -> EntityList(state.entities!!.persons)
                HomeTab.SELECTS -> Selects(state.entities!!.selects)
                HomeTab.SENSOR -> Sensors(state.entities!!.sensors)
                HomeTab.SWITCH -> ToggleableEntityList(state.entities!!.switches)
                HomeTab.TIMER -> EntityList(state.entities!!.timers)
                HomeTab.VACUUMS -> EntityList(state.entities!!.vacuums)
                HomeTab.ZONE -> EntityList(state.entities!!.zones)
            }
        }
    }
}

@Composable
private fun TabItems(selectedTab: HomeTab, onSelected: (HomeTab) -> Unit) {
    HomeTab.entries.forEach { tab ->
        val selected = selectedTab == tab
        Tab(
            selected = selected,
            onClick = { onSelected(tab) },
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            text = {
                Text(
                    text = tab.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }
        )
    }
}
