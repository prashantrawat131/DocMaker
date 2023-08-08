package com.oxodiceproductions.dockmaker.ui.compose.activity.single_image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.ui.compose.activity.document_view.DocumentView
import com.oxodiceproductions.dockmaker.ui.compose.components.SimpleDialog
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val docId = intent.getLongExtra("docId", 0)
        val imagePath = intent.getStringExtra("imagePath")
        val imageIndex = intent.getIntExtra("imageIndex", 0)
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[SingleImageViewModel::class.java]
        viewModel.deleteImageResponse.observe(this) {
            if (it > 0) {
                val intent = Intent(this, DocumentView::class.java)
                intent.putExtra("docId", docId)
                startActivity(intent)
                finish()
            } else {
                CO.toast("Image not deleted", this)
            }
        }
        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SingleImageView(viewModel, this, Image(imagePath!!, imageIndex, docId))
                }
            }
        }
    }
}

@Composable
fun SingleImageView(
    viewModel: SingleImageViewModel,
    context: Context,
    image: Image
) {
    val askToDelete = remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (askToDelete.value) {
            SimpleDialog(
                heading = "Do you want to delete this image?",
                description = "",
                positiveText = "Delete",
                negativeText = "Cancel",
                positive = {
                    viewModel.deleteImage(image) {
                        CO.log("Image deleted")
                    }
                }) {
                askToDelete.value = false
            }
        }
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    askToDelete.value = true
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Image")
                }
            }
            AsyncImage(
                model = image.imagePath, contentDescription = "Selected Image",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DocMakerTheme {
        SingleImageView(
            SingleImageViewModel(
                AppDatabase.getInstance(LocalContext.current)
            ), LocalContext.current, Image("https://picsum.photos/200/300", 1, 1L)
        )
    }
}