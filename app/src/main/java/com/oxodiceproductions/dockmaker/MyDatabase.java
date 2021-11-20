package com.oxodiceproductions.dockmaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;

public class MyDatabase extends SQLiteOpenHelper {
    public MyDatabase(@Nullable Context context) {
        super(context, "DocMakerDatabase", null, 2);
    }

    /*
        There is a table called document which consist of all the documents.
        Each document present in the documents table also have unique tables
        of their own which has a name of DocId. Each DocId table consist of one column called ImagePaths
        which is the path of the images present in that document*/
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /* here each document is created with unique DocId
         * */
        sqLiteDatabase.execSQL("create table if not exists documents(DocId varchar,SampleImageId varchar,DateCreated varchar,TimeCreated varchar,DocName varchar)");
    }

    public void InsertDocument(String DocId, String SampleImageId, String DateCreated, String TimeCreated, String DocName) {
        //whenever a new document is created it is stored with the help of this function
        ContentValues contentValues = new ContentValues();
        contentValues.put("DocId", DocId);//0
        contentValues.put("SampleImageId", SampleImageId);//1
        contentValues.put("DateCreated", DateCreated);//2
        contentValues.put("TimeCreated", TimeCreated);//3
        contentValues.put("DocName", DocName);//4

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.insert("documents", null, contentValues);
    }

    public void retake(String DocId, String oldFileName, String newFileName) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        try {
            String sql = "update '" + DocId + "' set ImagePath='" + newFileName + "' where ImagePath='" + oldFileName + "'";
            sqLiteDatabase.execSQL(sql);
        } catch (SQLException ignored) {
        }
    }

    public Cursor LoadDocuments() {
        /* It gives all the documents
         * in the form of a sql cursor.*/
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("select * from documents", null);
    }

    public Cursor LoadDocumentById(String DocId) {
        /* It gives a particular document by its DocId
         * It gives it in the form of a cursor object, I may change it in future*/
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cc = sqLiteDatabase.rawQuery("select * from documents where DocId = '" + DocId + "'", null);
        cc.moveToFirst();
        return cc;
    }

    public String GetDocumentName(String DocId) {
        /* It simply returns the name of the document by it DocId.
         * Initially the DocId and document name are same but
         * the user may change the document name */
        Cursor cc = LoadDocumentById(DocId);
        cc.moveToFirst();
        return cc.getString(4);
    }

    public void SetDocumentName(String DocId, String DocName) {
		/*It is used to change the name of the document.
		Two documents can have same name so unique DocId is used for differentiation
		 */
        Cursor cc = LoadDocumentById(DocId);
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from documents where DocId = '" + DocId + "'");
        String sampleImageId = cc.getString(1);
        String dateCreated = cc.getString(2);
        String timeCreated = cc.getString(3);
        InsertDocument(DocId, sampleImageId, dateCreated, timeCreated, DocName);
    }

    public String getNumberOfPics(String DocId) {
		/*It return the number of pics present in the document.
		It uses another function to get the imagePaths for a particular document.
		 */
        Cursor cc = LoadImagePaths(DocId);
        cc.moveToFirst();
        return cc.getCount() + "";
    }

    public String getSize(String DocId) {
        /* It is used to get the size of the document by
         * adding size of all the images*/
        Cursor cc = LoadImagePaths(DocId);
        cc.moveToFirst();
        long size = 0;
        do {
            File file = new File(cc.getString(0));
            size += file.length();
        } while (cc.moveToNext());
        return size + "";
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void CreateTable(String DocId) {
        /* Here the unique table for each document is created
         * It consist of image paths of the images present in the document.
         * The name of the table is same as the DocId of the document*/
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("create table if not exists '" + DocId + "'(ImagePath varchar)");
    }

    public void InsertImage(String DocId, String ImagePath) {
        /* It is used to store the image paths in the unique table
         * created for each document*/
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ImagePath", ImagePath);
        sqLiteDatabase.insert(DocId, null, contentValues);
    }


    public Cursor LoadImagePaths(String DocId) {
        /* It returns a cursor document containing all the image paths of a particular document*/
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("select * from '" + DocId + "'", null);
    }

    public void updateDoc(String ImagePath1, String ImagePath2, String DocId) {
        /*This function is used when the user wants to change the
         * order of the images in the document*/
        String temp = "temp";
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("update '" + DocId + "'set ImagePath = '" + temp + "' where ImagePath='" + ImagePath1 + "'");
        sqLiteDatabase.execSQL("update '" + DocId + "'set ImagePath = '" + ImagePath1 + "' where ImagePath='" + ImagePath2 + "'");
        sqLiteDatabase.execSQL("update '" + DocId + "'set ImagePath = '" + ImagePath2 + "' where ImagePath='" + temp + "'");
    }

    public void DeleteImage(String ImagePath, String DocId) {
        /*To delete an image from the document i.e. the imagePath from the unique document table */
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from '" + DocId + "' where ImagePath = '" + ImagePath + "'");
    }

    public void DeleteTable(String DocId) {
        /*To completely delete the document*/
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from '" + DocId + "'");
        sqLiteDatabase.execSQL("drop table '" + DocId + "'");
        sqLiteDatabase.execSQL("delete from documents where DocId='" + DocId + "'");
    }
}
