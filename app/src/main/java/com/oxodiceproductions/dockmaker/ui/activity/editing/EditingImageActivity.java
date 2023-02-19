package com.oxodiceproductions.dockmaker.ui.activity.editing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.oxodiceproductions.dockmaker.utils.CO;
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
        binding = ActivityEditingImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InitialWork();

        binding.progressBarEditImage.setVisibility(View.GONE);

        binding.backButtonEditImage.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.rotateButtonEditImage.setOnClickListener(view -> {
            binding.cropImageView.rotateImage(90);
        });

        binding.cropButtonEditImage.setOnClickListener(view -> {
            binding.progressBarEditImage.setVisibility(View.VISIBLE);
            //old file getting deleted
            CO.deleteFile(ImagePath);

            //creating new file name and saving it
            String uniqueName = CO.getUniqueName("jpg", 0);//date + time + ".jpg";
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
                Intent replyIntent = new Intent();
                replyIntent.putExtra("ImagePath", ImagePath);
                setResult(RESULT_OK, replyIntent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            binding.progressBarEditImage.setVisibility(View.GONE);
        });
    }


    private void InitialWork() {
        ImagePath = getIntent().getExtras().getString("ImagePath", "-1");

        bitmap = BitmapFactory.decodeFile(ImagePath);

        binding.cropImageView.setImageBitmap(bitmap);
    }



    private void Exit() {
        binding.progressBarEditImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Exit();
    }
}