package com.oxodiceproductions.dockmaker.ui.activity.single_image;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.oxodiceproductions.dockmaker.databinding.ActivitySingleImageBinding;
import com.oxodiceproductions.dockmaker.ui.activity.camera.CameraActivity;
import com.oxodiceproductions.dockmaker.ui.activity.document_view.DocumentViewActivity;
import com.oxodiceproductions.dockmaker.ui.activity.editing.EditingImageActivity;
import com.oxodiceproductions.dockmaker.utils.CO;
import com.oxodiceproductions.dockmaker.utils.Constants;
import com.oxodiceproductions.dockmaker.Database.AppDatabase;
import com.oxodiceproductions.dockmaker.Database.Image;
import com.oxodiceproductions.dockmaker.Database.ImageDao;
import com.oxodiceproductions.dockmaker.R;

import java.io.File;
import java.util.ArrayList;

public class SingleImage extends AppCompatActivity {
    String ImagePath = "-1";
    long DocId = -1;
    SharedPreferences sharedPreferences;
    ArrayList<String> imagesList = new ArrayList<>();
    private GestureDetectorCompat gestureDetectorCompat;
    ActivitySingleImageBinding binding;
    public static final int editingSingleImageId = 1908;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingleImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE);
        ImagePath = getIntent().getExtras().getString("ImagePath", "-1");
        DocId = getIntent().getExtras().getLong("DocId", -1);
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
                new Thread(() -> {
                    AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                    ImageDao imageDao = appDatabase.imageDao();
                    imageDao.deleteImageByPath(ImagePath);

                    CO.deleteFile(ImagePath);

                    Intent in = new Intent(SingleImage.this, DocumentViewActivity.class);
                    in.putExtra(Constants.SP_DOC_ID, DocId);
                    startActivity(in);
                    finish();
                }).start();

                /*Runnable runnable = () -> {
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
                thread.start();*/
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
            editImageActivityLauncher.launch(in);
            /*Intent in = new Intent(SingleImage.this, EditingImageActivity.class);
            in.putExtra("ImagePath", ImagePath);
//            in.putExtra("DocId", DocId);
//            in.putExtra("retakeImagePath", ImagePath);
            startActivityForResult(in, editingSingleImageId);*/
        });

        binding.retakeSingleImage.setOnClickListener(view -> {
            binding.progressBarSingleImage.setVisibility(View.VISIBLE);
            Intent in = new Intent(SingleImage.this, CameraActivity.class);
            in.putExtra(Constants.SP_DOC_ID, DocId);
            in.putExtra("ImagePath", ImagePath);
            startActivity(in);
            finish();
        });

        binding.previousSingleImage.setOnClickListener(view -> move(-1));

        binding.nextSingleImage.setOnClickListener(view -> move(1));

        binding.downloadSingleImage.setOnClickListener(view -> {
            CO.downloadImage(getApplicationContext(), ImagePath);
        });
    }


    ActivityResultLauncher<Intent> editImageActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
//                    CommonOperations.log("Arriving after result");
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            CO.log("Data is null");
                            return;
                        }
//                        assert data != null;
                        String receivedImagePath = data.getExtras().getString("ImagePath");

                        CO.log("Received image path: " + receivedImagePath);

                        new Thread(() -> {
                            try {
                                AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                                ImageDao imageDao = appDatabase.imageDao();

                                Image image = imageDao.getImageByImagePath(DocId, ImagePath);
                                image.setImagePath(receivedImagePath);

                                imageDao.update(image);

                                //setting the new image
                                imagesList.set(imagesList.indexOf(ImagePath), receivedImagePath);
                                ImagePath = receivedImagePath;
                                runOnUiThread(() -> {
                                    binding.imageSingleImage.setImageURI(Uri.fromFile(new File(ImagePath)));
                                    binding.progressBarSingleImage.setVisibility(View.GONE);
                                });
                            } catch (Exception e) {
                                CO.log("Error while updating the image path for edited image: " + e.getMessage());
                            }
                        }).start();

                    }
                }
            }
    );


/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == editingSingleImageId) {
            if (resultCode == RESULT_OK) {

                new Thread(() -> {
                    AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                    ImageDao imageDao = appDatabase.imageDao();

                    String newImagePath = data.getExtras().getString("ImagePath");
                    Image image = imageDao.getImageByImagePath(DocId, ImagePath);
                    image.setImagePath(newImagePath);

                    imageDao.update(image);

                    runOnUiThread(() -> {
                        //setting the new image
                        ImagePath = newImagePath;
                        binding.imageSingleImage.setImageURI(Uri.fromFile(new File(ImagePath)));
                        binding.progressBarSingleImage.setVisibility(View.GONE);
                    });
                }).start();

                *//*
                String newImagePath = data.getExtras().getString("ImagePath");
                MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                myDatabase.retake(DocId, ImagePath, newImagePath);
                myDatabase.close();

                //setting the new image
                ImagePath = newImagePath;
                binding.imageSingleImage.setImageURI(Uri.fromFile(new File(ImagePath)));
                binding.progressBarSingleImage.setVisibility(View.GONE);*//*
            }
        }
    }*/

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

        new Thread(() -> {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            ImageDao imageDao = appDatabase.imageDao();
            ArrayList<Image> imageArrayList = (ArrayList<Image>) imageDao.getImagesByDocId(DocId);
            for (Image image : imageArrayList) {
                imagesList.add(image.getImagePath());
            }

            runOnUiThread(() -> {
                String index = "" + (imagesList.indexOf(ImagePath) + 1);
                binding.singleImageIndexTv.setText(index);
            });
        }).start();
       /* MyDatabase database = new MyDatabase(getApplicationContext());
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
        binding.singleImageIndexTv.setText(index);*/
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
        in.putExtra(Constants.SP_DOC_ID, DocId);
        startActivity(in);
    }
}