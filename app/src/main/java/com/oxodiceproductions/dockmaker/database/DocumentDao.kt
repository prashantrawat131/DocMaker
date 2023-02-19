package com.oxodiceproductions.dockmaker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DocumentDao {
    @get:Query("select * from Document order by time DESC")
    val all: List<Document?>?

    @Insert
    fun insert(document: Document?): Long

    @Delete
    fun delete(document: Document?)

    @Query("update Document set name=:name where id=:id")
    fun updateName(id: Long, name: String?)

    @Query("delete from Document")
    fun deleteAll()

    @Query("select * from Document where id=:DocId")
    fun getDocById(DocId: Long): List<Document?>?

    @Query("delete from Document where id=:DocId")
    fun deleteDocById(DocId: Long)
}