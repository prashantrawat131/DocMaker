package com.oxodiceproductions.dockmaker.ui.activity.document_view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.oxodiceproductions.dockmaker.database.*
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.databinding.ActivityDocumentViewBinding
import com.oxodiceproductions.dockmaker.ui.activity.all_docs.AllDocsActivity
import com.oxodiceproductions.dockmaker.ui.activity.camera.CameraActivity
import com.oxodiceproductions.dockmaker.ui.activity.editing.EditingImageActivity
import com.oxodiceproductions.dockmaker.ui.activity.single_image.SingleImageActivity
import com.oxodiceproductions.dockmaker.utils.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class DocumentViewActivity : AppCompatActivity() {
    var imagesArrayList = ArrayList<Image>()
    var ImagePathsChecker = ArrayList<Boolean>()
    var numberOfCheckedImages = 0
    var adapter: DocViewRecyclerViewAdapter? = null
    var DocId: Long = 0
    lateinit var DocName: String
    var galleryImagesUris = ArrayList<Uri?>()
    var galleryImagesPaths = ArrayList<String>()
    var binding: ActivityDocumentViewBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentViewBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        InitialWork()
        DocId = intent.extras!!.getLong("DocId", -1)
        if (DocId == -1L) {
            binding!!.progressBarDocView.visibility = View.VISIBLE
            GoToAllDocs()
        }
        Thread {
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val documentDao = appDatabase.documentDao()
            val documents =
                documentDao.getDocById(DocId) as ArrayList<Document>
            DocName = documents[0].name
            runOnUiThread { binding!!.docNameTvDocView.text = DocName }
        }.start()
        setAdapter()
        populateList()
        binding!!.backButtonDocView.setOnClickListener { view ->
            binding!!.progressBarDocView.visibility = View.VISIBLE
            onBackPressed()
        }
        binding!!.shareDocButton.setOnClickListener { view ->
            binding!!.progressBarDocView.visibility = View.VISIBLE
            Thread {
                try {
                    val pdfMaker = PDFMaker(applicationContext)
                    val filepath = pdfMaker.MakeTempPDF(imagesArrayList, DocName)
                    if (filepath != "") {
                        val fileToShare = File(filepath)
                        val contentUri = FileProvider.getUriForFile(
                            applicationContext,
                            "com.oxodiceproductions.dockmaker",
                            fileToShare
                        )
                        grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "application/pdf"
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                        startActivity(Intent.createChooser(shareIntent, "Share with"))
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "File path not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    runOnUiThread {
                        binding!!.progressBarDocView.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        binding!!.progressBarDocView.visibility = View.GONE
                    }
                }
            }.start()
        }
        binding!!.downloadDocButton.setOnClickListener { view ->
//            createFile();
            val notificationModule = NotificationModule()
            notificationModule.generateNotification(
                applicationContext,
                DocName,
                "Go to downloads."
            )
            try {
                val downloadsFolder =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileOutputStream =
                    FileOutputStream("$downloadsFolder/$DocName.pdf")
                val pdfMaker = PDFMaker(applicationContext)
                pdfMaker.downloadPdf(imagesArrayList, fileOutputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        binding!!.pdfPreviewDovView.setOnClickListener { view ->
            binding!!.progressBarDocView.visibility = View.VISIBLE
            val pdfCreationThread = Thread {
                var emptyAvailable = false
                try {
                    for (image in imagesArrayList) {
                        val file = File(image.imagePath)
                        if (!file.exists()) {
                            emptyAvailable = true
                            break
                        }
                    }
                } catch (ignored: Exception) {
                }
                if (emptyAvailable) {
                    val alertCreator = AlertCreator()
                    alertCreator.createAlertForZeroSizeImages(this@DocumentViewActivity)
                } else {
                    val pdfMaker = PDFMaker(applicationContext)
                    val path = pdfMaker.MakeTempPDF(imagesArrayList, DocName)
                    runOnUiThread {
                        binding!!.progressBarDocView.visibility = View.GONE
                    }
                    if (path != "") {
                        val uri = FileProvider.getUriForFile(
                            applicationContext,
                            "com.oxodiceproductions.dockmaker",
                            File(path)
                        )
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "application/pdf")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        startActivity(intent)
                    }
                }
            }
            pdfCreationThread.start()
        }
        binding!!.deleteSelectedImagesButton.setOnClickListener { view ->
            val alertDialogBuilder =
                AlertDialog.Builder(this@DocumentViewActivity)
            val customView = arrayOf(
                layoutInflater.inflate(
                    R.layout.alert_box,
                    findViewById(R.id.alert_main_layout),
                    false
                )
            )
            alertDialogBuilder.setView(customView[0])
            val textView = customView[0].findViewById<TextView>(R.id.textView9)
            val cancel_button =
                customView[0].findViewById<Button>(R.id.button)
            val ok_button =
                customView[0].findViewById<Button>(R.id.button2)
            textView.text = getText(R.string.docViewAlertText)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
            ok_button.setOnClickListener { view2: View? ->
                for (k in ImagePathsChecker.indices) {
                    if (ImagePathsChecker[k]) {
                        delete(imagesArrayList[k].imagePath)
                        imagesArrayList.removeAt(k)
                    }
                }
                ImagePathsChecker.clear()
                ImagePathsChecker = ArrayList()
                for (k in imagesArrayList.indices) {
                    ImagePathsChecker.add(false)
                }
                refreshRecyclerView()
                numberOfCheckedImages = 0
                updateScene()
                alertDialog.dismiss()
            }
            cancel_button.setOnClickListener { view2: View? -> alertDialog.dismiss() }
        }
        binding!!.gallerySelect.setOnClickListener { view ->
            binding!!.progressBarDocView.visibility = View.VISIBLE
            val fileManagerIntent =
                Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            fileManagerIntent.type = "image/*"
            fileManagerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            galleryImageSelectActivityLauncher.launch(fileManagerIntent)
        }
        binding!!.clickNewImageButtonDocView.setOnClickListener { view ->
            binding!!.progressBarDocView.visibility = View.VISIBLE
            val intent = Intent(this@DocumentViewActivity, CameraActivity::class.java)
            intent.putExtra("DocId", DocId)
            startActivity(intent)
            finish()
        }
        binding!!.docNameTvDocView.setOnClickListener { view ->
            val alert =
                AlertDialog.Builder(this)
            val dialogView = LayoutInflater.from(applicationContext)
                .inflate(R.layout.name_changer_dialog_box, null, false)
            val input = dialogView.findViewById<EditText>(R.id.editTextTextPersonName2)
            val okButton =
                dialogView.findViewById<Button>(R.id.button4)
            val cancelButton =
                dialogView.findViewById<Button>(R.id.button3)
            input.setText(DocName)
            alert.setView(dialogView)
            val alertDialog = alert.create()
            alertDialog.show()
            input.requestFocus()
            input.selectAll()
            okButton.setOnClickListener { view2: View? ->
                if (!input.text.toString().isEmpty()) {
                    DocName = input.text.toString()
                    binding!!.docNameTvDocView.text = DocName
                    ChangeName()
                    binding!!.progressBarDocView.visibility = View.GONE
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(
                        this@DocumentViewActivity,
                        "Please fill something",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            cancelButton.setOnClickListener { view2: View? -> alertDialog.dismiss() }
        }
    }

    private fun setAdapter() {
        imagesArrayList = ArrayList()
        adapter = DocViewRecyclerViewAdapter()
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding!!.docRecyclerView.layoutManager = linearLayoutManager
        binding!!.docRecyclerView.adapter = adapter
    }

    private fun refreshRecyclerView() {
        runOnUiThread {
            if (imagesArrayList.size == 0) {
                binding!!.docRecyclerView.visibility = View.GONE
                binding!!.emptyDocTvDocView.visibility = View.VISIBLE
                CO.log("List empty so quiting refresh recycler view")
                return@runOnUiThread
            }
            adapter!!.notifyDataSetChanged()
            binding!!.emptyDocTvDocView.visibility = View.GONE
            CO.log("Adapter set up successful")
            if (emptyAvailable) {
                Toast.makeText(
                    applicationContext,
                    "Empty images available",
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding!!.progressBarDocView.visibility = View.GONE
        }
    }

    /* private void saveTheCurrentListToDatabase(boolean exitApp) {
        new Thread(() -> {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            ImageDao imageDao = appDatabase.imageDao();
            ArrayList<Image> oldList = (ArrayList<Image>) imageDao.getAll();

            if (oldList.equals(imageDao)) {
                return;
            }

            for (Image image : oldList) {
                imageDao.deleteImageByPath(image.getImagePath());
            }

            for (Image image : imagesArrayList) {
                imageDao.insert(image);
            }

        }).start();
    }*/
    override fun onStop() {
//        saveTheCurrentListToDatabase(true);
        super.onStop()
    }

    private fun GoToAllDocs() {
        val intent = Intent(this@DocumentViewActivity, AllDocsActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun delete(ImagePath: String) {
        Thread {
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val imageDao = appDatabase.imageDao()
            imageDao.deleteImageByPath(ImagePath)
            CO.deleteFile(ImagePath)
        }.start()
        /* MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        myDatabase.DeleteImage(ImagePath, DocId);
        myDatabase.close();
        CommonOperations.deleteFile(ImagePath);*/
    }

    var galleryImageSelectActivityLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        assert(result.resultCode == RESULT_OK)
        val data = result.data!!
        binding!!.progressBarDocView.visibility = View.VISIBLE
        if (data.clipData == null) {
            //                    this means only single image
            val uri = data.data
            galleryImagesUris.add(uri)
        } else {
            //                    this is for multiple images
            for (i in 0 until data.clipData!!.itemCount) {
                //                            CommonOperations.log("IMage number " + i + " " + data.getClipData().getItemAt(i).getUri());
                galleryImagesUris.add(data.clipData!!.getItemAt(i).uri)
            }
            //                        CommonOperations.log("Gallery images uris count: " + galleryImagesUris.size());
        }
        saveSelectedImage()
    }

    fun goForEditing() {
//        CommonOperations.log("goForEditing starting image path count: " + galleryImagesPaths.size());
        //checking if there are images left to crop
        if (galleryImagesPaths.size > 0) {
            val imagePathForEditing = galleryImagesPaths[0]
            galleryImagesPaths.removeAt(0)
            val intent = Intent(this@DocumentViewActivity, EditingImageActivity::class.java)
            intent.putExtra("ImagePath", imagePathForEditing)
            editImageActivityLauncher.launch(intent)
        } else {
            populateList()
        }
    }

    var editImageActivityLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result -> //                    CommonOperations.log("Arriving after result");
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data == null) {
                    CO.log("Data is null")
                    return@ActivityResultCallback
                }
                //                        assert data != null;
                val ImagePath = data.extras!!.getString("ImagePath")!!
                Thread {
                    val appDatabase =
                        AppDatabase.getInstance(applicationContext)
                    val imageDao = appDatabase.imageDao()
                    val images =
                        imageDao.all
                    val index = images?.size ?: 0
                    //                            CommonOperations.log("Saving image at index: " + index+ "" +
                    //                                    " ImagePath: "+ImagePath);
                    val newImage =
                        Image(ImagePath, index, DocId)
                    imageDao.insert(newImage)
                    runOnUiThread {
                        //                                CommonOperations.log("Going again for editing");
                        goForEditing()
                    }
                }.start()
            } else {
                CO.log("Result not ok")
            }
            /*
                          MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                          myDatabase.InsertImage(DocId, ImagePath);
                          myDatabase.close();
                          goForEditing();*/
        }
    )

    private fun saveSelectedImage() {
        binding!!.progressBarDocView.visibility = View.VISIBLE
        val runnable = Runnable {
            for (i in galleryImagesUris.indices) {
                val imageCompressor = ImageCompressor(applicationContext)
                val filePath = imageCompressor.compress(galleryImagesUris[i])
                if (filePath != "-1") {
                    galleryImagesPaths.add(filePath)
                }

//                CommonOperations.log("Saving selected image index: " + i);
            }
            galleryImagesUris.clear()
            goForEditing()
        }
        val t = Thread(runnable)
        t.start()
    }

    //    private void createFile() {//Uri pickerInitialUri) {
    //        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    //        intent.addCategory(Intent.CATEGORY_OPENABLE);
    //        intent.setType("application/pdf");
    //        String name = DocName + ".pdf";
    //        intent.putExtra(Intent.EXTRA_TITLE, name);
    ////            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
    //        startActivityForResult(intent, CREATE_FILE);
    //    }
    private fun populateList() {
        Thread {
            try {
                ImagePathsChecker.clear()
                imagesArrayList.clear()
                val appDatabase = AppDatabase.getInstance(applicationContext)
                val imageDao = appDatabase.imageDao()
                val images =
                    imageDao.getImagesByDocId(DocId) as ArrayList<Image>
                for (image in images) {
//                    CommonOperations.log("Adding: " + image.getId() + " " + image.getImagePath() + " " + image.getImageIndex());
                    imagesArrayList.add(image)
                    ImagePathsChecker.add(false)
                }
                runOnUiThread { refreshRecyclerView() }
            } catch (e: Exception) {
                runOnUiThread {
                    binding!!.emptyDocTvDocView.visibility = View.VISIBLE
                    binding!!.docRecyclerView.visibility = View.GONE
                    binding!!.progressBarDocView.visibility = View.GONE
                }
            }
        }.start()

        /* try (MyDatabase myDatabase = new MyDatabase(getApplicationContext())) {
            Cursor cc = myDatabase.LoadImagePaths(DocId);
            cc.moveToFirst();
            do {
                ImagePaths.add(cc.getString(0));
                ImagePathsChecker.add(false);
            } while (cc.moveToNext());

            //recycler view setup
//            CommonOperations.log("Size: " + ImagePaths.size());
            DocViewRecyclerViewAdapter adapter = new DocViewRecyclerViewAdapter();
            binding.docRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            binding.docRecyclerView.setAdapter(adapter);

//            ItemTouchHelper helper = new ItemTouchHelper(new MyTouches(adapter));
//            helper.attachToRecyclerView(recyclerView);

            binding.swipeRefreshDocView.setRefreshing(false);
            binding.docRecyclerView.setVisibility(View.VISIBLE);
            binding.swipeRefreshDocView.setVisibility(View.VISIBLE);
            binding.emptyDocTvDocView.setVisibility(View.GONE);
            if (emptyAvailable) {
                Toast.makeText(getApplicationContext(), "Empty images available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            binding.emptyDocTvDocView.setVisibility(View.VISIBLE);
            binding.docRecyclerView.setVisibility(View.GONE);
            binding.swipeRefreshDocView.setVisibility(View.GONE);
        }*/
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ChangeName()
        val intent = Intent(this@DocumentViewActivity, AllDocsActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun ChangeName() {
        Thread {
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val documentDao = appDatabase.documentDao()
            documentDao.updateName(DocId, DocName)
        }.start()
    }

    fun InitialWork() {
        binding!!.emptyDocTvDocView.visibility = View.GONE
        binding!!.progressBarDocView.visibility = View.GONE
        binding!!.deleteSelectedImagesButton.visibility = View.GONE
    }

    fun updateScene() {
        /*this function looks up whether any image is numberOfCheckedImages
		or not then it will perform the respective tasks
		*/
        if (numberOfCheckedImages == 0) {
            binding!!.deleteSelectedImagesButton.visibility = View.GONE
            binding!!.clickNewImageButtonDocView.visibility = View.VISIBLE
            binding!!.docViewOptionsLayout.visibility = View.VISIBLE
            binding!!.gallerySelect.visibility = View.VISIBLE
        } else {
            binding!!.docViewOptionsLayout.visibility = View.GONE
            binding!!.deleteSelectedImagesButton.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(
                applicationContext, R.anim.rotation_left_right_repeat
            )
            binding!!.deleteSelectedImagesButton.startAnimation(animation)
            binding!!.clickNewImageButtonDocView.visibility = View.GONE
            binding!!.gallerySelect.visibility = View.GONE
        }
    }

    //    public int dpFromPx(float px) {
    ////        CommonOperations.log(getResources().getDisplayMetrics().density+" density");
    //        return (int) (px / getResources().getDisplayMetrics().density);
    //    }
    inner class DocViewRecyclerViewAdapter :
        RecyclerView.Adapter<DocViewRecyclerViewAdapter.MyDocViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDocViewHolder {
            val view = LayoutInflater.from(applicationContext)
                .inflate(R.layout.single_image_rep, parent, false)
            return MyDocViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyDocViewHolder, position: Int) {
            val imageFile = File(imagesArrayList[position].imagePath)
            holder.positionTextView.text = getString(R.string.docViewImagePosition, position + 1)
            val size = imageFile.length().toFloat() / (1024 * 1024)
            emptyAvailable =
                size == 0.0f

//            String sizeString = "Size:\n" + String.format(Locale.getDefault(), "%.2f MB", size);
//            holder.sizeTextView.setText(sizeString);
            val bitmapLoadOptions = BitmapFactory.Options()
            bitmapLoadOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imageFile.path, bitmapLoadOptions)
            var width = bitmapLoadOptions.outWidth
            var height = bitmapLoadOptions.outHeight
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels
            val screenWidth = displayMetrics.widthPixels
            val maxImageWidth = (screenWidth.toFloat() / 1.5f).toInt() //this is in dp
            val minImageWidth = (maxImageWidth.toFloat() / 1.5f).toInt()
            //            int actualWidth = dpFromPx(width);
//            int actualHeight = dpFromPx(height);
            var ratio = width.toFloat() / height.toFloat()
            if (ratio.toInt() == 0) {
                ratio += 1f
            }
            if (width >= maxImageWidth) {
                width = maxImageWidth
                height = width / ratio.toInt()
            } else if (width < minImageWidth) {
                width = minImageWidth
                height = width / ratio.toInt()
            }
            val layoutParams = LinearLayout.LayoutParams(width, height)
            //            layoutParams.gravity = GravityCompat.END;
            holder.imageView.layoutParams = layoutParams

//            FrameLayout.LayoutParams mainLayoutParam = (FrameLayout.LayoutParams) holder.mainLayout.getLayoutParams();
//            mainLayoutParam.gravity = GravityCompat.END;
//            holder.mainLayout.setLayoutParams(mainLayoutParam);
            var options = RequestOptions().sizeMultiplier(0.5f)
            options = options.centerCrop()
            Glide.with(applicationContext).applyDefaultRequestOptions(options)
                .load(imageFile)
                .placeholder(R.drawable.image_placeholder).into(holder.imageView)

//            holder.checkBox.setChecked(ImagePathsChecker.get(position));

            /*  holder.checkBox.setOnClickListener(view2 -> {
                CheckBox checkBox = view2.findViewById(R.id.single_image_checkbox);
                if (checkBox.isChecked()) {
                    //this means after user clicked the checkbox become checked
                    selectImage(position);
                } else {
                    //this means after user clicked the checkbox become unchecked
                    deselectImage(position);
                }
            });
*/
//            upDownButtonControls(position, holder.upButton, holder.downButton);
        }

        override fun getItemCount(): Int {
            return imagesArrayList.size
        }

        inner class MyDocViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener {
            var imageView: ImageView

            //            CheckBox checkBox;
            //            TextView sizeTextView;
            var positionTextView: TextView
            var mainLayout: LinearLayout

            //            ImageButton upButton, downButton;
            init {
                mainLayout = itemView.findViewById(R.id.simple_image_cardView)
                imageView = itemView.findViewById(R.id.imageView3)
                //                checkBox = itemView.findViewById(R.id.single_image_checkbox);
//                sizeTextView = itemView.findViewById(R.id.textView6);
                positionTextView = itemView.findViewById(R.id.positionTextView)
                //                upButton = itemView.findViewById(R.id.up_button);
//                downButton = itemView.findViewById(R.id.down_button);
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View) {

                //this is a temporary method to get position i have to change this
                val textView = v.findViewById<TextView>(R.id.positionTextView)
                val position = textView.text.toString().toInt() - 1
                if (numberOfCheckedImages == 0) {
                    //this means no image is numberOfCheckedImages and the user wants to preview the image
                    GoToSingleImage(position)
                } else {
                    /*CheckBox checkBox = v.findViewById(R.id.single_image_checkbox);
                    if (checkBox.isChecked()) {
                        //this means the user wants to remove the selection
                        checkBox.setChecked(false);
                        deselectImage(position);
                    } else {
                        //this means the user wants to select the image
                        checkBox.setChecked(true);
                        selectImage(position);
                    }*/
                }
            }
        }

        fun Swap(p1: Int, p2: Int) {
            /* Runnable runnable = () -> {
                //database operations must be performed in separate thread
                MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                myDatabase.updateDoc(ImagePaths.get(p1), ImagePaths.get(p2), DocId);
                myDatabase.close();
            };
            Thread thread = new Thread(runnable);
            thread.start();*/
/*
            new Thread(() -> {
                AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                ImageDao imageDao = appDatabase.imageDao();

                Image image1 = imageDao.getImageByIndex(DocId, p1);
                Image image2 = imageDao.getImageByIndex(DocId, p2);

                image1.setImageIndex(p2);
                image2.setImageIndex(p1);

                imageDao.update(image1);
                imageDao.update(image2);
            }).start();*/
/*
            Collections.swap(imagesArrayList, p1, p2);
            Collections.swap(ImagePathsChecker, p1, p2);

            runOnUiThread(() -> {
                this.notifyDataSetChanged();
            });*/
        }

        fun selectImage(i: Int) {
            numberOfCheckedImages++
            ImagePathsChecker[i] = true
            updateScene()
        }

        fun deselectImage(i: Int) {
            numberOfCheckedImages--
            ImagePathsChecker[i] = false
            updateScene()
        }

        /*

        void upDownButtonControls(int i, ImageButton upButton, ImageButton downButton) {

           */
        /* //removes up button from first image
            if (i == 0) {
                upButton.setVisibility(View.GONE);
                downButton.setVisibility(View.VISIBLE);
            }

            //removes down button from last image
            if (i == imagesArrayList.size() - 1) {
                downButton.setVisibility(View.GONE);
                upButton.setVisibility(View.VISIBLE);
            }

            //setting up click listeners
            int index = imagesArrayList.get(i).getImageIndex();
            upButton.setOnClickListener(view22 -> Swap(index, index - 1));
            downButton.setOnClickListener(view23 -> Swap(index, index + 1));

            upButton.setFocusable(false);//for clicking the recyclerView item
            downButton.setFocusable(false);//for clicking the recyclerView item*/
        /*

        }
*/
        fun GoToSingleImage(single_image_position: Int) {
            binding!!.progressBarDocView.visibility = View.VISIBLE
            val runnable = Runnable {
                val intent = Intent(applicationContext, SingleImageActivity::class.java)
                intent.putExtra(
                    "ImagePath",
                    imagesArrayList[single_image_position].imagePath
                )
                intent.putExtra("DocId", DocId)
                startActivity(intent)
            }
            val thread = Thread(runnable)
            thread.start()
        }
    }

    companion object {
        var emptyAvailable = false
    }
}