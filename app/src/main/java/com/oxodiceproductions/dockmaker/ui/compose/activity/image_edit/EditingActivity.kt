package com.oxodiceproductions.dockmaker.ui.compose.activity.image_edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHost
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.ui.compose.activity.document_view.DocumentView
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class EditingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val docId = intent.extras?.getLong(Constants.docId)
        val imagePath = intent.extras?.getString(Constants.imagePath)
        val viewModel = ViewModelProvider(this)[EditingImageViewModel::class.java]
        CO.log("docId: $docId, imagePath: $imagePath")

        viewModel.saveImageResponse.observeForever{
            goToDocView(docId ?: 1L)
        }

        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    EditingView(
                        this, imagePath ?: "",
                        docId ?: 1L,
                        viewModel,
                        this::goToDocView
                    )
                }
            }
        }
    }

    private fun goToDocView(docId: Long) {
        finish()
    }
}


@Composable
fun EditingView(
    context: Context,
    imagePath: String,
    docId: Long,
    viewModel: EditingImageViewModel?,
    goToDocView: (Long) -> Unit
) {

    LaunchedEffect(Unit) {
        viewModel?.cropImage(imagePath, context)
        viewModel?.imageCropResponse?.observeForever { bitmap ->
            if (bitmap != null) {
                viewModel.saveImage(docId, bitmap, imagePath)
            } else {
                goToDocView(docId)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val cropState = viewModel!!.imageCropper.cropState
        if (cropState != null) {
            ImageCropperDialog(
                state = cropState, style = CropperStyle(
                    autoZoom = false
                ),
                dialogPadding = PaddingValues(0.dp)
            )
        }
    }
}

/*
@Composable
fun EditingView(
    context: Context,
    imagePath: String,
    docId: Long,
    viewModel: EditingImageViewModel?,
    goToDocView: (Long) -> Unit
) {
    var lastRotation = 0f

    var rotation by remember {
        mutableStateOf(0f)
    }

    var imageBitmap = remember(imagePath) {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, options)
        bitmap.asImageBitmap()
    }

    var editingState by remember {
        mutableStateOf(Constants.EditingState.NONE)
    }

    var size by remember {
        mutableStateOf(Size(0f, 0f))
    }

    LaunchedEffect(Unit) {
        viewModel!!.saveImageResponse.observeForever {
            editingState = Constants.EditingState.NONE
        }
        viewModel.imageCropResponse.observe(context as EditingActivity) {
            CO.log("imageCropResponse: ${it?.byteCount}")
            imageBitmap = it?.asImageBitmap() ?: imageBitmap
        }
        viewModel!!.cropImage(imageBitmap)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        *//*if (editingState != Constants.EditingState.NONE) {
            Row {
                Button(onClick = {
                    rotation = lastRotation
                    editingState = Constants.EditingState.NONE
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    viewModel!!.rotateImage(imagePath, rotation) {
                        CO.log("rotateImage: ${it.message}")
                    }
                }) {
                    Icon(Icons.Filled.Check, contentDescription = "Done")
                }
            }
        }*//*

       *//* Image(
            bitmap = imageBitmap,
            contentDescription = "Image for editing",
            modifier = Modifier
                .weight(1f)
        )*//*
       *//* val cropper = rememberImageCropper()
        if (cropper.cropState != null) {
            cropper.cropState?.let { ImageCropperDialog(state = it) }
        }else{
            CO.log("cropper.cropState is null")
        }*//*
        if (editingState == Constants.EditingState.ROTATE) {
            Row {
                Slider(
                    modifier = Modifier.padding(24.dp, 0.dp),
                    value = rotation,
                    onValueChange = {
                        rotation = it
                    },
                    valueRange = 0f..360f
                )
            }
        }
        if (editingState == Constants.EditingState.NONE) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    lastRotation = rotation
                    editingState = Constants.EditingState.ROTATE
                }) {
                    Column {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_rotate_right_24),
                            contentDescription = "Rotate"
                        )
                        Text(text = "Rotate")
                    }
                }
                Button(onClick = {
                    editingState = Constants.EditingState.CROP
                }) {
                    Column {
                        Icon(
                            painterResource(id = R.drawable.baseline_crop_24),
                            contentDescription = "Crop"
                        )
                        Text(text = "Crop")
                    }
                }
            }
        }
    }
}*/

@Preview(showBackground = true, widthDp = 300, heightDp = 600)
@Composable
fun EditingActivityPreview() {
    DocMakerTheme {
        EditingView(LocalContext.current, "", 1L, null, {})
    }
}