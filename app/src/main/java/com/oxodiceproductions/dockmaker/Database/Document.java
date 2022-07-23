package com.oxodiceproductions.dockmaker.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Document {
    @PrimaryKey
    int DocId;

    @ColumnInfo(name = "time")
    long time;

    @ColumnInfo(name = "name")
    String DocName;

    public Document(long time, String docName) {
        this.time = time;
        DocName = docName;
    }

    public int getDocId() {
        return DocId;
    }

    public void setDocId(int docId) {
        DocId = docId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDocName() {
        return DocName;
    }

    public void setDocName(String docName) {
        DocName = docName;
    }
}
