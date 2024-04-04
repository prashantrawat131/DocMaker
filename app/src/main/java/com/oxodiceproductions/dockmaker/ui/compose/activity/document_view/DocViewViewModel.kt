package com.oxodiceproductions.dockmaker.ui.compose.activity.document_view

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Document
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.PDFMaker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class DocViewViewModel @Inject constructor(val database: AppDatabase, val appContext: Application) :
    ViewModel() {
    val addImageResponse = MutableLiveData<Long>()
    val loadDocumentResponse = MutableLiveData<Document?>()

    private val loadImagesResponse = MutableLiveData<List<Image?>>()
    val images:LiveData<List<Image?>> get() = loadImagesResponse

    val renameDocResponse = MutableLiveData<Int>()
    val deleteDocResponse = MutableLiveData<Boolean>()
    val tempPdfResponse = MutableLiveData<String>()
    val sharePdfResponse = MutableLiveData<String>()
    val downloadPdfResponse=MutableLiveData<Boolean>()

    private fun makePdf(): String {
        try {
            val arrayList = ArrayList<Image>()
            loadImagesResponse.value?.forEach {
                CO.log("Adding Image to temp list: ${it?.imagePath}")
                arrayList.add(it!!)
            }
            return PDFMaker(appContext).MakeTempPDF(
                arrayList,
                loadDocumentResponse.value?.name ?: "Document"
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun downloadPdf(onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val tempFilePath = makePdf()
                val downloadsFolder =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                val docName=loadDocumentResponse.value?.name?:"Document"

                val fileOutputStream = FileOutputStream("$downloadsFolder/$docName.pdf")
                val fileInputStream = FileInputStream(tempFilePath)
                fileOutputStream.write(fileInputStream.readBytes())

                fileOutputStream.close()
                fileInputStream.close()

                downloadPdfResponse.value = true
            } catch (e: Exception) {
                downloadPdfResponse.value = false
                onException(e)
            }
        }
    }

    fun sharePdf(context: Context, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                sharePdfResponse.value = makePdf()
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun makeTempPDF(context: Context, onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                tempPdfResponse.value = makePdf()
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
                loadImagesResponse.value = imageDao.getImagesByDocId(docId)
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
                if (renameDocResponse.value!! > 0) {
                    getDocById(docId) {
                        CO.log("Error while loading doc in DocViewModel: ${it.message}")
                    }
                }
            } catch (e: Exception) {
                onException(e)
            }
        }
    }

    fun deleteDoc(docId: Long) {
        viewModelScope.launch {
            try {
                val docDao = database.documentDao()
                docDao.delete(docDao.getDocById(docId)!![0])
                deleteDocResponse.value = true
            } catch (e: Exception) {
                deleteDocResponse.value = false
            }
        }
    }
}