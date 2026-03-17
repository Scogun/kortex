package com.ucasoft.kortex.sample.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.ucasoft.kortex.compose.extensions.collectAsState
import com.ucasoft.kortex.entities.StateAttributes
import com.ucasoft.kortex.entities.ToggleableEntity
import kotlinx.coroutines.launch

@Composable
internal fun <T: ToggleableEntity<A>, A: StateAttributes> ToggleableEntityList(entities: List<T>) {
    EntityList(
        entities,
        trailingContent = {
            ToggleButton(it)
        }
    )
}

@Composable
private fun ToggleButton(entity: ToggleableEntity<*>) {
    val state by entity.collectAsState()
    val scope = rememberCoroutineScope()
    IconButton(
        onClick = {
            scope.launch {
                entity.toggle()
            }
        }
    ) {
        if (state.isOn) {
            Icon(Icons.Rounded.ToggleOn, contentDescription = "Turn off")
        } else {
            Icon(Icons.Rounded.ToggleOff, contentDescription = "Turn on")
        }
    }
}