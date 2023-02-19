package com.oxodiceproductions.dockmaker.ui.activity.camera

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.databinding.ActivityMyCameraBinding
import com.oxodiceproductions.dockmaker.ui.activity.document_view.DocumentViewActivity
import com.oxodiceproductions.dockmaker.ui.activity.editing.EditingImageActivity
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.ImageCompressor
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit


class CameraActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var imageCapture: ImageCapture
    lateinit var executor: Executor
    var DocId: Long = -2
    lateinit var camera: Camera
    lateinit var settingsSharedPreferences: SharedPreferences
    lateinit var sharedPreferences: SharedPreferences
    var ImagePath = "-1"
    var retakeImagePath = "-1"
    lateinit var cameraControl: CameraControl
    lateinit var capturedImage: File
    lateinit var binding: ActivityMyCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCameraBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

//        previewView = findViewById(R.id.previewView);
//        camera_menu = findViewById(R.id.camera_menu);
//        flash_button = findViewById(R.id.flash_button);
//        progressBar = findViewById(R.id.progress_bar_camera);
//        captureImageButton = findViewById(R.id.camera_button);
        settingsSharedPreferences = getSharedPreferences("DocMakerSettings", MODE_PRIVATE)
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE)
        try {
            //taking DocId
            //also taking ImagePath in case of retake image
            DocId = intent.extras!!.getLong("DocId", -1)
            ImagePath = intent.extras!!.getString("ImagePath", "-1")
            retakeImagePath = ImagePath
        } catch (ignored: Exception) {
        }
        if (sharedPreferences!!.getBoolean("flash", false)) {
            binding!!.flashButton.setImageDrawable(getDrawable(R.drawable.flash_on))
        }
        Setup()
        binding!!.progressBarCamera.visibility = View.GONE
        binding!!.previewView.setOnClickListener { view ->
            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(0f, 0f)
            val point = factory.createPoint(0f, 0f)
            val action =
                FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .addPoint(point, FocusMeteringAction.FLAG_AE) // could have many
                    // auto calling cancelFocusAndMetering in 5 seconds
                    .setAutoCancelDuration(5, TimeUnit.SECONDS)
                    .build()
            cameraControl!!.startFocusAndMetering(action)
        }
        binding!!.cameraButton.setOnClickListener { view ->
            binding!!.cameraMenu.visibility = View.GONE
            binding!!.progressBarCamera.visibility = View.VISIBLE
            try {
                val out =
                    FileOutputStream(capturedImage) //Use the stream as usual to w
                val outputFileOptions =
                    ImageCapture.OutputFileOptions.Builder(out).build()
                imageCapture!!.takePicture(
                    outputFileOptions,
                    executor!!,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {}
                        override fun onError(exception: ImageCaptureException) {}
                    })
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        binding!!.flashButton.setOnClickListener { view ->
            val editor = sharedPreferences!!.edit()
            //if flash was on then it needs to be off on click and vice-verse
            val flashStateAfterClick = !sharedPreferences!!.getBoolean("flash", false)
            editor.putBoolean("flash", flashStateAfterClick)
            editor.apply()
            if (flashStateAfterClick) {
                imageCapture!!.flashMode = ImageCapture.FLASH_MODE_ON
                binding!!.flashButton.setImageDrawable(getDrawable(R.drawable.flash_on))
            } else {
                imageCapture!!.flashMode = ImageCapture.FLASH_MODE_OFF
                binding!!.flashButton.setImageDrawable(getDrawable(R.drawable.flash_off))
            }
        }
    }

    fun Setup() {
        try {
            capturedImage = File.createTempFile("capturedImage", "jpeg")
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            GoToDocumentView()
        }
        if (capturedImage!!.exists()) capturedImage!!.delete()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture!!.addListener({
            try {
                val cameraProvider =
                    cameraProviderFuture!!.get()
                bindPreview(cameraProvider)
            } catch (ignored: ExecutionException) {
            } catch (ignored: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()

        //selection of camera i.e. back camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        //attaching surface provider
        preview.setSurfaceProvider(binding!!.previewView.surfaceProvider)

        //after image capture, executor is called
        executor = Executor { runnable: Runnable? -> GoToCompress() }
        imageCapture = if (sharedPreferences!!.getBoolean("flash", false)) {
            ImageCapture.Builder()
                .setFlashMode(ImageCapture.FLASH_MODE_ON)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(binding!!.previewView.display.rotation)
                .build()
        } else {
            ImageCapture.Builder()
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(binding!!.previewView.display.rotation)
                .build()
        }
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        cameraControl = camera!!.cameraControl
    }

    fun GoToCompress() {
        //Image compression
        val imageCompressor = ImageCompressor(applicationContext)
        val finalFilePath = imageCompressor.compress(capturedImage)

        //deleting old image
        capturedImage!!.delete()

        //setting imagePath and going for image editing
        ImagePath = finalFilePath
        GoToCrop()
    }

    private fun GoToCrop() {
        val `in` = Intent(this@CameraActivity, EditingImageActivity::class.java)
        //        Log.d(TAG, "GoToCrop: "+new File(ImagePath).length());
        `in`.putExtra("ImagePath", ImagePath)
        //        in.putExtra("DocId", DocId);
//        in.putExtra("fromCamera", true);
//        in.putExtra("retakeImagePath", retakeImagePath);
        startActivityForResult(`in`, cameraImageEditingId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraImageEditingId && resultCode == RESULT_OK) {
            val newImagePath = data!!.extras!!.getString("ImagePath")
            Thread {
                val appDatabase = AppDatabase.getInstance(applicationContext)
                val imageDao = appDatabase.imageDao()
                if (retakeImagePath == "-1") {
                    val index = imageDao.getImagesByDocId(DocId).size
                    val newImage =
                        Image(newImagePath!!, index, DocId)
                    imageDao.insert(newImage)
                    //                    myDatabase.InsertImage(DocId,newImagePath);
                } else {
                    val image =
                        imageDao.getImageByImagePath(DocId, retakeImagePath)
                    image.imagePath = newImagePath!!
                    imageDao.update(image)
                    CO.deleteFile(retakeImagePath)
                    //                    myDatabase.retake(DocId,retakeImagePath,newImagePath);
                }
                runOnUiThread { GoToDocumentView() }
            }.start()

            /* MyDatabase myDatabase=new MyDatabase(getApplicationContext());
            if(retakeImagePath.equals("-1")){
                myDatabase.InsertImage(DocId,newImagePath);
            }
            else{
                myDatabase.retake(DocId,retakeImagePath,newImagePath);
            }
            myDatabase.close();*/
        }
    }

    private fun GoToDocumentView() {
        val `in` = Intent(this@CameraActivity, DocumentViewActivity::class.java)
        `in`.putExtra("DocId", DocId)
        startActivity(`in`)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val `in` = Intent(this@CameraActivity, DocumentViewActivity::class.java)
        `in`.putExtra("DocId", DocId)
        startActivity(`in`)
        finish()
    }

    companion object {
        const val cameraImageEditingId = 1928
    }
}