package com.oxodiceproductions.dockmaker.model

data class DocumentPreviewModel(
    var id: Long,
    var name: String,
    var image: String?,
    var dateTime: DateTime,
    var imageCount: Int
) {
    class DateTime(var date: String, var time: String)
}
