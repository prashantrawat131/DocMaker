package com.oxodiceproductions.dockmaker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Document(
    @field:ColumnInfo(name = "time") var time: Long, @field:ColumnInfo(
        name = "name"
    ) var name: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

}