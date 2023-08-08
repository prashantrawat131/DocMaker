package com.oxodiceproductions.dockmaker.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun SimpleDialog(
    heading: String,
    description: String,
    positiveText: String,
    negativeText: String,
    positive: () -> Unit,
    negative: () -> Unit
) {
    Dialog(onDismissRequest = { /*TODO*/ }) {
        Column {
            Text(text = heading)
            Text(text = description)
            Button(onClick =  negative ) {
                Text(text = negativeText)
            }
            Button(onClick = positive) {
                Text(text = positiveText)
            }
        }
    }
}