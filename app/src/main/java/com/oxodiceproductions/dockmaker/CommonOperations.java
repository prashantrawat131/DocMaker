package com.oxodiceproductions.dockmaker;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.widget.Toast;

import com.oxodiceproductions.dockmaker.Database.AppDatabase;
import com.oxodiceproductions.dockmaker.Database.Document;
import com.oxodiceproductions.dockmaker.Database.DocumentDao;
import com.oxodiceproductions.dockmaker.Database.Image;
import com.oxodiceproductions.dockmaker.Database.ImageDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class CommonOperations {

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String getUniqueName(String extension, int offset) {
        Calendar c = Calendar.getInstance();
        String time = "" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);
        String date = "" + c.get(Calendar.DATE) + c.get(Calendar.MONTH) + "_" + c.get(Calendar.YEAR) + "_" + offset;
        String name = "DocMaker" + date + time;
        return name + "." + extension;
    }

    public static File createFile(Context context, String extension) {
        File folder = new File(context.getFilesDir().getPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        String child = getUniqueName(extension, 0);
        return new File(folder, child);
    }

    public static File createTempFile(Context context, String extension) {
        File folder = new File(context.getCacheDir().getPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        String child = getUniqueName(extension, 0);
        return new File(folder, child);
    }

    public static void downloadImage(Context context,String ImagePath){
        String imageName=getUniqueName("jpg",3);
        NotificationModule notificationModule=new NotificationModule();
        notificationModule.generateNotification(context,imageName,"Go to downloads.");

        File downloadsFolder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File imageFile=new File(downloadsFolder,imageName);
        try{
            FileOutputStream fileOutputStream=new FileOutputStream(imageFile);
            FileInputStream fileInputStream=new FileInputStream(new File(ImagePath));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(fileInputStream,fileOutputStream);
            }
            else{
                byte[] bb = new byte[fileInputStream.available()];
                fileInputStream.read(bb);
                fileOutputStream.write(bb);
            }
        }catch (Exception ignored){

        }
    }

    public static void deleteDocument(Context context, long docId){
        new Thread(()->{
            AppDatabase appDatabase= AppDatabase.getInstance(context);
            DocumentDao documentDao=appDatabase.documentDao();
//            Cursor cc = myDatabase2.LoadImagePaths(docId);
            ImageDao imageDao=appDatabase.imageDao();
            ArrayList<Image> images= (ArrayList<Image>) imageDao.getImagesByDocId(docId);
            try {
                for(Image image:images){
                    CommonOperations.deleteFile(image.getImagePath());
                    imageDao.delete(image);
                }

                documentDao.deleteDocById(docId);
            } catch (Exception ignored) { }
        }).start();
        /*new Thread(()->{
            MyDatabase myDatabase2 = new MyDatabase(context);
            Cursor cc = myDatabase2.LoadImagePaths(docId);
            try {
                cc.moveToFirst();
                do {
                    CommonOperations.deleteFile(cc.getString(0));
                } while (cc.moveToNext());
            } catch (Exception ignored) { }
            myDatabase2.DeleteTable(docId);
            myDatabase2.close();
        }).start();*/
    }

    public static void log(String msg){
        Log.d(Constants.TAG, msg+"");
    }

    public static void logError(String msg){
        Log.e(Constants.TAG, msg+"");
    }

    public static void toast(String msg,Context context){
        Toast.makeText(context, ""+msg, Toast.LENGTH_SHORT).show();
    }
}
