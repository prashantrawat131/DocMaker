package com.oxodiceproductions.dockmaker.ui.activity.single_image

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GestureDetectorCompat
import com.oxodiceproductions.dockmaker.database.AppDatabase
import com.oxodiceproductions.dockmaker.database.Image
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.databinding.ActivitySingleImageBinding
import com.oxodiceproductions.dockmaker.ui.activity.camera.CameraActivity
import com.oxodiceproductions.dockmaker.ui.activity.document_view.DocumentViewActivity
import com.oxodiceproductions.dockmaker.ui.activity.editing.EditingImageActivity
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import java.io.File

class SingleImageActivity  : AppCompatActivity() {

    var ImagePath: String? = "-1"
    var DocId: Long = -1
    lateinit var sharedPreferences: SharedPreferences
    var imagesList = ArrayList<String?>()
    private var gestureDetectorCompat: GestureDetectorCompat? = null
    lateinit var binding: ActivitySingleImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleImageBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        sharedPreferences = getSharedPreferences("DocMaker", MODE_PRIVATE)
        ImagePath = intent.extras!!.getString("ImagePath", "-1")
        DocId = intent.extras!!.getLong("DocId", -1)
        binding.imageSingleImage.setImageURI(Uri.fromFile(File(ImagePath)))
        binding.progressBarSingleImage.setVisibility(View.GONE)
        gestureDetectorCompat = GestureDetectorCompat(this, MyGestureDetector())
        populateImageList()
        binding.backButtonSingleImage.setOnClickListener { view -> onBackPressed() }
        binding.shareSingleImage.setOnClickListener { view ->
            binding.progressBarSingleImage.setVisibility(View.VISIBLE)
            try {
                val newFile = File(ImagePath)
                val contentUri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.oxodiceproductions.dockmaker",
                    newFile
                )
                grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                startActivity(Intent.createChooser(shareIntent, "hello"))
            } catch (ignored: Exception) {
            }
            binding.progressBarSingleImage.setVisibility(View.GONE)
        }
        binding.deleteSingleImage.setOnClickListener { view ->
            binding.progressBarSingleImage.setVisibility(View.VISIBLE)
            val viewGroup: ViewGroup = view.findViewById(R.id.alert_main_layout)
            val alertDialogBuilder =
                AlertDialog.Builder(this@SingleImageActivity)
            val customView = arrayOf<View>(
                layoutInflater.inflate(
                    R.layout.alert_box,
                    viewGroup,
                    false
                )
            )
            alertDialogBuilder.setView(customView[0])
            val textView = customView[0].findViewById<TextView>(R.id.textView9)
            val cancel_button =
                customView[0].findViewById<Button>(R.id.button)
            val ok_button =
                customView[0].findViewById<Button>(R.id.button2)
            textView.text = resources.getText(R.string.t16)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
            ok_button.setOnClickListener { view12: View? ->
                Thread {
                    val appDatabase =
                        AppDatabase.getInstance(applicationContext)
                    val imageDao = appDatabase.imageDao()
                    imageDao.deleteImageByPath(ImagePath!!)
                    CO.deleteFile(ImagePath)
                    val intent =
                        Intent(this@SingleImageActivity, DocumentViewActivity::class.java)
                    intent.putExtra(
                        Constants.SP_DOC_ID,
                        DocId
                    )
                    startActivity(intent)
                    finish()
                }.start()
            }
            cancel_button.setOnClickListener { view1: View? ->
                binding.progressBarSingleImage.setVisibility(View.GONE)
                alertDialog.dismiss()
            }
        }
        binding.editSingleImage.setOnClickListener { view ->
            binding.progressBarSingleImage.setVisibility(View.VISIBLE)
            val intent = Intent(this@SingleImageActivity, EditingImageActivity::class.java)
            intent.putExtra("ImagePath", ImagePath)
            editImageActivityLauncher.launch(intent)
        }
        binding.retakeSingleImage.setOnClickListener { view ->
            binding.progressBarSingleImage.setVisibility(View.VISIBLE)
            val intent = Intent(this@SingleImageActivity, CameraActivity::class.java)
            intent.putExtra(Constants.SP_DOC_ID, DocId)
            intent.putExtra("ImagePath", ImagePath)
            startActivity(intent)
            finish()
        }
        binding.previousSingleImage.setOnClickListener { view -> move(-1) }
        binding.nextSingleImage.setOnClickListener { view -> move(1) }
        binding.downloadSingleImage.setOnClickListener { view ->
            CO.downloadImage(
                applicationContext,
                ImagePath
            )
        }
    }

    var editImageActivityLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            //                    CommonOperations.log("Arriving after result");
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data == null) {
                    CO.log("Data is null")
                    return@ActivityResultCallback
                }
                //                        assert data != null;
                val receivedImagePath = data.extras!!.getString("ImagePath")
                CO.log("Received image path: $receivedImagePath")
                Thread {
                    try {
                        val appDatabase =
                            AppDatabase.getInstance(applicationContext)
                        val imageDao = appDatabase.imageDao()
                        val image =
                            imageDao.getImageByImagePath(DocId, ImagePath)
                        image.imagePath = receivedImagePath!!
                        imageDao.update(image)

                        //setting the new image
                        imagesList[imagesList.indexOf(ImagePath)] = receivedImagePath
                        ImagePath = receivedImagePath
                        runOnUiThread {
                            binding.imageSingleImage.setImageURI(
                                Uri.fromFile(
                                    File(
                                        ImagePath
                                    )
                                )
                            )
                            binding.progressBarSingleImage.setVisibility(View.GONE)
                        }
                    } catch (e: Exception) {
                        CO.log("Error while updating the image path for edited image: " + e.message)
                    }
                }.start()
            }
        }
    )

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

                */
    /*
                String newImagePath = data.getExtras().getString("ImagePath");
                MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                myDatabase.retake(DocId, ImagePath, newImagePath);
                myDatabase.close();

                //setting the new image
                ImagePath = newImagePath;
                binding.imageSingleImage.setImageURI(Uri.fromFile(new File(ImagePath)));
                binding.progressBarSingleImage.setVisibility(View.GONE);*/
    /*
            }
        }
    }*/
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetectorCompat!!.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    internal inner class MyGestureDetector : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (velocityX < -2000) {
                move(1)
            } else if (velocityX > 2000) {
                move(-1)
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    fun populateImageList() {
        Thread {
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val imageDao = appDatabase.imageDao()
            val imageArrayList =
                imageDao.getImagesByDocId(DocId) as ArrayList<Image>
            for (image in imageArrayList) {
                imagesList.add(image.imagePath)
            }
            runOnUiThread {
                val index = "" + (imagesList.indexOf(ImagePath) + 1)
                binding.singleImageIndexTv.setText(index)
            }
        }.start()
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

    fun move(x: Int) {
        val index = imagesList.indexOf(ImagePath)
        var next = index + x
        if (next > imagesList.size - 1) {
            next = 0
        } else if (next < 0) {
            next = imagesList.size - 1
        }
        val stringIndex = "" + (next + 1)
        binding.singleImageIndexTv.setText(stringIndex)
        ImagePath = imagesList[next]
        binding.imageSingleImage.setImageURI(Uri.fromFile(File(ImagePath)))
    }

    override fun onBackPressed() {
        binding.progressBarSingleImage.setVisibility(View.VISIBLE)
        val intent = Intent(this@SingleImageActivity, DocumentViewActivity::class.java)
        intent.putExtra(Constants.SP_DOC_ID, DocId)
        startActivity(intent)
    }

    companion object {
        const val editingSingleImageId = 1908
    }
}
