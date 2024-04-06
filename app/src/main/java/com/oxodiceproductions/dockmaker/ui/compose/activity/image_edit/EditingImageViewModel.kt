package com.oxodiceproductions.dockmaker.ui.compose.activity.image_edit

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImagePicker
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class EditingImageViewModel @Inject constructor(val database: AppDatabase) : ViewModel() {
    val saveImageResponse = MutableLiveData<Boolean>()
    val imageCropResponse = MutableLiveData<ImageBitmap?>()
    val imageCropper = ImageCropper()

    fun cropImage(imagePath: String, context: Context) {
        viewModelScope.launch {
            when (val result = imageCropper.crop(Uri.fromFile(File(imagePath)), context)) {
                is CropResult.Cancelled -> {
                    CO.log("Crop cancelled")
                    imageCropResponse.value = null
                }

                is CropError -> {
                    CO.log("Crop error: ${result.name}")
                    imageCropResponse.value = null
                }

                is CropResult.Success -> {
                    CO.log("Crop success")
                    imageCropResponse.value = result.bitmap
                }
            }
        }
    }

    fun saveImage(
        context: Context,
        docId: Long,
        imageBitmap: ImageBitmap,
        oldImagePath: String,
        newImage: Boolean?
    ) {
        viewModelScope.launch {
            try {
                val file = File(oldImagePath)
                file.delete()

                val imageDao = database.imageDao()
                if (newImage == true) {
                    imageBitmap.asAndroidBitmap()
                        .compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))

                    val index = imageDao.all()?.size ?: 0
                    val imgObj = Image(
                        oldImagePath, index, docId
                    )
                    val res: Long = imageDao.insert(imgObj)
                    saveImageResponse.value = (res > 0)
                } else {
                    val newFile =
                        File(context.filesDir, CO.getUniqueName(Constants.imageExtension, 0))
                    imageBitmap.asAndroidBitmap()
                        .compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(newFile))
                    val imgObj = imageDao.getImageByImagePath(docId, oldImagePath)
                    imgObj.imagePath = newFile.absolutePath
                    imageDao.update(imgObj)
                    saveImageResponse.value = true
                }

                CO.log("Image saved successfully")
            } catch (e: Exception) {
                saveImageResponse.value = false
                CO.log("Error saving image: ${e.message}")
            }
        }
    }

    /*
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
        }*/
}