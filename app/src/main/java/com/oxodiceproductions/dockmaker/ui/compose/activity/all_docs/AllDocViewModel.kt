package com.oxodiceproductions.dockmaker.ui.compose.activity.all_docs

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Document
import com.oxodiceproductions.dockmaker.model.DocumentPreviewModel
import com.oxodiceproductions.dockmaker.utils.CO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class AllDocViewModel @Inject constructor(val database: AppDatabase) : ViewModel() {
    val allDocsList = mutableStateListOf<DocumentPreviewModel>()
    val addDocResponse = MutableLiveData<Long>()
    val isSelectionModeOn = mutableStateOf(false)

    fun getAllDocs(onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val documents = database.documentDao().getAll()
                val imageDao = database.imageDao()
                val previewDocuments = arrayListOf<DocumentPreviewModel>()
                documents?.forEach { doc ->
                    val images = imageDao.getImagesByDocId(doc!!.id)
                    var imagePath: String? = null
                    var imageCount = 0
                    if ((images?.size ?: 0) > 0) {
                        imagePath = images?.get(0)?.imagePath
                        imageCount = images?.size ?: 0
                    }
                    val displayDocument = DocumentPreviewModel(
                        doc.id,
                        doc.name ?: "No Name",
                        imagePath,
                        CO.getDocTime(doc.time),
                        imageCount
                    )
                    previewDocuments.add(displayDocument)
                }
                isSelectionModeOn.value = false
                allDocsList.clear()
                allDocsList.addAll(previewDocuments)
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun addDocument(onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val documentDao = database.documentDao()
                val newDocument =
                    Document(
                        Calendar.getInstance().timeInMillis, "New Document"
                    )
                addDocResponse.value = documentDao.insert(newDocument)
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun selectDocument(docId: Long, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val newList = allDocsList.map {
                    if (it.id == docId) {
                        it.isSelected = !it.isSelected
                    }
                    it
                }
                var flag = false
                newList.forEach {
                    if (it.isSelected) {
                        flag = true
                    }
                }
                isSelectionModeOn.value = flag
                allDocsList.clear()
                allDocsList.addAll(newList)
            } catch (e: Exception) {
                onException(e)
            }
        }
    }
}