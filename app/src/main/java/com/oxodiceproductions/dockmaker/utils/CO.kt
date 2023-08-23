package com.oxodiceproductions.dockmaker.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import android.widget.Toast
import com.oxodiceproductions.dockmaker.model.DocumentPreviewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CO {
    companion object {
        fun deleteFile(filePath: String?) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                CO.logError("Error on CommonOperations.deleteFile: " + e.message)
            }
        }

        fun getUniqueName(extension: String, offset: Int): String {
            val c = Calendar.getInstance()
            val time = "" + c[Calendar.HOUR_OF_DAY] + "_" + c[Calendar.MINUTE] + c[Calendar.SECOND]
            val date =
                "" + c[Calendar.DATE] + c[Calendar.MONTH] + "_" + c[Calendar.YEAR] + "_" + offset
            val name = "DocMaker$date$time"
            return "$name.$extension"
        }

        fun createFile(context: Context, extension: String): File? {
            val folder = File(context.filesDir.path)
            if (!folder.exists()) {
                folder.mkdir()
            }
            val child = getUniqueName(extension, 0)
            return File(folder, child)
        }

        fun createTempFile(context: Context, extension: String): File? {
            val folder = File(context.cacheDir.path)
            if (!folder.exists()) {
                folder.mkdir()
            }
            val child = getUniqueName(extension, 0)
            return File(folder, child)
        }

        fun downloadImage(context: Context, ImagePath: String?) {
            val imageName = getUniqueName("jpg", 3)
            val notificationModule = NotificationModule()
            notificationModule.generateNotification(context, imageName, "Go to downloads.")
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val imageFile = File(downloadsFolder, imageName)
            try {
                val fileOutputStream = FileOutputStream(imageFile)
                val fileInputStream = FileInputStream(File(ImagePath))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(fileInputStream, fileOutputStream)
                } else {
                    val bb = ByteArray(fileInputStream.available())
                    fileInputStream.read(bb)
                    fileOutputStream.write(bb)
                }
            } catch (ignored: Exception) {
            }
        }
/*
         fun deleteDocument(context: Context?, docId: Long) {
            Thread {
                val appDatabase = AppDatabase.getInstance(context)
                val documentDao = appDatabase.documentDao()
                //            Cursor cc = myDatabase2.LoadImagePaths(docId);
                val imageDao = appDatabase.imageDao()
                val images =
                    imageDao.getImagesByDocId(docId) as ArrayList<Image>
                try {
                    for (image in images) {
                        CO.deleteFile(image.imagePath)
                        imageDao.delete(image)
                    }
//                    documentDao.deleteDocById(docId)
                } catch (ignored: Exception) {
                }
            }.start()
        }*/

        fun log(msg: String) {
            Log.d(Constants.TAG, msg + "")
        }

        fun logError(msg: String) {
            Log.e(Constants.TAG, msg + "")
        }

        fun toast(msg: String, context: Context?) {
            Toast.makeText(context, "" + msg, Toast.LENGTH_SHORT).show()
        }

        fun getDocTime(time: Long): DocumentPreviewModel.DateTime {
            val dateSDF = SimpleDateFormat("dd MMM", Locale.getDefault())
            val timeSDF = SimpleDateFormat("HH:mm a", Locale.getDefault())
            return DocumentPreviewModel.DateTime(
                dateSDF.format(Date(time)),
                timeSDF.format(Date(time))
            )
        }
    }
}