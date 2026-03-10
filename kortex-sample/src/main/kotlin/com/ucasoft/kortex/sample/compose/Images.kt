package com.ucasoft.kortex.sample.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.ucasoft.kortex.client.ClientConfig
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

    val clientConfig = koinInject<ClientConfig>()

    val state by image.collectAsState()
    if (state.attributes.entityPicture != null) {
        AsyncImage(
            modifier = Modifier.weight(1f),
            model = "http://${clientConfig.hostname}:${clientConfig.port}${state.attributes.entityPicture}",
            contentDescription = state.attributes.friendlyName
        )
    }
}