package com.oxodiceproductions.dockmaker.database

import androidx.room.*

@Dao
interface ImageDao {
    @Query("select * from Image")
    suspend fun all(): List<Image?>?

    @Insert
    suspend fun insert(image: Image?): Long

    @Delete
    suspend fun delete(image: Image?)

    /* @Query("update Image set imageIndex=:newIndex where id=:id")
    void updateIndex(long id, int newIndex);*/
    @Query("delete from Image")
    suspend fun deleteAll()

    @Query("select * from Image where docId=:docId")
    suspend fun getImagesByDocId(docId: Long): List<Image?>

    @Query("delete from Image where imagePath=:imagePath")
    suspend fun deleteImageByPath(imagePath: String)

    @Update
    suspend fun update(image: Image)

    @Query("select * from Image where id=:id and imageIndex=:imageIndex")
    suspend fun getImageByIndex(id: Long, imageIndex: Int): Image

    @Query("select * from Image where docId=:docId and imagePath=:imagePath")
    suspend fun getImageByImagePath(docId: Long, imagePath: String?): Image
}