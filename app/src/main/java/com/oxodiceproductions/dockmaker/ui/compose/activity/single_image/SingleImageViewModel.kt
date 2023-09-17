package com.oxodiceproductions.dockmaker.ui.compose.activity.single_image

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Image
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingleImageViewModel @Inject constructor(val database: AppDatabase) : ViewModel() {
    val deleteImageResponse = MutableLiveData<Int>()
    val loadImageResponse = MutableLiveData<String>()

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