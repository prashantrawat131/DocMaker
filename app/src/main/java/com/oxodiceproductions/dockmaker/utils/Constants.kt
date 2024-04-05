package com.oxodiceproductions.dockmaker.utils

object Constants {
    const val TAG = "tagJi"
    const val PROPER_DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy"
    const val SP_DOC_ID = "DocId"
    const val docId = "docId"
    const val imagePath = "imagePath"
    const val imageExtension = "jpg"


    enum class EditingState {
        NONE, ROTATE, CROP, FILTER, TEXT, STICKER
    }
}
