package com.oxodiceproductions.dockmaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageCompressor {

    Context context;
    //offset is a variable for unique name every time
    private static int offset = 0;

    public ImageCompressor(Context context) {
        this.context = context;
    }

    public String compress(Uri uri) {
        File appDir = new File(context.getFilesDir().getPath());
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        try {
            String child = CommonOperations.getUniqueName("jpg", ++offset);
            File tempFile = new File(appDir, child);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(inputStream, outputStream);
            } else {
                byte[] bb = new byte[inputStream.available()];
                inputStream.read(bb);
                outputStream.write(bb);
            }
            return compress(tempFile);
        } catch (Exception e) {
            return "-1";
        }
    }

    //do not use bitmap.compress because it increases the size and loss of quality also happens
    public String compress(File actualImageFile) {
//        Log.d("tagJi","Image compression started");
        File destinationFile = getFile();
        try {
            //decoding only bitmap bounds and setting values for size variable
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(actualImageFile.getPath(), options);
            int width = options.outWidth;
            int height = options.outHeight;
            Size size = getQualityFactor(width, height);

            //setting inSampleSize and loading a scaled bitmap
            options.inSampleSize = calculateInSampleSize(options, size.getWidth(), size.getHeight());
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap = BitmapFactory.decodeFile(actualImageFile.getPath(), options);

            //writing scaled bitmap into a file
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

            return destinationFile.getPath();
        } catch (Exception e) {
            return "-1";
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //this function was provided by google so no interfere

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.min(heightRatio, widthRatio);
        }

        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private Size getQualityFactor(int width, int height) {
        //getting max width and maintaining the same aspect ratio
        int maxWidth = getMaxWidth();
        float fWidth = (float) width;
        float fHeight = (float) height;
        float ratio = fWidth / fHeight;
        Size size = new Size(width, height);

        if (width > maxWidth && height > maxWidth) {
            fWidth = maxWidth;
            fHeight = fWidth / ratio;
            size = new Size((int) fWidth, (int) fHeight);
        }

        return size;
    }

    private int getMaxWidth() {
        //this function returns the max width which is according to the quality
        SharedPreferences sharedPreferences = context.getSharedPreferences("DocMakerSettings", Context.MODE_PRIVATE);
        int maxWidth;
        switch (sharedPreferences.getInt("pdf_image_quality", -1)) {
            case 1:
                maxWidth = 500;
                break;
            case 3:
                maxWidth = 1500;
                break;
            default:
                maxWidth = 1000;
        }
        return maxWidth;
    }


    public File getFile() {
        //this file returns a image file for writing and it also increments the offset for different images
        String child = CommonOperations.getUniqueName("jpg", ++offset);
        File appDir = new File(context.getFilesDir().getPath());
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        return new File(appDir, child);
    }
}
