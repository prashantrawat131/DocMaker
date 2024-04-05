package com.oxodiceproductions.dockmaker.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SimpleDialog(
    heading: String,
    description: String,
    positiveText: String,
    negativeText: String,
    modifier: Modifier,
    positive: () -> Unit,
    negative: () -> Unit
) {
    Dialog(onDismissRequest = { /*TODO*/ }) {
        Column(
            modifier = modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3F51B5),
                            Color(0xFF303F9F),
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = heading,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color.White,
                fontSize = 12.sp,
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = negative) {
                    Text(text = negativeText)
                }
                Button(onClick = positive) {
                    Text(text = positiveText)
                }
            }
        }
    }
}

@Preview
@Composable
fun SimpleDialogPreview() {
    SimpleDialog(
        heading = "Heading",
        description = "Description",
        positiveText = "Positive",
        negativeText = "Negative",
        modifier = Modifier,
        positive = { /*TODO*/ },
        negative = { /*TODO*/ }
    )
}