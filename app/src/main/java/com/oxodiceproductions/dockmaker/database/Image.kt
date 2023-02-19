package com.oxodiceproductions.dockmaker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Image(
    @field:ColumnInfo(name = "imagePath") var imagePath: String, @field:ColumnInfo(
        name = "imageIndex"
    ) var imageIndex: Int, @field:ColumnInfo(name = "docId") var docId: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

}