package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ucasoft.kortex.compose.extensions.collectAsState
import com.ucasoft.kortex.entities.Entity
import com.ucasoft.kortex.entities.StateAttributes
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility

@Composable
internal fun <T: Entity<A>, A: StateAttributes> EntityList(
    entities: List<T>,
    trailingContent: @Composable (T) -> Unit = {},
    selectedContent: @Composable RowScope.(T) -> Unit = {
        EntityInfo(it, Modifier.weight(1f))
    }
) {

    var selectedEntity by remember { mutableStateOf<T?>(null) }

    Row {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(entities) {
                ListItem(
                    modifier = Modifier.clickable {
                        selectedEntity = it
                    }.background(
                        if (selectedEntity == it)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent
                    ),
                    headlineContent = {
                        Text(it.attributes.friendlyName)
                    },
                    trailingContent = {
                        trailingContent(it)
                    }
                )
            }
        }
        selectedEntity?.let {
            selectedContent.invoke(this, it)
        } ?: Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text("Select an entity to see its attributes")
        }
    }
}

@Composable
internal fun EntityInfo(entity: Entity<*>, modifier: Modifier = Modifier) {
    val state by entity.collectAsState()
    Column(
        modifier = modifier
    ) {
        EntityState(state.state)
        EntityAttributes(state.attributes)
    }
}

@Composable
internal fun EntityState(state: String, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        overlineContent = {
            Text("state")
        },
        headlineContent = {
            Text(state)
        }
    )
}

@Composable
internal fun EntityAttributes(attributes: StateAttributes, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        items(attributes.toMap().toList()) {
            ListItem(
                overlineContent = {
                    Text(it.first)
                },
                headlineContent = {
                    Text(it.second ?: "")
                }
            )
        }
    }
}

private fun StateAttributes.toMap(): Map<String, String?> {
    return this::class.members.filterIsInstance<KProperty<*>>().filter { it.visibility == KVisibility.PUBLIC }.associate {
        it.name to it.getter.call(this)?.toString()
    }
}