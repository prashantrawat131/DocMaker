package com.oxodiceproductions.dockmaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class PDFMaker {

    Context context;

    public PDFMaker(Context context) {
        this.context = context;
    }

    //native version
    public void Save(ParcelFileDescriptor pfd, ArrayList<String> ImagePaths, ProgressBar progressBar) {

        progressBar.setVisibility(View.VISIBLE);

        try {
            //output stream set up
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            pdfCreation(ImagePaths, fileOutputStream);
        } catch (Exception ignored) {
        }
    }


    //native android method starts here
    public String MakeTempPDF(ProgressBar progressBar, ArrayList<String> ImagePaths,String DocName) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        String filepath = "";

        try {
            //file creation
            File folder = new File(context.getCacheDir().getPath());
            if (!folder.exists()) {
                folder.mkdir();
            }
            String child = DocName+".pdf";
            File file = new File(folder, child);
            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
            pdfCreation(ImagePaths, fileOutputStream);
            filepath = file.getPath();
        } catch (Exception ignored) {
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        return filepath;
    }

    private void pdfCreation(ArrayList<String> ImagePaths, FileOutputStream fileOutputStream) throws Exception {
        int pageCount = 0;

        //document creation
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo;

        //loop for every page
        for (int i = 0; i < ImagePaths.size(); i++) {

            //loading bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(ImagePaths.get(i));

            //setting page info for current page
            PdfDocument.Page page;
            pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), pageCount).create();
            page = pdfDocument.startPage(pageInfo);

            //drawing image on canvas
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            canvas.drawBitmap(bitmap, 0, 0, paint);

            //saving page
            pdfDocument.finishPage(page);
            pageCount++;
        }

        //finally writing document and closing it
        pdfDocument.writeTo(fileOutputStream);
        pdfDocument.close();
    }
}
