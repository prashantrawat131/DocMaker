package com.oxodiceproductions.dockmaker.database

import androidx.room.*

@Dao
interface ImageDao {
    @get:Query("select * from Image")
    val all: List<Image?>?

    @Insert
    fun insert(image: Image?): Long

    @Delete
    fun delete(image: Image?)

    /* @Query("update Image set imageIndex=:newIndex where id=:id")
    void updateIndex(long id, int newIndex);*/
    @Query("delete from Image")
    fun deleteAll()

    @Query("select * from Image where docId=:docId")
    fun getImagesByDocId(docId: Long): List<Image?>

    @Query("delete from Image where imagePath=:imagePath")
    fun deleteImageByPath(imagePath: String)

    @Update
    fun update(image: Image)

    @Query("select * from Image where id=:id and imageIndex=:imageIndex")
    fun getImageByIndex(id: Long, imageIndex: Int): Image

    @Query("select * from Image where docId=:docId and imagePath=:imagePath")
    fun getImageByImagePath(docId: Long, imagePath: String?): Image
}