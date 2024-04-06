package com.oxodiceproductions.dockmaker.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.database.Image
import java.io.File

@Composable
fun ImagePreviewItem(item: Image, index: Int, onClick: () -> Unit) {
//    val imageSize = (File(item.imagePath).length() / 1024).toString() + " KB"
    Row(
        modifier = Modifier
            .fillMaxWidth(1f)
            .selectable(selected = false, onClick = onClick)
    ) {
        AsyncImage(
            model = item.imagePath, contentDescription = "Image",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .padding(16.dp)
        )
        Text(
            text = index.toString(),
            modifier = Modifier
                .padding(32.dp, 0.dp)
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePreviewItemPreview() {
    ImagePreviewItem(Image("https://picsum.photos/200/300", 1, 1L), 1) {

    }
}