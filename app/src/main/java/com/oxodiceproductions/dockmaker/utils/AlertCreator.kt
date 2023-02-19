package com.oxodiceproductions.dockmaker.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class AlertCreator {

    //    Todo:Change the name of this class to dialogCreator
    fun createAlertForZeroSizeImages(context: Context?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Warning!!!")
        builder.setMessage("Images with file size zero available.\nPlease remove those images")
        builder.setPositiveButton("ok", null)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    fun showDialog(context: Context?, text: String?) {
        val builder = AlertDialog.Builder(context)
        val detailsDialog = builder.setMessage(text)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            .create()
        detailsDialog.show()
    }
}