package com.oxodiceproductions.dockmaker.ui.compose.activity.camera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.ui.compose.activity.image_edit.EditingActivity
import com.oxodiceproductions.dockmaker.ui.compose.ui.theme.DocMakerTheme
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import java.io.File

class CameraActivity : ComponentActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        CO.log("CameraActivity");
//        val docId = intent.extras?.getLong(Constants.docId)
        setContent {
            DocMakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(1f),
                    color = MaterialTheme.colors.background
                ) {
                    AppContent(cameraProviderFuture, returnActivity = {
                        fileName->
                        val resultIntent= Intent().apply {
                            putExtra(Constants.imagePath, fileName)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    })
                }
            }
        }
    }
}

@Composable
fun AppContent(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    returnActivity: (String) -> Unit
) {
    val context = LocalContext.current
    val previewView = PreviewView(LocalContext.current)
    val cameraProvider = cameraProviderFuture.get()
    val preview = androidx.camera.core.Preview.Builder().build()
    val cameraSelector =
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

    val imageCapture =
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    val fileName = CO.getUniqueName("jpg", 0)
    val capturedImage = File(context.filesDir, fileName)
    val imageCaptureOutputOptions =
        ImageCapture.OutputFileOptions.Builder(capturedImage).build()

    val camera =
        cameraProvider.bindToLifecycle(
            LocalLifecycleOwner.current,
            cameraSelector,
            preview,
            imageCapture
        )

    Box(modifier = Modifier.fillMaxSize(1f)) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize(1f)) { view ->
            preview.setSurfaceProvider(view.surfaceProvider)
        }
        Button(onClick = {
            imageCapture.takePicture(imageCaptureOutputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        CO.log("Image saved")
                        returnActivity(capturedImage.path)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        CO.log("Image not saved")
                        CO.log(exception.message.toString())
                    }
                })
        }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), shape = androidx.compose.foundation.shape.CircleShape) {
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_photo_camera_24),
                contentDescription = "Click to capture image"
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    DocMakerTheme {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(LocalContext.current)
        AppContent(cameraProviderFuture, {})
    }
}