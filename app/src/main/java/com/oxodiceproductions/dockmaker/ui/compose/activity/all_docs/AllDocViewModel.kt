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
                        CO.getDocTime(doc.id),
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
                val c = Calendar.getInstance()
                val DocName =
                    "DocMaker" + "_" + c[Calendar.DATE] + "_" + c[Calendar.MONTH] + "_" + c[Calendar.YEAR] + "_" + c[Calendar.HOUR_OF_DAY] + "_" + c[Calendar.MINUTE] + "_" + c[Calendar.SECOND] //date.getSeconds()+"_"+date.getDate()+"_"+(date.getMonth()+1)+"_"+date.getHours()+"_"+date.getMinutes();da
                val documentDao = database.documentDao()
                val newDocument =
                    Document(
                        Calendar.getInstance().timeInMillis, DocName
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