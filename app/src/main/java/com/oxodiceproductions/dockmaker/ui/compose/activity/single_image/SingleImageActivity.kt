package com.oxodiceproductions.dockmaker.ui.compose.activity.single_image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.ui.compose.activity.document_view.DocumentView
import com.oxodiceproductions.dockmaker.ui.compose.activity.image_edit.EditingActivity
import com.oxodiceproductions.dockmaker.ui.compose.components.SimpleDialog
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.NotificationModule
import com.oxodiceproductions.dockmaker.utils.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleImageActivity : ComponentActivity() {

    private lateinit var viewModel: SingleImageViewModel
    private lateinit var image: Image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val docId = intent.getLongExtra("docId", 0)
        val imagePath = intent.getStringExtra("imagePath")
        val imageIndex = intent.getIntExtra("imageIndex", 0)
        image = Image(imagePath ?: "", imageIndex, docId)

        viewModel = ViewModelProvider(this)[SingleImageViewModel::class.java]

        viewModel.downloadImageResponse.observeForever {
            CO.log("downloadImageResponse: $it")
            NotificationModule().generateNotification(
                this,
                "Image Downloaded",
                "Image has been downloaded successfully"
            )
        }

        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SingleImageView(
                        viewModel,
                        this,
                        image,
                        this::goToDocumentView,
                        this::goToEditingActivity
                    )
                }
            }
        }
    }

    private fun goToDocumentView() {
        finish()
    }

    fun goToEditingActivity() {
        val intent = Intent(this, EditingActivity::class.java)
        intent.putExtra("docId", image.docId)
        intent.putExtra("imagePath", image.imagePath)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadImage(intent.getStringExtra("imagePath") ?: "") {
            CO.log("loadImage: ${it.message}")
        }
    }
}

@Composable
fun SingleImageView(
    viewModel: SingleImageViewModel,
    context: Context,
    image: Image,
    goToDocumentView: () -> Unit,
    goToEditingActivity: () -> Unit
) {
    var imagePath by remember {
        mutableStateOf(image.imagePath)
    }

    LaunchedEffect(Unit) {
        viewModel.loadImageResponse.observeForever {
            imagePath = it
        }
        viewModel.deleteImageResponse.observeForever {
            if (it > 0) {
                goToDocumentView()
            } else {
                CO.toast("Image not deleted", context)
            }
        }
    }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        if (scale != 1f) {
            offset += offsetChange
        }
        if (scale < 1f) {
            scale = 1f
            offset = Offset.Zero
        }
    }
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
                modifier = Modifier.fillMaxWidth(),
                positive = {
                    viewModel.deleteImage(image) {
                        CO.log("Image deleted")
                    }
                }) {
                askToDelete.value = false
            }
        }
        Column {
            if (scale == 1f) {
                Row(modifier = Modifier.fillMaxWidth()) {

                    IconButton(onClick = goToDocumentView) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                    IconButton(onClick = {
                        askToDelete.value = true
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Image")
                    }
                    IconButton(onClick = {
                        viewModel.downloadImage(context) {
                            CO.log("Image downloaded")
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_get_app_24),
                            contentDescription = "Download image"
                        )
                    }
                    IconButton(onClick = {
                        ShareUtil.shareImage(context, image.imagePath)
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Image")
                    }
                    IconButton(onClick = goToEditingActivity) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Image")
                    }

                }
            }
            AsyncImage(
                model = image.imagePath, contentDescription = "Selected Image",
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        translationX = offset.x
                        translationY = offset.y
                        scaleX = scale
                        scaleY = scale
                    }
                    .transformable(state = state),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SingleImageActivityPreview() {
    DocMakerTheme {
        SingleImageView(
            SingleImageViewModel(
                AppDatabase.getInstance(LocalContext.current)
            ), LocalContext.current, Image("https://picsum.photos/200/300", 1, 1L), {}
        ) {

        }
    }
}