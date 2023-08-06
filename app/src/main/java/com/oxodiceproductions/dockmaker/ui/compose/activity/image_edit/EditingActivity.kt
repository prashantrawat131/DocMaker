package com.oxodiceproductions.dockmaker.ui.compose.activity.image_edit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme

class EditingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagePath = intent.extras?.getString("ImagePath")
        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    EditingView(this, imagePath ?: "")
                }
            }
        }
    }
}

@Composable
fun EditingView(context: Context, imagePath: String) {
    Column {
        AsyncImage(model = imagePath, contentDescription = "Image for editing")
    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 600)
@Composable
fun DefaultPreview5() {
    DocMakerTheme {
        EditingView(LocalContext.current, "")
    }
}