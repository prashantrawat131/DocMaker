package com.oxodiceproductions.dockmaker.ui.compose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.model.DocumentPreviewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun DocumentPreviewItem(
    item: DocumentPreviewModel,
    isSelectionModeOn: Boolean,
    onItemClick: () -> Unit,
    onLongPress: (Long) -> Unit
) {
    Card(
        elevation = 2.dp,
        modifier = Modifier.combinedClickable(
            onClick = {
                if (isSelectionModeOn) {
                    onLongPress(item.id)
                } else {
                    onItemClick()
                }
            },
            onLongClick = {
                onLongPress(item.id)
            }
        )
    ) {
        Row {
            if (isSelectionModeOn) {
                Checkbox(checked = item.isSelected, onCheckedChange = {
                    onLongPress(item.id)
                }, modifier = Modifier.align(Alignment.CenterVertically))
            }
            if (item.image != null) {
                AsyncImage(
                    model = item.image,
                    contentDescription = "Preview Image",
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                        .padding(16.dp)
                        .align(Alignment.CenterVertically)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_broken_image_24),
                    contentDescription = "Preview Image",
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                        .padding(16.dp)
                        .align(Alignment.CenterVertically),
                    colorFilter = ColorFilter.tint(Color.Gray)
                )
            }
            Text(
                text = item?.name ?: "",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(16.dp),
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
            )
        }
    }
}

//Preview
@Preview(showBackground = true)
@Composable
fun DocumentPreviewItemPreview() {
    DocumentPreviewItem(
        item = DocumentPreviewModel(
            id = 0,
            name = "Test",
            image = null,
            dateTime = DocumentPreviewModel.DateTime(
                date = "01/01/2021",
                time = "12:00"
            ),
            imageCount = 0
        ), false, {

        }
    ) {

    }
}