package com.oxodiceproductions.dockmaker;

import android.content.Context;

import java.io.File;
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
}
