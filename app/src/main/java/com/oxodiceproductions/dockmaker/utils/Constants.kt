package com.oxodiceproductions.dockmaker.utils

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Constants {
    const val appName="DockMaker"

    const val TAG = "tagJi"
    const val PROPER_DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy"
    const val SP_DOC_ID = "DocId"
    const val docId = "docId"
    const val imagePath = "imagePath"
    const val imageExtension = "jpg"
    const val newImage="newImage"


    enum class EditingState {
        NONE, ROTATE, CROP, FILTER, TEXT, STICKER
    }


//    UI Constants
     val brush:Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF3F51B5),
            Color(0xFF303F9F),
        )
    )
}
