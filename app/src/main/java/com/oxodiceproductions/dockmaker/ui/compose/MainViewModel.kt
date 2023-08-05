package com.oxodiceproductions.dockmaker.ui.compose

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Document
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.model.DocumentPreviewModel
import com.oxodiceproductions.dockmaker.utils.CO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val database: AppDatabase) : ViewModel() {
    val TAG = "MainViewModel"
    val allDocsList = MutableLiveData<List<DocumentPreviewModel>>()
    val allDocsResponse = MutableLiveData<List<Document?>>()
    val addDocResponse = MutableLiveData<Long>()
    val loadDocumentResponse = MutableLiveData<Document?>()
    val addImageResponse = MutableLiveData<Long>()
    val loadImagesResponse = MutableLiveData<List<Image?>>()
    val previewImageResponse = MutableLiveData<HashMap<Long, String>>()

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


    fun getAllDocs(onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val documents = database.documentDao().getAll()
                val imageDao = database.imageDao()
                val previewDocuments = arrayListOf<DocumentPreviewModel>()
//                allDocsResponse.value=documents
                documents?.forEach { doc ->
                    val images = imageDao.getImagesByDocId(doc!!.id)
                    var imagePath: String? = null
                    var imageCount = 0
                    if ((images?.size ?: 0) > 0) {
                        imagePath = images?.get(0)?.imagePath
                        imageCount = images?.size ?: 0
                    }
                    val displayDocument = DocumentPreviewModel(
                        doc?.id!!,
                        doc?.name ?: "No Name",
                        imagePath,
                        CO.getDocTime(doc?.id!!),
                        imageCount
                    )
                    previewDocuments.add(displayDocument)
                }
                allDocsList.value = previewDocuments
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

    /* private fun getPreviewImage(
         onException: (Exception) -> Unit
     ) {
         viewModelScope.launch {
             try {
                 val imageDao = database.imageDao()
                 val hashMap = HashMap<Long, String>()
                 allDocsResponse.value?.forEach { doc ->
                     val imagesPath = imageDao.getImagesByDocId(doc!!.id)[0]?.imagePath
                     CO.log("Image Path: $imagesPath")
                     hashMap[doc.id] = imagesPath ?: ""
                 }
                 if (hashMap.isNotEmpty()) {
                     previewImageResponse.value = hashMap
                 }
             } catch (e: Exception) {
                 onException(e)
             }
         }
     }*/
}