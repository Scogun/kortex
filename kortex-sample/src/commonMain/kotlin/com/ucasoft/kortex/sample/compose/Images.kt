package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.ucasoft.kortex.client.rest.RestKortexClient
import com.ucasoft.kortex.compose.extensions.collectAsState
import com.ucasoft.kortex.entities.Image
import org.koin.compose.koinInject

@Composable
internal fun Images(images: List<Image>) {
    EntityList(images) {
        Image(it)
    }
}

@Composable
private fun RowScope.Image(image: Image) {

    val restClient = koinInject<RestKortexClient>()

    val state by image.collectAsState()
    if (state.attributes.entityPicture != null) {
        AsyncImage(
            modifier = Modifier.weight(1f),
            model = restClient.buildImageUrl(state.attributes.entityPicture!!),
            contentDescription = state.attributes.friendlyName
        )
    }
}