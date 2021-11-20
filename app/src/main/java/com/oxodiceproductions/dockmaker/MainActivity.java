package com.oxodiceproductions.dockmaker;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    int camera_permission_request_code, write_permission_request_code;
    boolean all_permissions_granted = false;
    boolean dialog_running = false;
    boolean first_time = true;

    //TextView appNameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mycode
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE);
        CheckPermissions();
    }


    public void displayNeverAskAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.t15));
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            dialog_running = false;
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialog_running = false);
        builder.show();
    }

    void CheckPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, camera_permission_request_code);
        } else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, write_permission_request_code);
        } else {
            Next();
            all_permissions_granted = true;
        }
        if ((!dialog_running) && (!first_time) && (!all_permissions_granted)) {
            cross_check();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == camera_permission_request_code) {
                CheckPermissions();
            }
            if (requestCode == write_permission_request_code) {
                CheckPermissions();
            }
        }
        if (!all_permissions_granted) {
            CheckPermissions();
        }
        first_time = false;
    }

    private void cross_check() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                dialog_running = true;
                displayNeverAskAgainDialog();
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                dialog_running = true;
                displayNeverAskAgainDialog();
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                dialog_running = true;
                displayNeverAskAgainDialog();
            } else {
                all_permissions_granted = true;
                Next();
            }
        } else {
        }
    }

    private void Next() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, AllDocs.class);
            startActivity(intent);
            finish();
        }, 500);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
