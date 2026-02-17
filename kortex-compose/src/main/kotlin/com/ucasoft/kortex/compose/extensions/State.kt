package com.ucasoft.kortex.compose.extensions

import androidx.compose.runtime.*
import com.ucasoft.kortex.entities.Entity
import com.ucasoft.kortex.entities.StateAttributes
import androidx.compose.runtime.State as ComposeState

@Composable
fun <T: Entity<A>, A: StateAttributes> T.collectAsState(): ComposeState<T> {
    val state = remember(this) { mutableStateOf(this, neverEqualPolicy()) }
    LaunchedEffect(this) {
        stateFlow.collect {
            state.value = this@collectAsState
        }
    }

    return state
}