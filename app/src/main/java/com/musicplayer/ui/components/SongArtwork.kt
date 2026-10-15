package com.musicplayer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.musicplayer.R
import java.io.File

@Composable
fun SongArtwork(path: String?, modifier: Modifier = Modifier,
                size: Dp = 56.dp, cornerRadius: Dp = 10.dp) {
    val placeholder = painterResource(R.drawable.artwork_placeholder)
    AsyncImage(model = path?.let(::File), contentDescription = null,
        placeholder = placeholder, fallback = placeholder, error = placeholder,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(size).clip(RoundedCornerShape(cornerRadius)))
}
