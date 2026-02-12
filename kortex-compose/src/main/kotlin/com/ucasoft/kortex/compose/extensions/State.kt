package com.ucasoft.kortex.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.State as ComposeState
import com.ucasoft.kortex.entities.Light

@Composable
fun Light.collectAsState(): ComposeState<Light> {
    val state = remember(this) { mutableStateOf(this, neverEqualPolicy()) }
    LaunchedEffect(this) {
        stateFlow.collect {
            state.value = this@collectAsState
        }
    }

    return state
}