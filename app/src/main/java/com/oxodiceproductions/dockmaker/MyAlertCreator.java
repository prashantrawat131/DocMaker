package com.oxodiceproductions.dockmaker;

import android.app.AlertDialog;
import android.content.Context;

public class MyAlertCreator {

    public void createAlertForZeroSizeImages(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning!!!");
        builder.setMessage("Images with file size zero available.\nPlease remove those images");
        builder.setPositiveButton("ok", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
