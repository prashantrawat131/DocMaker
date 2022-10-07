package com.oxodiceproductions.dockmaker;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.oxodiceproductions.dockmaker.Database.AppDatabase;
import com.oxodiceproductions.dockmaker.Database.Document;
import com.oxodiceproductions.dockmaker.Database.DocumentDao;
import com.oxodiceproductions.dockmaker.Database.Image;
import com.oxodiceproductions.dockmaker.Database.ImageDao;
import com.oxodiceproductions.dockmaker.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    int camera_permission_request_code, write_permission_request_code;
    boolean all_permissions_granted = false;
    boolean dialog_running = false;
    boolean first_time = true;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        createNotificationChannel();

        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_screen_text_anim);
        binding.appNameTvSplashScreen.setAnimation(animation);
        animation.start();

        CheckPermissions();

        importDataToNewDatabase();

        testing();
    }

    private void testing() {
      /*  new Thread(() -> {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            ImageDao imageDao = appDatabase.imageDao();
            for (Image image : imageDao.getAll()) {
                CommonOperations.log(image.getImageIndex() + " " + image.getId() + " " + image.getImagePath());
            }
        }).start();*/
    }

    private void importDataToNewDatabase() {
        new Thread(() -> {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            MyDatabase myDatabase = new MyDatabase(getApplicationContext());
            DocumentDao documentDao = appDatabase.documentDao();
            ImageDao imageDao = appDatabase.imageDao();
            Cursor docCursor = myDatabase.LoadDocuments();
//            appDatabase.documentDao().deleteAll();
//            appDatabase.imageDao().deleteAll();
            if (documentDao.getAll() == null || documentDao.getAll().size() == 0) {
                try {
                    docCursor.moveToFirst();
                    do {
                        try {
                            String DocId = docCursor.getString(0);
                            String DateCreated = docCursor.getString(2);
                            String TimeCreated = docCursor.getString(3);
                            String DocName = docCursor.getString(4);

                            String dateTimeString = DateCreated + " " + TimeCreated;
//                        CommonOperations.log(dateTimeString);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//                        CommonOperations.log(sdf.parse(dateTimeString).getTime()+"");
                            long time = sdf.parse(dateTimeString).getTime();
                            Document document = new Document(time, DocName);
                            long docIdLong = documentDao.insert(document);
                            CommonOperations.log("Document saved: " + document.getId() + " " + document.getName() + " time: " + document.getTime());

                            Cursor imageCursor = myDatabase.LoadImagePaths(DocId);
                            imageCursor.moveToFirst();
                            int i = 0;
                            do {
                                String imagePath = imageCursor.getString(0);
                                Image image = new Image(imagePath, i, docIdLong);
                                imageDao.insert(image);
                                CommonOperations.log("Image inserted: " + i + " " + imagePath + " " + image.getDocId());
                                i++;
                            } while (imageCursor.moveToNext());

                        } catch (Exception e) {
                            CommonOperations.logError("Inner code error: " + e.getMessage());
                        }
                    } while (docCursor.moveToNext());
                } catch (Exception e) {
                    CommonOperations.logError(e.getMessage());
                }
            }
        }).start();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.DocMakerNotificationChannelName);
            String description = getString(R.string.DocMakerNotificationDescription);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.DocMakerNotificationChannelId), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
        }, 1500);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
