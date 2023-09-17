package com.oxodiceproductions.dockmaker.ui.compose.activity.document_view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.ui.compose.activity.image_edit.EditingActivity
import com.oxodiceproductions.dockmaker.ui.compose.activity.single_image.SingleImageActivity
import com.oxodiceproductions.dockmaker.ui.compose.components.ImagePreviewItem
import com.oxodiceproductions.dockmaker.ui.compose.components.RenameDocDialog
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import com.oxodiceproductions.dockmaker.utils.ImageCompressor
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class DocumentView : ComponentActivity() {
    private var docId = 0L
    private lateinit var viewModel: DocViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DocViewViewModel::class.java]
        docId = intent.getLongExtra(Constants.SP_DOC_ID, 0L)
        viewModel.loadImagesForDoc(docId) {
            CO.log("loadImagesForDoc: ${it.message}")
        }
        val getImageFromGallery =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    CO.log("Uri Path: ${uri.path}")
                    val imageCompressor = ImageCompressor(this)
                    val filePath = imageCompressor.compress(uri)
                    viewModel.addImageToDocument(docId, filePath) {
                        CO.log("Image Added to Document: ${it.message}")
                        val intent = Intent(this, EditingActivity::class.java)
                        intent.putExtra("docId", docId)
                        intent.putExtra("imagePath", filePath)
                        startActivity(intent)
                    }
                }
            }
        val clickImageFromCamera =
            registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (it) {
                    CO.log("Image Captured")
                    val imageCompressor = ImageCompressor(this)
                    val filePath = imageCompressor.compress(File(filesDir,"picFromCamera"))
                    viewModel.addImageToDocument(docId, filePath) {
                        CO.log("Image Added to Document: ${it.message}")
                    }
                }
            }

        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DocView(this, docId, getImageFromGallery, clickImageFromCamera, viewModel)
                }
            }
        }
    }
}

@Composable
fun DocView(
    context: Context,
    docId: Long,
    getImageFromGallery: ActivityResultLauncher<String>?,
    clickImageFromCamera: ActivityResultLauncher<Uri>?,
    viewModel: DocViewViewModel
) {

    LaunchedEffect(key1 = docId) {
        viewModel.loadImagesForDoc(docId) {
            CO.log("loadImagesForDoc: ${it.message}")
        }
        viewModel.getDocById(docId) {
            CO.log("Error while getting doc by id: ${it.message}")
        }
    }

    val list = viewModel.loadImagesResponse.observeAsState()
    val doc = viewModel.loadDocumentResponse.observeAsState()
    val renameDialogVisible = remember { mutableStateOf(false) }
    val photoInputTypeDialogVisible = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (renameDialogVisible.value) {
            RenameDocDialog(doc.value?.name ?: "Doc Name", {
                viewModel.renameDoc(docId, it) { e ->
                    CO.log("Error while updating name: ${e.message}")
                }
                renameDialogVisible.value = false
            }) {
                renameDialogVisible.value = false
            }
        }

        if (photoInputTypeDialogVisible.value) {
            Dialog(onDismissRequest = { /*TODO*/ }) {
                Column {
                    Button(onClick = {
                        getImageFromGallery?.launch("image/*")
                        photoInputTypeDialogVisible.value = false
                    }) {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_photo_library_24),
                                contentDescription = "Choose from gallery"
                            )
                            Text(text = "Choose from gallery")
                        }
                    }
                    Button(onClick = {
                        val file = File(context.filesDir, "picFromCamera")
                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            file
                        )

                        clickImageFromCamera?.launch(uri)
                        photoInputTypeDialogVisible.value = false
                    }) {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_photo_camera_24),
                                contentDescription = "Take a photo"
                            )
                            Text(text = "Take a photo")
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TextButton(
                content = {
                    Text(
                        text = doc.value?.name ?: "Document"
                    )
                },
                modifier = Modifier.padding(16.dp),
                onClick = {
                    renameDialogVisible.value = true
                }
            )
            LazyColumn() {
                items(list.value ?: listOf()) { item ->
                    ImagePreviewItem(item!!) {
                        val intent = Intent(context, SingleImageActivity::class.java)
                        intent.putExtra("docId", docId)
                        intent.putExtra("imagePath", item.imagePath)
                        intent.putExtra("imageIndex", item.imageIndex)
                        context.startActivity(intent)
                    }
                }
            }
        }

//        Add Button
        FloatingActionButton(
            onClick = {
                photoInputTypeDialogVisible.value = true
            },
            modifier = Modifier
                .width(80.dp)
                .height(80.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            backgroundColor = Color(0xFFF3B96D),
            shape = RoundedCornerShape(8.dp),
        ) {
            Icon(
                Icons.Filled.Add, contentDescription = "Add Document",
                tint = Color.White
            )
        }

    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun DefaultPreview3() {
    DocMakerTheme {
        DocView(
            LocalContext.current,
            1L,
            null,
            null,
            DocViewViewModel(AppDatabase.getInstance(LocalContext.current))
        )
    }
}