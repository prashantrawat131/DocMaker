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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.oxodiceproductions.dockmaker.databinding.ActivitySingleImageBinding;

import java.io.File;
import java.util.ArrayList;

public class SingleImage extends AppCompatActivity {
    String ImagePath = "-1";
    String DocId = "-1";
    SharedPreferences sharedPreferences;
    ArrayList<String> imagesList = new ArrayList<>();
    private GestureDetectorCompat gestureDetectorCompat;
    ActivitySingleImageBinding binding;
    public static final int editingSingleImageId = 1908;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySingleImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE);
        ImagePath = getIntent().getExtras().getString("ImagePath", "-1");
        DocId = getIntent().getExtras().getString("DocId", "-1");
        binding.imageSingleImage.setImageURI(Uri.fromFile(new File(ImagePath)));
        binding.progressBarSingleImage.setVisibility(View.GONE);

        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureDetector());

        populateImageList();

        binding.backButtonSingleImage.setOnClickListener(view -> onBackPressed());

        binding.shareSingleImage.setOnClickListener(view -> {
            binding.progressBarSingleImage.setVisibility(View.VISIBLE);
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
            binding.progressBarSingleImage.setVisibility(View.GONE);
        });

        binding.deleteSingleImage.setOnClickListener(view -> {
            binding.progressBarSingleImage.setVisibility(View.VISIBLE);

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
                    Intent in = new Intent(SingleImage.this, DocumentViewActivity.class);
                    in.putExtra("DocId", DocId);
//                in.putExtra("first_time", false);
                    startActivity(in);
                    finish();
                };

                Thread thread = new Thread(runnable);
                thread.start();
            });
            cancel_button.setOnClickListener(view1 -> {
                binding.progressBarSingleImage.setVisibility(View.GONE);
                alertDialog.dismiss();
            });
        });

        binding.editSingleImage.setOnClickListener(view -> {
            binding.progressBarSingleImage.setVisibility(View.VISIBLE);
            Intent in = new Intent(SingleImage.this, EditingImageActivity.class);
            in.putExtra("ImagePath", ImagePath);
//            in.putExtra("DocId", DocId);
//            in.putExtra("retakeImagePath", ImagePath);
            startActivityForResult(in, editingSingleImageId);
        });

        binding.retakeSingleImage.setOnClickListener(view -> {
            binding.progressBarSingleImage.setVisibility(View.VISIBLE);
            Intent in = new Intent(SingleImage.this, MyCamera.class);
            in.putExtra("DocId", DocId);
            in.putExtra("ImagePath", ImagePath);
            startActivity(in);
            finish();
        });

        binding.previousSingleImage.setOnClickListener(view -> move(-1));

        binding.nextSingleImage.setOnClickListener(view -> move(1));

        binding.downloadSingleImage.setOnClickListener(view -> {
            CommonOperations.downloadImage(getApplicationContext(), ImagePath);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == editingSingleImageId) {
            if (resultCode == RESULT_OK) {
                String newImagePath = data.getExtras().getString("ImagePath");
                MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                myDatabase.retake(DocId, ImagePath, newImagePath);
                myDatabase.close();

                //setting the new image
                ImagePath = newImagePath;
                binding.imageSingleImage.setImageURI(Uri.fromFile(new File(ImagePath)));
                binding.progressBarSingleImage.setVisibility(View.GONE);
            }
        }
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
        binding.singleImageIndexTv.setText(index);
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
        binding.singleImageIndexTv.setText(stringIndex);
        ImagePath = imagesList.get(next);
        binding.imageSingleImage.setImageURI(Uri.fromFile(new File(ImagePath)));
    }

    @Override
    public void onBackPressed() {
        binding.progressBarSingleImage.setVisibility(View.VISIBLE);
        Intent in = new Intent(SingleImage.this, DocumentViewActivity.class);
        in.putExtra("DocId", DocId);
        startActivity(in);
    }
}
