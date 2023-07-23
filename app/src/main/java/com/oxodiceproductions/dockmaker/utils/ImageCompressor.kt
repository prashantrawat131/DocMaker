package com.oxodiceproductions.dockmaker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import android.util.Size
import com.oxodiceproductions.dockmaker.utils.CO.Companion.getUniqueName
import java.io.File
import java.io.FileOutputStream


class ImageCompressor(val context: Context) {

    //offset is a variable for unique name every time
    private var offset = 0

    fun compress(uri: Uri?): String {
        val appDir = File(context!!.filesDir.path)
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return try {
            val child = getUniqueName("jpg", ++offset)
            val tempFile = File(appDir, child)
            val outputStream = FileOutputStream(tempFile)
            val inputStream = context!!.contentResolver.openInputStream(uri!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(inputStream!!, outputStream)
            } else {
                val bb = ByteArray(inputStream!!.available())
                inputStream.read(bb)
                outputStream.write(bb)
            }
            compress(tempFile)
        } catch (e: Exception) {
            "-1"
        }
    }

    //do not use bitmap.compress because it increases the size and loss of quality also happens
    fun compress(actualImageFile: File): String {
//        Log.d("tagJi","Image compression started");
        val destinationFile = getFile()
        return try {
            //decoding only bitmap bounds and setting values for size variable
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(actualImageFile.path, options)
            val width = options.outWidth
            val height = options.outHeight
            val size = getQualityFactor(width, height)

            //setting inSampleSize and loading a scaled bitmap
            options.inSampleSize = calculateInSampleSize(options, size.width, size.height)
            options.inJustDecodeBounds = false
            val scaledBitmap = BitmapFactory.decodeFile(actualImageFile.path, options)

            //writing scaled bitmap into a file
            val fileOutputStream = FileOutputStream(destinationFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            destinationFile.path
        } catch (e: Exception) {
            "-1"
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        //this function was provided by google so no interfere
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = Math.min(heightRatio, widthRatio)
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    private fun getQualityFactor(width: Int, height: Int): Size {
        //getting max width and maintaining the same aspect ratio
        val maxWidth = getMaxWidth()
        var fWidth = width.toFloat()
        var fHeight = height.toFloat()
        val ratio = fWidth / fHeight
        var size = Size(width, height)
        if (width > maxWidth && height > maxWidth) {
            fWidth = maxWidth.toFloat()
            fHeight = fWidth / ratio
            size = Size(fWidth.toInt(), fHeight.toInt())
        }
        return size
    }

    private fun getMaxWidth(): Int {
        //this function returns the max width which is according to the quality
        val sharedPreferences = context!!.getSharedPreferences("DocMakerSettings", Context.MODE_PRIVATE)
        val maxWidth: Int
        maxWidth = when (sharedPreferences.getInt("pdf_image_quality", -1)) {
            1 -> 500
            3 -> 1500
            else -> 1000
        }
        return maxWidth
    }

    fun getFile(): File {
        //this file returns a image file for writing and it also increments the offset for different images
        val child = getUniqueName("jpg", ++offset)
        val appDir = File(context!!.filesDir.path)
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return File(appDir, child)
    }

}