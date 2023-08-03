package com.oxodiceproductions.dockmaker.ui.compose

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import com.oxodiceproductions.dockmaker.utils.ImageCompressor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DocumentView : ComponentActivity() {

    private var docId = 0L

    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        docId = intent.getLongExtra(Constants.SP_DOC_ID, 0L)
        mainViewModel.loadImagesForDoc(docId) {
            CO.log("loadImagesForDoc: ${it.message}")
        }
        val getImageFromGallery =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    CO.log("Uri Path: ${uri.path}")
                    val imageCompressor = ImageCompressor(this)
                    val filePath = imageCompressor.compress(uri)
                    mainViewModel.addImageToDocument(docId, filePath) {
                        CO.log("Image Added to Document: ${it.message}")
                    }
//                    val intent = Intent(this, EditingActivity::class.java)
//                    intent.putExtra("ImagePath", uri.path)
//                    startActivity(intent)
                }
            }
        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DocView(this, getImageFromGallery, mainViewModel)
                }
            }
        }
    }
}

@Composable
fun DocView(
    context: Context,
    getImageFromGallery: ActivityResultLauncher<String>?,
    mainViewModel: MainViewModel
) {
    val list = mainViewModel.loadImagesResponse.observeAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn() {
                items(list.value ?: listOf()) { item ->
                    Row() {
                        AsyncImage(model = item?.imagePath, contentDescription = "Image")
                    }
                }
            }
        }

//        Add Button
        FloatingActionButton(
            onClick = {
                getImageFromGallery?.launch("image/*")
            },
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RectangleShape,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Document")
        }

    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun DefaultPreview3() {
    DocMakerTheme {
        DocView(
            LocalContext.current,
            null,
            MainViewModel(AppDatabase.getInstance(LocalContext.current))
        )
    }
}