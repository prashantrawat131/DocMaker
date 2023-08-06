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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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

    private lateinit var mainViewModel: AllDocViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this)[AllDocViewModel::class.java]

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

    override fun onResume() {
        super.onResume()
        mainViewModel.getAllDocs {
            CO.log(it.message?:"")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Main(context: Context, mainViewModel: AllDocViewModel) {

    LaunchedEffect(Unit) {
        mainViewModel.getAllDocs {
            CO.log("getAllDocs: $it")
        }
    }

    val list = mainViewModel.allDocsList.observeAsState()

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
                items(list.value ?: listOf()) { item ->
                    DocumentPreviewItem(item = item) {
                        val intent = Intent(context, DocumentView::class.java)
                        intent.putExtra(Constants.SP_DOC_ID, item?.id)
                        intent.putExtra("first_time", false)
                        context.startActivity(intent)
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
        Main(LocalContext.current, AllDocViewModel(AppDatabase.getInstance(LocalContext.current)))
    }
}