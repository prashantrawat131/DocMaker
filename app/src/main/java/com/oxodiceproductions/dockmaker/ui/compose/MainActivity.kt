package com.oxodiceproductions.dockmaker.ui.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.ui.activity.all_docs.AllDocsActivity
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Main(this)
                }
            }
        }
    }
}

@Composable
fun Main(context: Context) {
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


        }

        Button(
            onClick = {
//                      Go to alldocs acitvity
                val intent = Intent(context, AllDocsActivity::class.java)
                context.startActivity(intent)
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
        Main(LocalContext.current)
    }
}