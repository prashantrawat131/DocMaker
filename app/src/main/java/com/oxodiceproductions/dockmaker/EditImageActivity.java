package com.oxodiceproductions.dockmaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class EditImageActivity extends AppCompatActivity {
    String ImagePath = "-1";
    CropImageView cropImageView;
    Button bnwButton,cropButton;
    ImageButton backButton, flipButton,rotateImageButton;
    String retakeImagePath = "-1";
    String DocId = "-1";
    File finalFile;
    Bitmap bitmap;
    ProgressBar progressBar;
//    boolean isBnw = false;
    boolean fromGallery = false, fromCamera = false;
    ArrayList<String> selectedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);


        IdProvider();


        InitialWork();

        progressBar.setVisibility(View.GONE);

        backButton.setOnClickListener(view->{
            onBackPressed();
        });

        rotateImageButton.setOnClickListener(view->{
            cropImageView.rotateImage(90);
        });

        cropButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            //old file getting deleted
            CommonOperations.deleteFile(ImagePath);

            //creating new file name and saving it
            String uniqueName = CommonOperations.getUniqueName("jpg", 0);//date + time + ".jpg";
            File appDir = new File(getFilesDir().getPath());
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            finalFile = new File(appDir, uniqueName);
            ImagePath = finalFile.getPath();

            //saving to database
            MyDatabase myDatabase = new MyDatabase(getApplicationContext());
            if (retakeImagePath.equals("-1")) {
                myDatabase.InsertImage(DocId, finalFile.getPath());
            } else {
                myDatabase.retake(DocId, retakeImagePath, finalFile.getPath());
            }
            myDatabase.close();

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(finalFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                cropImageView.getCroppedImage().compress(Bitmap.CompressFormat.JPEG, 100, bos);
                fileOutputStream.write(bos.toByteArray());
                fileOutputStream.close();

                if (fromGallery) {
                    //this section is for user which came from gallery
                    selectedImages.remove(0);
                    //check whether the list is empty or nor
                    if (selectedImages.isEmpty()) {
//                        Log.d("tagJi","Exit");
                        Exit();
                    } else {
                        /*this block is not as simple as it appears
                        this block is executed when the user clicks
                        save and there are still some images in the text view
                        so it had cropped and save the present image and removed it from
                        selectedImage and now we are passing the image in the
                        list to the crop function which is actually not getting cropped but
                        it being placed in the crop image view.
                        */
                        crop(selectedImages.get(0));
                    }
//                    isBnw = false;
                } else {
                    //not from gallery
                    new File(retakeImagePath).delete();
                    Exit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
        });

    }

    private void InitialWork(){
        ImagePath = getIntent().getExtras().getString("ImagePath", "-1");
        DocId = getIntent().getExtras().getString("DocId", "-1");
        retakeImagePath = getIntent().getExtras().getString("retakeImagePath", "-1");
        fromGallery = getIntent().getExtras().getBoolean("fromGallery", false);
        fromCamera = getIntent().getExtras().getBoolean("fromCamera", false);
        selectedImages = getIntent().getExtras().getStringArrayList("galleryImagesPaths");

        bitmap = BitmapFactory.decodeFile(ImagePath);

        cropImageView.setImageBitmap(bitmap);
    }

    private void IdProvider(){
        flipButton = findViewById(R.id.imageButton13);
        backButton = findViewById(R.id.edit_back_button);
        cropImageView = (CropImageView) findViewById(R.id.crop_imageView);
        progressBar = findViewById(R.id.progressBar5);
        bnwButton = findViewById(R.id.bnwButton);
        rotateImageButton=findViewById(R.id.imageButton12);
        cropButton=findViewById(R.id.button7);
    }


/*
    private void bnw() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            int width, height, i = 0, bnwMargin = 122;
            Bitmap.Config config;
//            Rect rect = cropImageView.getCropRect();
            RectF rect = cropImageView.getActualCropRect();
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            config = bitmap.getConfig();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            bitmap.recycle();
            bitmap = Bitmap.createBitmap(width, height, config);
            for (int pixel : pixels) {
                if (!(Color.red(pixel) < bnwMargin && Color.green(pixel) < bnwMargin))
                    pixels[i] = Color.argb(225, 225, 225, 225);
                i++;
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            cropImageView.setImageBitmap(bitmap);
//            cropImageView.setCropRect(rect);
            isBnw = true;
            progressBar.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
    }*/

    private void Exit() {
        progressBar.setVisibility(View.VISIBLE);
        Intent in;
        if (fromGallery || fromCamera)
            in = new Intent(EditImageActivity.this, document_view.class);
        else {
            in = new Intent(EditImageActivity.this, SingleImage.class);
            in.putExtra("ImagePath", ImagePath);
        }
        in.putExtra("DocId", DocId);
        startActivity(in);
        finish();
    }

    void crop(String pathToFile) {
        ImagePath = pathToFile;
        //crop is clicked and now it is updating the bitmap and placing it on crop imageView
        bitmap.recycle();
        bitmap = BitmapFactory.decodeFile(ImagePath);
        if (bitmap == null) {
            selectedImages.remove(ImagePath);
            if (selectedImages.isEmpty()) {
                Exit();
            } else {
                crop(selectedImages.get(0));
            }
        } else {
            cropImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*It first of all check whether the selectedImages arrayList is null or not
         * because there can be a case in which it can be null.
         * Then it deletes if any image is left in the list because the user has clicked back*/
        try {
            if (selectedImages != null) {
                for (String selectedImagePath : selectedImages) {
                    CommonOperations.deleteFile(selectedImagePath);
                }
            }
        } catch (Exception ignored) {
        }

        Exit();
    }
}