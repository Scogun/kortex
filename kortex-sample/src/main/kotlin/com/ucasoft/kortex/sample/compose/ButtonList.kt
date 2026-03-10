package com.ucasoft.kortex.sample.compose

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.ucasoft.kortex.entities.Button
import kotlinx.coroutines.launch

@Composable
internal fun ButtonList(entities: List<Button>) {
    EntityList(
        entities,
        trailingContent = {
            Press(it)
        }
    )
}

@Composable
private fun Press(entity: Button) {
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