package com.oxodiceproductions.dockmaker.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class ShareUtil {
    companion object {
        fun shareImage(context: Context, imagePath: String) {
            try {
                val newFile = File(imagePath)
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "com.oxodiceproductions.dockmaker",
                    newFile
                )
                context.grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                context.startActivity(Intent.createChooser(shareIntent, "Select an app to share"))
            } catch (ignored: Exception) {
            }
        }
    }
}