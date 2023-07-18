package com.oxodiceproductions.dockmaker.ui.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SplashView(this) {
                        val intent = Intent(this, AllDocs::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun SplashView(context: Context, goToMainActivity: () -> Unit) {

//    After 2 seconds, go to the main activity
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
        goToMainActivity()
    }, 2000)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val rotation by animateFloatAsState(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon_orange_foreground),
                contentDescription = "Image",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer(
                        rotationZ = rotation
                    )
            )

            Text(
                text = "DocMaker",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    DocMakerTheme {
        SplashView(LocalContext.current){

        }
    }
}