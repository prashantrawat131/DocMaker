package com.oxodiceproductions.dockmaker.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Image {
    @PrimaryKey
    int id;

    @ColumnInfo(name = "imagePath")
    String imagePath;

    @ColumnInfo(name = "imageIndex")
    int imageIndex;

    public Image(String imagePath, int imageIndex) {
        this.imagePath = imagePath;
        this.imageIndex = imageIndex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}
