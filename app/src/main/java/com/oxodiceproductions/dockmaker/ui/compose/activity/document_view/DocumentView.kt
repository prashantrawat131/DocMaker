package com.oxodiceproductions.dockmaker.ui.compose.activity.document_view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.ui.compose.activity.camera.CameraActivity
import com.oxodiceproductions.dockmaker.ui.compose.activity.single_image.SingleImageActivity
import com.oxodiceproductions.dockmaker.ui.compose.components.ImagePreviewItem
import com.oxodiceproductions.dockmaker.ui.compose.components.RenameDocDialog
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import com.oxodiceproductions.dockmaker.utils.ImageCompressor
import com.oxodiceproductions.dockmaker.utils.NotificationModule
import com.oxodiceproductions.dockmaker.utils.PDFMaker
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream


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
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    if (uri != null) {
                        CO.log("Uri Path: ${uri.path}")
                        val imageCompressor = ImageCompressor(this)
                        val filePath = imageCompressor.compress(uri)
                        viewModel.addImageToDocument(docId, filePath) {
                            CO.log("Error while adding image to document: ${it.message}")
                        }

                        /*val intent = Intent(this, EditingActivity::class.java)
                        intent.putExtra("docId", docId)
                        intent.putExtra("imagePath", filePath)
                        startActivity(intent)*/
                    }
                }
            }

        val captureImage =
            registerForActivityResult(object : ActivityResultContract<Unit, String?>() {
                override fun createIntent(context: Context, input: Unit): Intent {
                    return Intent(context, CameraActivity::class.java)
                }

                override fun parseResult(resultCode: Int, intent: Intent?): String? {
                    CO.log("Result Code: $resultCode \t Intent: ${intent?.getStringExtra(Constants.imagePath)}")
                    return intent?.getStringExtra(Constants.imagePath)
                }

            }) {
                if (it != null) {
                    val imageCompressor = ImageCompressor(this)
                    val filePath = imageCompressor.compress(File(it))
                    CO.log("File Path from camera activity: $filePath")
                    viewModel.addImageToDocument(docId, filePath) {
                        CO.log("Error while adding image to document: ${it.message}")
                    }
                }
            }

        viewModel.tempPdfResponse.observeForever { tempPdfPath ->
            CO.log("Temp PDF Path: $tempPdfPath")
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(
                this,
                "com.oxodiceproductions.dockmaker",
                File(tempPdfPath)
            )
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        }

        viewModel.sharePdfResponse.observeForever {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            val uri = FileProvider.getUriForFile(this, "com.oxodiceproductions.dockmaker", File(it))
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(intent, "Share PDF"))
        }

        viewModel.downloadPdfResponse.observeForever {
            val notificationModule=NotificationModule()
            if (it) {
                notificationModule.generateNotification(this, "PDF downloaded", "Go to downloads.");
                CO.toast("PDF downloaded successfully", this)
            } else {
                notificationModule.generateNotification(this, "Error", "Error while downloading PDF.");
                CO.toast("Error while downloading PDF", this)
            }
        }

        setContent()
        {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DocView(
                        this,
                        docId,
                        getImageFromGallery,
                        captureImage,
                        {
                            finish()
                        },
                        viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getDocById(docId) {
            CO.log("Error while getting doc by id: ${it.message}")
        }

        viewModel.loadImagesForDoc(docId) {
            CO.log("Error while loading images for doc: ${it.message}")
        }
    }
}

@Composable
fun DocView(
    context: Context,
    docId: Long,
    getImageFromGallery: ActivityResultLauncher<Intent>?,
    captureImage: ActivityResultLauncher<Unit>?,
    goToAllDocsActivity: () -> Unit,
    viewModel: DocViewViewModel
) {

    LaunchedEffect(key1 = docId) {
        viewModel.loadImagesForDoc(docId) {
            CO.log("loadImagesForDoc: ${it.message}")
        }
        viewModel.getDocById(docId) {
            CO.log("Error while getting doc by id: ${it.message}")
        }
        viewModel.deleteDocResponse.observeForever {
            if (it) {
                goToAllDocsActivity()
            }
        }
    }

    val images by viewModel.images.observeAsState(initial = emptyList())
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
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        getImageFromGallery?.launch(intent)
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
                        captureImage?.launch(Unit)
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
            Row {
                IconButton(onClick = {
                    goToAllDocsActivity()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_upward_24),
                        modifier = Modifier.rotate(-90f),
                        contentDescription = "Back"
                    )
                }

                IconButton(onClick = {
                    viewModel.deleteDoc(docId)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = "Delete"
                    )
                }

                IconButton(onClick = {
                    viewModel.makeTempPDF(context) {
                        CO.log("Error while making temp pdf: ${it.message}")
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_picture_as_pdf_24),
                        contentDescription = "Delete"
                    )
                }

                IconButton(onClick = {
                    viewModel.sharePdf(context) {
                        CO.log("Error while sharing pdf: ${it.message}")
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_share_24),
                        contentDescription = "Share"
                    )
                }

                IconButton(onClick = {
                    viewModel.downloadPdf {
                        CO.log("Error while downloading pdf: ${it.message}")
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_get_app_24),
                        contentDescription = "Download PDF"
                    )
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
                    items(images) { item ->
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
/*

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun DefaultPreview3() {
    DocMakerTheme {
        DocView(
            LocalContext.current,
            1L,
            null,
            { },
            DocViewViewModel(AppDatabase.getInstance(LocalContext.current)),
            null
        )
    }
}*/
