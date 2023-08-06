package com.oxodiceproductions.dockmaker.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun RenameDocDialog(docName: String, setName: (String) -> Unit, hideDialog: () -> Unit) {
    val newName = remember {
        mutableStateOf(docName)
    }
    Dialog(onDismissRequest = { /*TODO*/ }) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Enter the new name")
            TextField(value = newName.value, modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent), onValueChange = {
                newName.value = it
            })
            Row() {
                Button(
                    onClick = {
                        hideDialog()
                    },
                    modifier = Modifier
                        .weight(1f, true)
                        .padding(0.dp, 0.dp, 4.dp, 0.dp)
                ) {
                    Text(text = "Cancel")
                }
                Button(
                    onClick = {
                        if (newName.value.trim().isNotEmpty()) {
                            setName(newName.value)
                        }
                    },
                    modifier = Modifier
                        .weight(1f, true)
                        .padding(4.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text(text = "Ok")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialogPreview() {
    RenameDocDialog(docName = "Doc Name", {

    }, {

    })
}