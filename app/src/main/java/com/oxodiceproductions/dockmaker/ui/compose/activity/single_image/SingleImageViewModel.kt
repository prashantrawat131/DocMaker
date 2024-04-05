package com.oxodiceproductions.dockmaker.ui.compose.activity.single_image

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SingleImageViewModel @Inject constructor(val database: AppDatabase) : ViewModel() {
    val deleteImageResponse = MutableLiveData<Int>()
    val loadImageResponse = MutableLiveData<String>()
    val downloadImageResponse = MutableLiveData<Boolean>()

    fun downloadImage(
        context: Context,
        onException: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val downloadsFolder =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                val docName = CO.getUniqueName(Constants.imageExtension, 0)

                val file = File("$downloadsFolder/$docName")
                val fileOutputStream = FileOutputStream(file)
                val fileInputStream = FileInputStream(loadImageResponse.value)
                fileOutputStream.write(fileInputStream.readBytes())

                fileOutputStream.close()
                fileInputStream.close()
                downloadImageResponse.value = true
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun deleteImage(
        image: Image,
        onException: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val imageDao = database.imageDao()
                deleteImageResponse.value = imageDao.deleteImageByPath(image.imagePath)
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun loadImage(imagePath: String, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                loadImageResponse.value = imagePath
            } catch (e: Exception) {
                onException(e)
            }
        }
    }
}