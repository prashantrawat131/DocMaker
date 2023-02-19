package com.oxodiceproductions.dockmaker.ui.activity.editing

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.oxodiceproductions.dockmaker.databinding.ActivityEditingImageBinding
import com.oxodiceproductions.dockmaker.utils.CO
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class EditingImageActivity : AppCompatActivity() {
    var ImagePath = "-1"
    var finalFile: File? = null
    var bitmap: Bitmap? = null
    var binding: ActivityEditingImageBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditingImageBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        InitialWork()
        binding!!.progressBarEditImage.visibility = View.GONE
        binding!!.backButtonEditImage.setOnClickListener { view -> onBackPressed() }
        binding!!.rotateButtonEditImage.setOnClickListener { view ->
            binding!!.cropImageView.rotateImage(
                90
            )
        }
        binding!!.cropButtonEditImage.setOnClickListener { view ->
            binding!!.progressBarEditImage.visibility = View.VISIBLE
            //old file getting deleted
            CO.deleteFile(ImagePath)

            //creating new file name and saving it
            val uniqueName = CO.getUniqueName("jpg", 0) //date + time + ".jpg";
            val appDir = File(filesDir.path)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            finalFile = File(appDir, uniqueName)
            ImagePath = finalFile!!.path
            try {
                val fileOutputStream = FileOutputStream(finalFile)
                val bos = ByteArrayOutputStream()
                binding!!.cropImageView.croppedImage
                    .compress(Bitmap.CompressFormat.JPEG, 100, bos)
                fileOutputStream.write(bos.toByteArray())
                fileOutputStream.close()

                //sending the path of the cropped image
                val replyIntent = Intent()
                replyIntent.putExtra("ImagePath", ImagePath)
                setResult(RESULT_OK, replyIntent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            binding!!.progressBarEditImage.visibility = View.GONE
        }
    }

    private fun InitialWork() {
        ImagePath = intent.extras!!.getString("ImagePath", "-1")
        bitmap = BitmapFactory.decodeFile(ImagePath)
        binding!!.cropImageView.setImageBitmap(bitmap)
    }

    private fun Exit() {
        binding!!.progressBarEditImage.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Exit()
    }
}