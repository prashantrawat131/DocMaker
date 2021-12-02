package com.oxodiceproductions.dockmaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class MySettings extends AppCompatActivity {

    int pdf_image_quality;
    SharedPreferences sharedPreferences;
    Button save_button;
    RadioGroup radioGroup;
    RadioButton low_rd, medium_rd, high_rd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_settings);

        sharedPreferences = getSharedPreferences("DocMakerSettings", MODE_PRIVATE);

        save_button = findViewById(R.id.button5);
        radioGroup = findViewById(R.id.radioGroup);
        low_rd = findViewById(R.id.radioButton);
        medium_rd = findViewById(R.id.radioButton2);
        high_rd = findViewById(R.id.radioButton3);
        save_button.setVisibility(View.GONE);

        Initializer();

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.radioButton) {
                pdf_image_quality = 1;//low
            }
            if (i == R.id.radioButton2) {
                pdf_image_quality = 2;//medium
            }
            if (i == R.id.radioButton3) {
                pdf_image_quality = 3;//high
            }
            save_button.setVisibility(View.VISIBLE);
        });
    }

    private void Initializer() {
        pdf_image_quality = sharedPreferences.getInt("pdf_image_quality", 2);
        switch (pdf_image_quality) {
            case 1:
                low_rd.setChecked(true);
                break;
            case 2:
                medium_rd.setChecked(true);
                break;
            case 3:
                high_rd.setChecked(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GoToAllDocs();
    }

    void GoToAllDocs() {
        Intent in = new Intent(MySettings.this, AllDocs.class);
        startActivity(in);
        finish();
    }

    public void saveSettings(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("pdf_image_quality", pdf_image_quality);
        editor.apply();
        GoToAllDocs();
    }
}