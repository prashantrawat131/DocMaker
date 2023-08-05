package com.oxodiceproductions.dockmaker.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import com.oxodiceproductions.dockmaker.database.Document

@Composable
fun RenameDocDialog(doc: Document) {
    val newName = remember {
        mutableStateOf(doc.name)
    }
    Dialog(onDismissRequest = { /*TODO*/ }) {
        Column() {
            TextField(value = newName.value, onValueChange = {
                if(it.trim().isNotEmpty()){
                    newName.value = it
                }
            })
            Row() {
                Button(onClick = {

                }) {
                    Text(text = "Cancel")
                }
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Ok")
                }
            }
        }
    }
}