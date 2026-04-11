package com.ucasoft.kortex.sample.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.ucasoft.kortex.entities.PressableEntity
import com.ucasoft.kortex.entities.StateAttributes
import kotlinx.coroutines.launch

@Composable
internal fun<T: PressableEntity<A>, A: StateAttributes> PressableList(entities: List<T>) {
    EntityList(
        entities,
        trailingContent = {
            Press(it)
        }
    )
}

@Composable
private fun Press(entity: PressableEntity<*>) {
    val scope = rememberCoroutineScope()
    IconButton(
        onClick = {
            scope.launch {
                entity.press()
            }
        }
    ) {
        Icon(Icons.Rounded.TouchApp, contentDescription = "Press")
    }
}