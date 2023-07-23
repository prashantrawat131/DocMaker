package com.oxodiceproductions.dockmaker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DocumentDao {
    @Query("select * from Document order by time DESC")
    suspend fun getAll(): List<Document?>?

    @Insert
    suspend fun insert(document: Document?): Long

    @Delete
    suspend fun delete(document: Document?)

    @Query("update Document set name=:name where id=:id")
    suspend fun updateName(id: Long, name: String?)

    @Query("delete from Document")
    suspend fun deleteAll()

    @Query("select * from Document where id=:DocId")
    suspend fun getDocById(DocId: Long): List<Document?>?

    @Query("delete from Document where id=:DocId")
    suspend fun deleteDocById(DocId: Long)
}