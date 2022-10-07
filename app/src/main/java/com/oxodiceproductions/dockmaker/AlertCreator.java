package com.oxodiceproductions.dockmaker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class AlertCreator {

//    Todo:Change the name of this class to dialogCreator

    public void createAlertForZeroSizeImages(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning!!!");
        builder.setMessage("Images with file size zero available.\nPlease remove those images");
        builder.setPositiveButton("ok", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showDialog(Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog detailsDialog=builder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                .create();
        detailsDialog.show();
    }
}
