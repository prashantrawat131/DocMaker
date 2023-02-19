package com.oxodiceproductions.dockmaker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Document::class, Image::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun imageDao(): ImageDao

    companion object {
        fun getInstance(context: Context?): AppDatabase {
            return Room.databaseBuilder(
                context!!,
                AppDatabase::class.java, "DocMakerRoomDB"
            ).build()
        }
    }
}