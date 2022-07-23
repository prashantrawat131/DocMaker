package com.oxodiceproductions.dockmaker.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageDao {
    @Query("select * from Image")
    List<Image> getAll();

    @Insert
    void insert(Image image);

    @Delete
    void delete(Image image);

    @Query("update Image set imageIndex=:newIndex where id=:id")
    void updateIndex(int id, int newIndex);
}
