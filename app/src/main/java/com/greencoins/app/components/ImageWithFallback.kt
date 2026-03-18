package com.greencoins.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.greencoins.app.theme.AppColors

/**
 * Preserves exact behavior of original ImageWithFallback:
 * shows image or fallback placeholder on error.
 */
@Composable
fun ImageWithFallback(
    src: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(src).crossfade(true).build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        error = androidx.compose.ui.graphics.painter.ColorPainter(AppColors.card),
    )
}
