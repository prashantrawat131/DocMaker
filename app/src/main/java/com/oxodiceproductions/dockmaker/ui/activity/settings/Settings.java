package com.oxodiceproductions.dockmaker.ui.activity.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.oxodiceproductions.dockmaker.R;
import com.oxodiceproductions.dockmaker.databinding.ActivityMySettingsBinding;
import com.oxodiceproductions.dockmaker.ui.activity.all_docs.AllDocs;

public class Settings extends AppCompatActivity {

    int pdf_image_quality;
    SharedPreferences sharedPreferences;
    ActivityMySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("DocMakerSettings", MODE_PRIVATE);

//        save_button = findViewById(R.id.button5);
//        radioGroup = findViewById(R.id.radioGroup);
//        low_rd = findViewById(R.id.radioButton);
//        medium_rd = findViewById(R.id.radioButton2);
//        high_rd = findViewById(R.id.radioButton3);
        binding.saveSetttingsButton.setVisibility(View.GONE);

        Initializer();

        binding.imageQualityRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.low_image_quality_button) {
                pdf_image_quality = 1;//low
            }
            if (i == R.id.medium_image_quality_button) {
                pdf_image_quality = 2;//medium
            }
            if (i == R.id.high_image_quality_button) {
                pdf_image_quality = 3;//high
            }
            binding.saveSetttingsButton.setVisibility(View.VISIBLE);
        });

        binding.saveSetttingsButton.setOnClickListener(view->{
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("pdf_image_quality", pdf_image_quality);
            editor.apply();
            GoToAllDocs();
        });
    }

    private void Initializer() {
        pdf_image_quality = sharedPreferences.getInt("pdf_image_quality", 2);
        switch (pdf_image_quality) {
            case 1:
                binding.lowImageQualityButton.setChecked(true);
                break;
            case 2:
                binding.mediumImageQualityButton.setChecked(true);
                break;
            case 3:
                binding.highImageQualityButton.setChecked(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GoToAllDocs();
    }

    void GoToAllDocs() {
        Intent in = new Intent(Settings.this, AllDocs.class);
        startActivity(in);
        finish();
    }
}