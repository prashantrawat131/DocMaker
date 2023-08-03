package com.oxodiceproductions.dockmaker.ui.compose

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Document
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllDocs : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    Main(this, mainViewModel)
                }
            }
        }

        mainViewModel.addDocResponse.observe(this) {
            Log.d("AllDocs", "addDocResponse: $it")
            val intent = Intent(this, DocumentView::class.java)
            intent.putExtra(Constants.SP_DOC_ID, it)
            intent.putExtra("first_time", false)
            startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Main(context: Context, mainViewModel: MainViewModel) {

    LaunchedEffect(Unit) {
        mainViewModel.getAllDocs {
            CO.log("getAllDocs: $it")
        }
    }

    val list = mainViewModel.allDocsResponse.observeAsState()

    val previewImageMap = mainViewModel.previewImageResponse.observeAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Documents",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(16.dp),
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                fontSize = 24.sp,
            )

            LazyColumn() {
                items(list.value ?: listOf(Document(3232L, ""))) { item ->
                    Card(
                        elevation = 2.dp,
                        onClick = {
                            val intent = Intent(context, DocumentView::class.java)
                            intent.putExtra(Constants.SP_DOC_ID, item?.id)
                            intent.putExtra("first_time", false)
                            context.startActivity(intent)
                        }
                    ) {
                        Row() {
                            if (previewImageMap.value?.get(item?.id) != null) {
                                AsyncImage(model = previewImageMap.value?.get(item?.id), contentDescription = "Preview Image")
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_baseline_broken_image_24),
                                    contentDescription = "Preview Image",
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(64.dp)
                                        .padding(16.dp)
                                        .align(Alignment.CenterVertically),
                                    colorFilter = ColorFilter.tint(Color.Gray)
                                )
                            }
                            Text(
                                text = item?.name ?: "",
                                modifier = Modifier
                                    .fillMaxWidth(1f)
                                    .padding(16.dp),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                mainViewModel.addDocument {
                    CO.log("addDocument: $it")
                }
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DefaultPreview() {
    DocMakerTheme {
        Main(LocalContext.current, MainViewModel(AppDatabase.getInstance(LocalContext.current)))
    }
}