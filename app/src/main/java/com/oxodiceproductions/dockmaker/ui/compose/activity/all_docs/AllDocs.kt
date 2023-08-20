package com.oxodiceproductions.dockmaker.ui.compose.activity.all_docs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.ui.compose.activity.document_view.DocumentView
import com.oxodiceproductions.dockmaker.ui.compose.components.DocumentPreviewItem
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllDocs : ComponentActivity() {

    private lateinit var viewModel: AllDocViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AllDocViewModel::class.java]

        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    Main(this, viewModel)
                }
            }
        }

        viewModel.addDocResponse.observe(this) {
            Log.d("AllDocs", "addDocResponse: $it")
            val intent = Intent(this, DocumentView::class.java)
            intent.putExtra(Constants.SP_DOC_ID, it)
            intent.putExtra("first_time", false)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllDocs {
            CO.log(it.message ?: "")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Main(context: Context, viewModel: AllDocViewModel) {

    LaunchedEffect(Unit) {
        viewModel.getAllDocs {
            CO.log("getAllDocs: $it")
        }
    }

    val list = viewModel.allDocsList
    val isSelectedModeOn = viewModel.isSelectionModeOn

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isSelectedModeOn.value) {
                Row {
                    Button(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete selected documents")
                        Text(text = "Delete")
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share selected documents")
                        Text(text = "Share")
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Check, contentDescription = "Select all documents")
                        Text(text = "Select All")
                    }
                }
            } else {
                Text(
                    text = "Documents",
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(16.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontSize = 24.sp,
                )
            }

            LazyColumn() {
                items(list) { item ->
                    DocumentPreviewItem(item = item, isSelectedModeOn.value, {
                        val intent = Intent(context, DocumentView::class.java)
                        intent.putExtra(Constants.SP_DOC_ID, item.id)
                        intent.putExtra("first_time", false)
                        context.startActivity(intent)
                    }, { docId ->
                        CO.log("Long press")
                        viewModel.selectDocument(docId) {
                            CO.log("selectDocument: $it")
                        }
                    })
                }
            }

            if (list.size == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, true)
                ) {
                    Icon(
                        Icons.Filled.Face,
                        contentDescription = "No Documents image"
                    )

                    Text(text = "No Documents Yet.")
                }
            }
        }
        FloatingActionButton(
            onClick = {
                viewModel.addDocument {
                    CO.log("addDocument: $it")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(80.dp)
                .height(80.dp)
                .padding(16.dp),
            backgroundColor = Color(0xFFF3B96D),
            shape = RoundedCornerShape(8.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Document")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DefaultPreview() {
    DocMakerTheme {
        Main(LocalContext.current, AllDocViewModel(AppDatabase.getInstance(LocalContext.current)))
    }
}