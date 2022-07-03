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

public class EditingImageActivity extends AppCompatActivity {

    String ImagePath = "-1";
    CropImageView cropImageView;
    Button bnwButton,cropButton;
    ImageButton backButton, flipButton,rotateImageButton;
    File finalFile;
    Bitmap bitmap;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_image);

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

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(finalFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                cropImageView.getCroppedImage().compress(Bitmap.CompressFormat.JPEG, 100, bos);
                fileOutputStream.write(bos.toByteArray());
                fileOutputStream.close();

                //sending the path of the cropped image
                Intent replyIntent=new Intent();
                replyIntent.putExtra("ImagePath",ImagePath);
                setResult(RESULT_OK,replyIntent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
        });
    }


    private void InitialWork(){
        ImagePath = getIntent().getExtras().getString("ImagePath", "-1");

        bitmap = BitmapFactory.decodeFile(ImagePath);

        cropImageView.setImageBitmap(bitmap);
    }

    private void IdProvider(){
        flipButton = findViewById(R.id.imageButton13);
        backButton = findViewById(R.id.edit_back_button);
        cropImageView = findViewById(R.id.crop_imageView);
        progressBar = findViewById(R.id.progressBar5);
        bnwButton = findViewById(R.id.bnwButton);
        rotateImageButton=findViewById(R.id.imageButton12);
        cropButton=findViewById(R.id.button7);
    }

    private void Exit() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Exit();
    }
}