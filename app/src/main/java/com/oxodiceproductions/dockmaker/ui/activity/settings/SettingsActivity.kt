package com.oxodiceproductions.dockmaker.ui.activity.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.databinding.ActivityMySettingsBinding
import com.oxodiceproductions.dockmaker.ui.activity.all_docs.AllDocsActivity

class SettingsActivity : AppCompatActivity() {
    var pdf_image_quality = 0
    var sharedPreferences: SharedPreferences? = null
    lateinit var binding: ActivityMySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySettingsBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        sharedPreferences = getSharedPreferences("DocMakerSettings", MODE_PRIVATE)

        binding.saveSetttingsButton.setVisibility(View.GONE)
        Initializer()
        binding.imageQualityRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i === R.id.low_image_quality_button) {
                pdf_image_quality = 1 //low
            }
            if (i === R.id.medium_image_quality_button) {
                pdf_image_quality = 2 //medium
            }
            if (i === R.id.high_image_quality_button) {
                pdf_image_quality = 3 //high
            }
            binding.saveSetttingsButton.setVisibility(View.VISIBLE)
        }
        binding.saveSetttingsButton.setOnClickListener { view ->
            val editor = sharedPreferences!!.edit()
            editor.putInt("pdf_image_quality", pdf_image_quality)
            editor.apply()
            GoToAllDocs()
        }
    }

    private fun Initializer() {
        pdf_image_quality = sharedPreferences!!.getInt("pdf_image_quality", 2)
        when (pdf_image_quality) {
            1 -> binding.lowImageQualityButton.setChecked(true)
            2 -> binding.mediumImageQualityButton.setChecked(true)
            3 -> binding.highImageQualityButton.setChecked(true)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GoToAllDocs()
    }

    fun GoToAllDocs() {
        val `in` = Intent(this, AllDocsActivity::class.java)
        startActivity(`in`)
        finish()
    }
}