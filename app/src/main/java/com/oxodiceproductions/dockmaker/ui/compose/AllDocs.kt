package com.oxodiceproductions.dockmaker.ui.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Main(this, mainViewModel)
                }
            }
        }

        mainViewModel.getAllDocs {
            CO.log("getAllDocs: $it")
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

@Composable
fun Main(context: Context, mainViewModel: MainViewModel) {

      val (list,setList) = remember {
          mutableStateOf<List<Document?>?>(null)
      }
   /* val list = remember {
        MutableLiveData<List<Document?>>()
    }
*/
    mainViewModel.allDocsResponse.observe(context as ComponentActivity) {
        setList(it)
        CO.log("Value Changed: ${list}")
    }

    fun getPreviewImage(document: Document?):ImageRequest{
//        Extract the first image from the document and return it.
        return ImageRequest.Builder(context)
            .data(R.drawable.ic_baseline_add_24)
            .target {
                Log.d("AllDocs", "getPreviewImage: $it")
            }
            .build()
    }

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
                items(list ?: listOf(Document(3232L,"fake data"))) { item ->
                    Card() {
                        Row() {
                            Image(painter = painterResource(id = R.drawable.app_icon_orange_foreground), contentDescription = "Preview Image" ,modifier = Modifier
                                .width(64.dp)
                                .height(64.dp)
                                .padding(16.dp)
                                .align(Alignment.CenterVertically)
                            )
                            Text(
                                text = item?.name?:"",
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

        Button(
            onClick = {
                mainViewModel.addDocument {
                    CO.log("addDocument: $it")
                }
            },
            modifier = Modifier
                .width(64.dp)
                .height(64.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Add Document",
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    androidx.compose.ui.graphics.Color(0xFF000000)
                )
            )
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