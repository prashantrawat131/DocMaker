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
import com.oxodiceproductions.dockmaker.databinding.ActivityEditingImageBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class EditingImageActivity extends AppCompatActivity {

    String ImagePath = "-1";
    File finalFile;
    Bitmap bitmap;
    ActivityEditingImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEditingImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        IdProvider();

        InitialWork();

        binding.progressBarEditImage.setVisibility(View.GONE);

        binding.backButtonEditImage.setOnClickListener(view->{
            onBackPressed();
        });

        binding.rotateButtonEditImage.setOnClickListener(view->{
            binding.cropImageView.rotateImage(90);
        });

        binding.cropButtonEditImage.setOnClickListener(view -> {
            binding.progressBarEditImage.setVisibility(View.VISIBLE);
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
                binding.cropImageView.getCroppedImage().compress(Bitmap.CompressFormat.JPEG, 100, bos);
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
            binding.progressBarEditImage.setVisibility(View.GONE);
        });
    }


    private void InitialWork(){
        ImagePath = getIntent().getExtras().getString("ImagePath", "-1");

        bitmap = BitmapFactory.decodeFile(ImagePath);

        binding.cropImageView.setImageBitmap(bitmap);
    }

   /* private void IdProvider(){
        flipButton = findViewById(R.id.flip_button_edit_image);
        backButton = findViewById(R.id.back_button_edit_image);
        cropImageView = findViewById(R.id.crop_imageView);
        progressBar = findViewById(R.id.progress_bar_edit_image);
        bnwButton = findViewById(R.id.bnwButton);
        rotateImageButton=findViewById(R.id.rotate_button_edit_image);
        cropButton=findViewById(R.id.crop_button_edit_image);
    }*/

    private void Exit() {
        binding.progressBarEditImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Exit();
    }
}