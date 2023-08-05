package com.oxodiceproductions.dockmaker.ui.compose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.database.Image

@Composable
fun ImagePreviewItem(item: Image) {
    Row() {
        AsyncImage(model = item.imagePath, contentDescription = "Image",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePreviewItemPreview() {
    ImagePreviewItem(Image("https://picsum.photos/200/300",1,1L))
}