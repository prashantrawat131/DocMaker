package com.oxodiceproductions.dockmaker.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Image {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    long id;

    @ColumnInfo(name = "imagePath")
    String imagePath;

    @ColumnInfo(name = "imageIndex")
    int imageIndex;

    @ColumnInfo(name="docId")
    long docId;

    public Image(String imagePath, int imageIndex, long docId) {
        this.imagePath = imagePath;
        this.imageIndex = imageIndex;
        this.docId = docId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }

    public long getDocId() {
        return docId;
    }

    public void setDocId(long docId) {
        this.docId = docId;
    }
}
