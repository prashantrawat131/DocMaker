package com.oxodiceproductions.dockmaker.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DocumentDao {
    @Query("select * from Document order by time DESC")
    List<Document> getAll();

    @Insert
    long insert(Document document);

    @Delete
    void delete(Document document);

    @Query("update Document set name=:name where id=:id")
    void updateName(long id, String name);

    @Query("delete from Document")
    void deleteAll();

    @Query("select * from Document where id=:DocId")
    List<Document> getDocById(long DocId);

    @Query("delete from Document where id=:DocId")
    void deleteDocById(long DocId);
}
