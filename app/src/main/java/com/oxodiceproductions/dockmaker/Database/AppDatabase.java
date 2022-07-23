package com.oxodiceproductions.dockmaker.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Document.class, Image.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DocumentDao documentDao();

    public abstract ImageDao imageDao();
}
