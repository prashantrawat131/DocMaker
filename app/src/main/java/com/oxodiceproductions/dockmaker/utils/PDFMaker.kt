package com.oxodiceproductions.dockmaker.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.oxodiceproductions.dockmaker.database.Image
import java.io.File
import java.io.FileOutputStream

class PDFMaker(var context: Context) {
    //native version
    fun Save(pfd: ParcelFileDescriptor, ImagePaths: ArrayList<Image>, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        try {
            //output stream set up
            val fileOutputStream = FileOutputStream(pfd.fileDescriptor)
            pdfCreation(ImagePaths, fileOutputStream)
        } catch (ignored: Exception) {
        }
    }

    //native android method starts here
    fun MakeTempPDF(ImagePaths: ArrayList<Image>, DocName: String): String {
//        if (progressBar != null) {
//            progressBar.setVisibility(View.VISIBLE);
//        }
        var filepath = ""
        try {
            //file creation
            val folder = File(context.cacheDir.path)
            if (!folder.exists()) {
                folder.mkdir()
            }
            val child = "$DocName.pdf"
            val file = File(folder, child)
            val fileOutputStream = FileOutputStream(file, false)
            pdfCreation(ImagePaths, fileOutputStream)
            filepath = file.path
        } catch (ignored: Exception) {
        }
        //        if (progressBar != null) {
//            progressBar.setVisibility(View.GONE);
//        }
        return filepath
    }

    @Throws(Exception::class)
    private fun pdfCreation(ImagePaths: ArrayList<Image>, fileOutputStream: FileOutputStream) {
        var pageCount = 0

        //document creation
        val pdfDocument = PdfDocument()
        var pageInfo: PageInfo?

        //loop for every page
        for (i in ImagePaths.indices) {

            //loading bitmap
            val bitmap = BitmapFactory.decodeFile(ImagePaths[i].imagePath)

            //setting page info for current page
            var page: PdfDocument.Page
            pageInfo = PageInfo.Builder(bitmap.width, bitmap.height, pageCount).create()
            page = pdfDocument.startPage(pageInfo)

            //drawing image on canvas
            val canvas = page.canvas
            val paint = Paint()
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            //saving page
            pdfDocument.finishPage(page)
            pageCount++
        }

        //finally writing document and closing it
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
    }

    fun downloadPdf(ImagePaths: ArrayList<Image>, fileOutputStream: FileOutputStream) {
        try {
            pdfCreation(ImagePaths, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
    }
}