package com.oxodiceproductions.dockmaker.ui.compose.activity.document_view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Document
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.utils.CO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocViewViewModel @Inject constructor(val database: AppDatabase) : ViewModel() {
    val addImageResponse = MutableLiveData<Long>()
    val loadDocumentResponse = MutableLiveData<Document?>()
    val loadImagesResponse = MutableLiveData<List<Image?>>()
    val renameDocResponse = MutableLiveData<Int>()

    fun addImageToDocument(docId: Long, imagePath: String, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val imageDao = database.imageDao()
                val index = imageDao.all()?.size ?: 0
                val newImage = Image(
                    imagePath, index, docId
                )
                addImageResponse.value = imageDao.insert(newImage)
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun loadImagesForDoc(docId: Long, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val imageDao = database.imageDao()
                loadImagesResponse.value = imageDao.getImagesByDocId(docId)
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun getDocById(docId: Long, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val document = database.documentDao().getDocById(docId)?.get(0)
                loadDocumentResponse.value = document
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun renameDoc(docId: Long, newName: String, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val docDao = database.documentDao()
                renameDocResponse.value = docDao.updateName(docId, newName)
                if(renameDocResponse.value!! >0){
                    getDocById(docId){
                        CO.log("Error while loading doc in DocViewModel: ${it.message}")
                    }
                }
            } catch (e: Exception) {
                onException(e)
            }
        }
    }
}