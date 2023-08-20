package com.oxodiceproductions.dockmaker.model

data class DocumentPreviewModel(
    var id: Long,
    var name: String,
    var image: String?,
    var dateTime: DateTime,
    var imageCount: Int,
    var isSelected: Boolean = false
) {
    class DateTime(var date: String, var time: String)
}
