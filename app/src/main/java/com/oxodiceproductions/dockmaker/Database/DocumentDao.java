package com.oxodiceproductions.dockmaker.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DocumentDao {
    @Query("select * from Document")
    List<Document> getAll();

    @Insert
    void insert(Document document);

    @Delete
    void delete(Document document);

    @Query("update Document set name=:name where DocId=:id")
    void updateName(int id,String name);
}
