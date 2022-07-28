package com.oxodiceproductions.dockmaker.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageDao {
    @Query("select * from Image")
    List<Image> getAll();

    @Insert
    long insert(Image image);

    @Delete
    void delete(Image image);

   /* @Query("update Image set imageIndex=:newIndex where id=:id")
    void updateIndex(long id, int newIndex);*/

    @Query("delete from Image")
    void deleteAll();

    @Query("select * from Image where docId=:docId")
    List<Image> getImagesByDocId(long docId);

    @Query("delete from Image where imagePath=:imagePath")
    void deleteImageByPath(String imagePath);

    @Update
    void update(Image image);

    @Query("select * from Image where id=:id and imageIndex=:imageIndex")
    Image getImageByIndex(long id,int imageIndex);
}
