package com.oxodiceproductions.dockmaker.ui.compose

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Document
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val database: AppDatabase) : ViewModel() {
    val TAG = "MainViewModel"
    val allDocsResponse = MutableLiveData<List<Document?>>()
    val addDocResponse = MutableLiveData<Long>()

    fun getAllDocs(onException: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                allDocsResponse.postValue(database.documentDao().all)
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
}