package com.oxodiceproductions.dockmaker.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Document.class, Image.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DocumentDao documentDao();

    public abstract ImageDao imageDao();

    public static AppDatabase getInstance(Context context){
        return Room.databaseBuilder(context,AppDatabase.class,"DocMakerRoomDB").build();
    }
}