package com.oxodiceproductions.dockmaker.ui.compose.activity.image_edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.utils.CO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditingImageViewModel : ViewModel() {
    val saveImageResponse = MutableLiveData<Boolean>()
    val imageCropResponse = MutableLiveData<Bitmap?>()

     fun cropImage(bitmap: ImageBitmap) {

    }

    fun rotateImage(imagePath: String, degrees: Float, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    Matrix().apply {
                        postRotate(degrees)
                    },
                    false
                )
                val file = File(imagePath)
                file.delete()
                withContext(Dispatchers.IO) {
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
                    saveImageResponse.value = true
                }
            } catch (e: Exception) {
                onException(e)
                saveImageResponse.value = false
            }
        }
    }
}