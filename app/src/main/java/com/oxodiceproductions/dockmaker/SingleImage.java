package com.oxodiceproductions.dockmaker;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import java.io.File;
import java.util.ArrayList;

public class SingleImage extends AppCompatActivity {
    ImageView imageView;
    ProgressBar progressBar;
    String ImagePath = "-1";
    String DocId = "-1";
    boolean first_time = false;
    SharedPreferences sharedPreferences;
    TextView imageIndexTextView;
    ArrayList<String> imagesList = new ArrayList<>();
    private GestureDetectorCompat gestureDetectorCompat;
    ImageButton backButton, shareImageButton, deleteImageButton, editImageButton;
    ImageButton retakeImageButton, previousImageButton, nextImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);
        imageView = findViewById(R.id.imageView2);
        progressBar = findViewById(R.id.progressBar4);
        imageIndexTextView = findViewById(R.id.single_image_index_tv);
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE);
        ImagePath = getIntent().getExtras().getString("ImagePath", "-1");
        DocId = getIntent().getExtras().getString("DocId", "-1");
        first_time = getIntent().getExtras().getBoolean("first_time", false);
        imageView.setImageURI(Uri.fromFile(new File(ImagePath)));
        progressBar.setVisibility(View.GONE);

        backButton = findViewById(R.id.imageButton4);
        shareImageButton = findViewById(R.id.imageButton3);
        deleteImageButton = findViewById(R.id.imageButton2);
        editImageButton = findViewById(R.id.edit_button);
        retakeImageButton = findViewById(R.id.imageButton5);
        previousImageButton = findViewById(R.id.imageButton15);
        nextImageButton = findViewById(R.id.imageButton14);

        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureDetector());

        populateImageList();

        backButton.setOnClickListener(view -> onBackPressed());

        shareImageButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            try {
                File newFile = new File(ImagePath);
                Uri contentUri = getUriForFile(getApplicationContext(), "com.oxodiceproductions.dockmaker", newFile);
                grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "hello"));
            } catch (Exception ignored) {
            }
            progressBar.setVisibility(View.GONE);
        });

        deleteImageButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);

            ViewGroup viewGroup = view.findViewById(R.id.alert_main_layout);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SingleImage.this);
            final View[] customView = {getLayoutInflater().inflate(R.layout.alert_box, viewGroup, false)};
            alertDialogBuilder.setView(customView[0]);

            TextView textView = customView[0].findViewById(R.id.textView9);
            Button cancel_button = customView[0].findViewById(R.id.button);
            Button ok_button = customView[0].findViewById(R.id.button2);
            textView.setText(getResources().getText(R.string.t16));

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            ok_button.setOnClickListener(view12 -> {
                Runnable runnable = () -> {
                    //deleting from database
                    MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                    myDatabase.DeleteImage(ImagePath, DocId);
                    myDatabase.close();

                    //deleting from storage
                    CommonOperations.deleteFile(ImagePath);

                    //going to document view
                    Intent in = new Intent(SingleImage.this, document_view.class);
                    in.putExtra("DocId", DocId);
//                in.putExtra("first_time", false);
                    startActivity(in);
                    finish();
                };

                Thread thread = new Thread(runnable);
                thread.start();
            });
            cancel_button.setOnClickListener(view1 -> {
                progressBar.setVisibility(View.GONE);
                alertDialog.dismiss();
            });
        });

        editImageButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent in = new Intent(SingleImage.this, EditImageActivity.class);
            in.putExtra("ImagePath", ImagePath);
            in.putExtra("DocId", DocId);
            in.putExtra("retakeImagePath", ImagePath);
            startActivity(in);
            finish();
        });

        retakeImageButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent in = new Intent(SingleImage.this, MyCamera.class);
            in.putExtra("DocId", DocId);
            in.putExtra("ImagePath", ImagePath);
            startActivity(in);
            finish();
        });

        previousImageButton.setOnClickListener(view -> move(-1));

        nextImageButton.setOnClickListener(view -> move(1));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX < -2000) {
                move(1);
            } else if (velocityX > 2000) {
                move(-1);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    void populateImageList() {
        MyDatabase database = new MyDatabase(getApplicationContext());
        Cursor cc = database.LoadImagePaths(DocId);
        try {
            cc.moveToFirst();
            do {
                imagesList.add(cc.getString(0));
            } while (cc.moveToNext());
        } catch (Exception ignored) {

        } finally {
            database.close();
        }
        String index = "" + (imagesList.indexOf(ImagePath) + 1);
        imageIndexTextView.setText(index);
    }

    void move(int x) {
        int index = imagesList.indexOf(ImagePath);
        int next = index + x;
        if (next > imagesList.size() - 1) {
            next = 0;
        } else if (next < 0) {
            next = imagesList.size() - 1;
        }
        String stringIndex = "" + (next + 1);
        imageIndexTextView.setText(stringIndex);
        ImagePath = imagesList.get(next);
        imageView.setImageURI(Uri.fromFile(new File(ImagePath)));
    }

    @Override
    public void onBackPressed() {
        progressBar.setVisibility(View.VISIBLE);
        Intent in = new Intent(SingleImage.this, document_view.class);
        in.putExtra("DocId", DocId);
//        in.putExtra("first_time", false);
        startActivity(in);
    }
}
