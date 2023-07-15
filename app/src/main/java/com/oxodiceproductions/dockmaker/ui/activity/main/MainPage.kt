package com.oxodiceproductions.dockmaker.ui.activity.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.databinding.ActivityMainBinding
import com.oxodiceproductions.dockmaker.ui.activity.all_docs.AllDocsActivity

class MainPage : AppCompatActivity(){

    var sharedPreferences: SharedPreferences? = null
    var camera_permission_request_code = 0
    var write_permission_request_code = 0
    var all_permissions_granted = false
    var dialog_running = false
    var first_time = true
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE)
        val animation = AnimationUtils.loadAnimation(
            applicationContext, R.anim.splash_screen_text_anim
        )
        binding.appNameTvSplashScreen.animation = animation
        animation.start()
        CheckPermissions()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.DocMakerNotificationChannelName)
            val description = getString(R.string.DocMakerNotificationDescription)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                getString(R.string.DocMakerNotificationChannelId),
                name,
                importance
            )
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun displayNeverAskAgainDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.t15))
        builder.setCancelable(false)
        builder.setPositiveButton("Permit Manually") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            dialog_running = false
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface: DialogInterface?, i: Int ->
            dialog_running = false
        }
        builder.show()
    }

    fun CheckPermissions() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                camera_permission_request_code
            )
        } else if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                write_permission_request_code
            )
        } else {
            Next()
            all_permissions_granted = true
        }
        if (!dialog_running && !first_time && !all_permissions_granted) {
//            cross_check()
            Next()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == camera_permission_request_code) {
                CheckPermissions()
            }
            if (requestCode == write_permission_request_code) {
                CheckPermissions()
            }
        }
        if (!all_permissions_granted) {
            CheckPermissions()
        }
        first_time = false
    }

    private fun cross_check() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                dialog_running = true
//                displayNeverAskAgainDialog()
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                dialog_running = true
//                displayNeverAskAgainDialog()
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                dialog_running = true
//                displayNeverAskAgainDialog()
            } else {
                all_permissions_granted = true
                Next()
            }
        } else {
        }
    }

    private fun Next() {
        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, AllDocsActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
