package com.oxodiceproductions.dockmaker.ui.activity.all_docs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.oxodiceproductions.dockmaker.database.*
import com.oxodiceproductions.dockmaker.R
import com.oxodiceproductions.dockmaker.databinding.ActivityAllDocsBinding
import com.oxodiceproductions.dockmaker.model.DocumentDataModel
import com.oxodiceproductions.dockmaker.ui.activity.document_view.DocumentViewActivity
import com.oxodiceproductions.dockmaker.ui.activity.settings.SettingsActivity
import com.oxodiceproductions.dockmaker.utils.AlertCreator
import com.oxodiceproductions.dockmaker.utils.CO
import com.oxodiceproductions.dockmaker.utils.Constants
import com.oxodiceproductions.dockmaker.utils.PDFMaker
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AllDocsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    /*This activity show all the documents present in the database*/
    var arrayList = ArrayList<DocumentDataModel>()
    lateinit var adapter: AllDocsRecyclerViewAdapter
    lateinit var binding: ActivityAllDocsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllDocsBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

//        IDProvider();
        binding.toolBarAllDocs.inflateMenu(R.menu.toolbar_menu)
        binding.toolBarAllDocs.setTitleTextColor(Color.BLACK)
        binding.clearCacheImage.setVisibility(View.GONE)
        binding.swipeRefreshAllDoc.setOnRefreshListener { Initializer() }

        //Navigation Drawer setup
        binding.navView.setNavigationItemSelectedListener(this)
        Initializer()
        binding.toolBarAllDocs.setOnMenuItemClickListener { item ->
            if (item.getItemId() === R.id.action_settings) {
                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
            }
            if (item.getItemId() === R.id.action_share_app) {
                shareApp()
            }
            if (item.getItemId() === R.id.action_clear_cache) {
                clearCache()
            }
            false
        }
        binding.toolBarAllDocs.setOnClickListener { view ->
            binding.drawerLayout.openDrawer(
                GravityCompat.START
            )
        }
        binding.addDocButton.setOnClickListener { view ->
            binding.progressBarAllDocs.setVisibility(View.VISIBLE)

            /* Here the document is created.
             * Each document has a unique name given by the calender function.
             * After creating the name for the document then it is stored in the database
             * and an intent is fired which change the activity to that document*/
            /*MyDatabase myDatabase = new MyDatabase(getApplicationContext());
            Calendar c = Calendar.getInstance();
            String DocId = "DocMaker" + "_" + c.get(Calendar.DATE) + "_" + c.get(Calendar.MONTH) + "_" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND);//date.getSeconds()+"_"+date.getDate()+"_"+(date.getMonth()+1)+"_"+date.getHours()+"_"+date.getMinutes();
            String date_s = c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR);
            String time_s = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
            myDatabase.InsertDocument(DocId, DocId, date_s, time_s, DocId);
            myDatabase.CreateTable(DocId);
            myDatabase.close();*/Thread {
            val c = Calendar.getInstance()
            val DocName =
                "DocMaker" + "_" + c[Calendar.DATE] + "_" + c[Calendar.MONTH] + "_" + c[Calendar.YEAR] + "_" + c[Calendar.HOUR_OF_DAY] + "_" + c[Calendar.MINUTE] + "_" + c[Calendar.SECOND] //date.getSeconds()+"_"+date.getDate()+"_"+(date.getMonth()+1)+"_"+date.getHours()+"_"+date.getMinutes();
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val documentDao = appDatabase.documentDao()
            val newDocument =
                Document(
                    Calendar.getInstance().timeInMillis, DocName
                )
            val docId = documentDao!!.insert(newDocument)
            val intent = Intent(applicationContext, DocumentViewActivity::class.java)
            intent.putExtra(Constants.SP_DOC_ID, docId)
            intent.putExtra("first_time", false)
            startActivity(intent)
        }.start()
        }
    }

    fun deleteUnusedFiles() {
        val animation = AnimationUtils.loadAnimation(
            applicationContext, R.anim.memory_free_anim
        )
        binding.clearCacheImage.setVisibility(View.VISIBLE)
        binding.clearCacheImage.setAnimation(animation)
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread { binding.clearCacheImage.setVisibility(View.GONE) }
            }
        }, 3000)
        Thread(label@ Runnable {
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val imageDao = appDatabase.imageDao()
            try {
                val usefulImages =
                    ArrayList<String>()
                val images =
                    imageDao!!.all as ArrayList<Image>
                for (image in images) {
                    usefulImages.add(image.imagePath)
                }
                val file = File(filesDir.path)
                val allFiles = file.listFiles()
                for (allFile in allFiles) {
                    val filePath = allFile.path
                    if (!usefulImages.contains(filePath)) {
                        val file1 = File(filePath)
                        file1.delete()
                    }
                }
            } catch (e: Exception) {
                binding.progressBarAllDocs.setVisibility(View.GONE)
            }
        }).start()
        /*
        MyDatabase myDatabase = null;
        try {
            ArrayList<String> usefulImages = new ArrayList<>();
            myDatabase = new MyDatabase(getApplicationContext());
            Cursor cc = myDatabase.LoadDocuments();
            cc.moveToFirst();
            do {
                Cursor ccc = myDatabase.LoadImagePaths(cc.getString(0));
                ccc.moveToFirst();
                do {
                    usefulImages.add(ccc.getString(0));
//                    Log.d("tagJi", "" + ccc.getString(0));
                } while (ccc.moveToNext());
            } while (cc.moveToNext());
            File file = new File(getFilesDir().getPath());
            File[] allFiles = file.listFiles();

//            Log.d("tagJi", "All files starts here");
            for (int i = 0; i < Objects.requireNonNull(allFiles).length; i++) {
                String filePath = allFiles[i].getPath();
//                Log.d("tagJi", allFiles[i].getPath());
                if (!usefulImages.contains(filePath)) {
                    File file1 = new File(filePath);
                    file1.delete();
                }
            }
        } catch (Exception e) {
            binding.progressBarAllDocs.setVisibility(View.GONE);
            //Toast.makeText(AllDocs.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            myDatabase.close();
        }*/
    }

    fun Initializer() {
        binding.progressBarAllDocs.setVisibility(View.VISIBLE)
        binding.addDocButton.setVisibility(View.VISIBLE)
        arrayList.clear()
        Thread(label@ Runnable {
            val appDatabase = AppDatabase.getInstance(applicationContext)
            val documentDao = appDatabase.documentDao()
            val imageDao = appDatabase.imageDao()
            val documents =
                documentDao!!.all as ArrayList<Document>
            if (documents == null) {
                CO.log("There are no documents")
                return@Runnable;
            }
            for (document in documents) {
                try {
                    val DocId = document.id
                    val DocName = document.name
                    val images =
                        imageDao!!.getImagesByDocId(DocId) as ArrayList<Image>
                    var sampleImageId: String? = ""
                    var numberOfImages = "0"
                    if (images != null) {
                        if (images.size > 0) {
                            numberOfImages = images.size.toString() + ""
                            sampleImageId = images[0].imagePath
                        }
                    }
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = document.time
                    val dateSdf =
                        SimpleDateFormat("dd/MM/yyyy")
                    val timeSdf = SimpleDateFormat("hh:mm")
                    val dateCreated = dateSdf.format(calendar.time)
                    val timeCreated = timeSdf.format(calendar.time)
                    arrayList.add(
                        DocumentDataModel(
                            false, DocId, sampleImageId!!, dateCreated, timeCreated, DocName, "",
                            numberOfImages
                        )
                    )
                } catch (e: Exception) {
                    CO.logError("Single document reading error: " + e.message)
                    e.printStackTrace()
                }
            }

//            Collections.sort(arrayList);
            runOnUiThread {
                try {
                    //adapter setup
                    adapter = AllDocsRecyclerViewAdapter()
                    binding.allDocsRecyclerView.setLayoutManager(
                        LinearLayoutManager(
                            applicationContext
                        )
                    )
                    binding.allDocsRecyclerView.setAdapter(adapter)
                    binding.emptyHomeTvAllDocs.setVisibility(View.GONE)
                    binding.swipeRefreshAllDoc.setRefreshing(false)
                } catch (e: Exception) {
                    binding.emptyHomeTvAllDocs.setVisibility(View.VISIBLE)
                    binding.allDocsRecyclerView.setVisibility(View.GONE)
                    binding.swipeRefreshAllDoc.setVisibility(View.GONE)
                }
                binding.progressBarAllDocs.setVisibility(View.GONE)
            }
        }).start()
    }

    /*public void Initializer() {
     */
    /* This function fill the arrayList which documents to show in the listView.
     * Here the database is used to fetch all the documents.
     * ListView adapter is used to insert documents in the listView*/
    /*
        binding.progressBarAllDocs.setVisibility(View.VISIBLE);
        binding.addDocButton.setVisibility(View.VISIBLE);
        arrayList.clear();
        MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        Cursor cc = myDatabase.LoadDocuments();
        try {
            cc.moveToFirst();
            do {
                String DocId = cc.getString(0);
                String SampleImageId = cc.getString(1);
                String DateCreated = cc.getString(2);
                String TimeCreated = cc.getString(3);
                String DocName = cc.getString(4);
                String Size = "0";
                String NumberOfPics = "0";
                try {
                    Size = myDatabase.getSize(DocId);
                    NumberOfPics = myDatabase.getNumberOfPics(DocId);
                } catch (Exception ignored) {
                }
                arrayList.add(new DocumentDataModel(false, DocId, SampleImageId, DateCreated, TimeCreated, DocName, Size, NumberOfPics));
            } while (cc.moveToNext());

            Collections.sort(arrayList);

//            int a=10/0;
            //adapter setup
            adapter = new AllDocsRecyclerViewAdapter(binding.addDocButton, arrayList, getApplicationContext(), this);
            binding.allDocsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            binding.allDocsRecyclerView.setAdapter(adapter);

            binding.emptyHomeTvAllDocs.setVisibility(View.GONE);
            binding.swipeRefreshAllDoc.setRefreshing(false);
        } catch (Exception e) {
            binding.emptyHomeTvAllDocs.setVisibility(View.VISIBLE);
            binding.allDocsRecyclerView.setVisibility(View.GONE);
            binding.swipeRefreshAllDoc.setVisibility(View.GONE);
        } finally {
            myDatabase.close();
        }
        binding.progressBarAllDocs.setVisibility(View.GONE);
    }
*/
    override fun onBackPressed() {
        finishAffinity()
    }

    private fun clearCache() {
        binding.progressBarAllDocs.setVisibility(View.VISIBLE)
        deleteUnusedFiles()
        deleteCache(applicationContext)
        binding.progressBarAllDocs.setVisibility(View.GONE)
    }

    private fun shareApp() {
        //firing intent for app link share to download the app
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        val message =
            "DocMaker\n\nA light weight app to turn\nimages into pdf files.\n\nLink:-\nhttps://play.google.com/store/apps/details?id=com.oxodiceproductions.docmaker"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(Intent.createChooser(intent, "Share this app with friends and family."))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        if (id == R.id.nav_share_app) {
            shareApp()
        } else if (id == R.id.nav_about_app) {
//            GoToAboutApp();
        } else if (id == R.id.nav_setting) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    inner class AllDocsRecyclerViewAdapter :
        RecyclerView.Adapter<AllDocsRecyclerViewAdapter.MyDocViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDocViewHolder {
            val view: View =
                LayoutInflater.from(applicationContext).inflate(R.layout.doc_rep, parent, false)
            return MyDocViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyDocViewHolder, position: Int) {
//            holder.binding.toolBarAllDocs.setTitle(arrayList.get(position).getDocName());
            holder.docNameTv.text = arrayList[position].docName
            try {
                val sdf = SimpleDateFormat("dd MMM yyyy")
                val parser = SimpleDateFormat("dd/MM/yyyy")
                val parsedDate = parser.parse(arrayList[position].dateCreated)
                val dateStr = sdf.format(parsedDate)
                holder.date_created_tv.text = dateStr
            } catch (e: Exception) {
                CO.log("Error: " + e.message)
                val dateStr = "Date: " + arrayList[position].dateCreated
                holder.date_created_tv.text = dateStr
            }
            try {
//                Todo:Change "hh:mm" to "hh:mm a" after some time
                val sdf = SimpleDateFormat("hh:mm")
                val parser = SimpleDateFormat("HH:mm")
                val parsedTime = parser.parse(arrayList[position].timeCreated)
                val timeStr = sdf.format(parsedTime)
                holder.time_created_tv.text = timeStr
            } catch (e: Exception) {
                val timeStr = "Time: " + arrayList[position].timeCreated
                holder.time_created_tv.text = timeStr
            }
            //            holder.date_created_tv.setText(getApplicationContext().getString(R.string.date, arrayList.get(position).getDateCreated()));
            holder.number_of_pics_tv.text = arrayList[position].numberOfPics
            holder.indexNumberTextView.text = "" + (position + 1)

            //thumbnail extraction
            try {
                val file = File(arrayList[position].sampleImageId)
                val options = RequestOptions().fitCenter().sizeMultiplier(0.2f)
                if (file.exists()) {
                    Glide.with(applicationContext).applyDefaultRequestOptions(options)
                        .load(file)
                        .into(holder.sample_image)
                }
            } catch (e: Exception) {
                val options = RequestOptions().fitCenter().sizeMultiplier(0.2f)
                Glide.with(applicationContext).applyDefaultRequestOptions(options)
                    .load(R.drawable.ic_baseline_broken_image_24)
                    .into(holder.sample_image)
            }


            //click listeners
            /* holder.optionsButton.setOnClickListener(v -> {
                int visibility = holder.optionsLayout.getVisibility();
                ImageButton currentOptionsButton = (ImageButton) v.findViewById(R.id.doc_options_button);
                if (visibility == View.GONE) {
                    holder.optionsLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.optionsLayout.setVisibility(View.GONE);
                }
                currentOptionsButton.setRotation(currentOptionsButton.getRotation() + 180);
            });*/

            /*   holder.shareButton.setOnClickListener(v -> {
                long DocId = arrayList.get(holder.getAdapterPosition()).getDocId();
                SharePdfButtonListener(DocId);
            });

            holder.deleteButton.setOnClickListener(v -> {
                long DocId = arrayList.get(holder.getAdapterPosition()).getDocId();
                String DocName = arrayList.get(holder.getAdapterPosition()).getDocName();
                int position1 = holder.getAdapterPosition();
                DeleteDoc(DocId, DocName, position1);
            });

            holder.detailsButton.setOnClickListener(v -> ShowDocDetails(arrayList.get(position)));
*/
            holder.clickLayout.setOnClickListener { view: View? ->
                GotoDocumentView(
                    position
                )
            }
        }

        private fun ShowDocDetails(doc: DocumentDataModel) {
            val alertCreator = AlertCreator()
            //size calculations
//            float size = Float.parseFloat(doc.getSize());
//            size = size / (1048576f);//1024 * 1024 = 1048576
//        Log.d(TAG, "ShowDocDetails: " + Float.parseFloat(doc.getSize()));
            val text = applicationContext
                .getString(
                    R.string.docDetails,
                    doc.docName,
                    doc.dateCreated,
                    doc.timeCreated,
                    doc.numberOfPics
                )
            alertCreator.showDialog(this@AllDocsActivity, text)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        fun GotoDocumentView(i: Int) {
            val intent = Intent(applicationContext, DocumentViewActivity::class.java)
            intent.putExtra(Constants.SP_DOC_ID, arrayList[i].docId)
            intent.putExtra("first_time", false)
            startActivity(intent)
        }

        private fun SharePdfButtonListener(DocId: Long) {
            Thread {
                try {
//                    ArrayList<String> ImagePaths = new ArrayList<>();
                    val DocName = ""
                    val appDatabase = AppDatabase.getInstance(applicationContext)
                    val imageDao = appDatabase.imageDao()
                    val images =
                        imageDao.getImagesByDocId(DocId) as ArrayList<Image>
                    Collections.sort(
                        images
                    ) { o1: Image, o2: Image ->
                        Integer.compare(
                            o1.imageIndex,
                            o2.imageIndex
                        )
                    }
                    //                    for (Image image : images) {
//                        ImagePaths.add(image.getImagePath());
//                    }
                    if (!images.isEmpty()) {
                        val pdfMaker = PDFMaker(applicationContext)
                        //                        String filepath = pdfMaker.MakeTempPDF(ImagePaths, myDatabase.getDocName(DocId));
                        val filepath = pdfMaker.MakeTempPDF(images, DocName)
                        if (filepath != "") {
                            val fileToShare = File(filepath)
                            val contentUri = FileProvider.getUriForFile(
                                applicationContext,
                                "com.oxodiceproductions.dockmaker",
                                fileToShare
                            )
                            applicationContext.grantUriPermission(
                                "*",
                                contentUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "application/pdf"
                            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                            startActivity(Intent.createChooser(shareIntent, "Share with"))
                        }
                    }
                } catch (e: Exception) {
                    Log.d("tagJi", "SharePdfButtonListener: " + e.message)
                }
            }.start()
        }

        fun DeleteDoc(DocId: Long, DocName: String?, position: Int) {
            //do not put notifyItemRemoved in a thread because it will not work there properly.
            AlertDialog.Builder(this@AllDocsActivity).setTitle("Do you want to delete this document")
                .setMessage(DocName)
                .setCancelable(true)
                .setPositiveButton("delete") { dialog: DialogInterface, which: Int ->
                    CO.deleteDocument(applicationContext, DocId)
                    arrayList.removeAt(position)
                    notifyItemRemoved(position)
                    dialog.dismiss()
                }.setNegativeButton(
                    "cancel"
                ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .show()
        }

         inner class MyDocViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            //implements View.OnClickListener {
            var date_created_tv: TextView
            var time_created_tv: TextView
            var number_of_pics_tv: TextView
            var docNameTv: TextView
            var sample_image: ImageView
            var indexNumberTextView: TextView

            //            ImageButton optionsButton, deleteButton, shareButton, detailsButton;
            //            LinearLayout optionsLayout;
            var clickLayout: CardView

            init {
                date_created_tv = itemView.findViewById(R.id.textView3)
                time_created_tv = itemView.findViewById(R.id.textView2)
                sample_image = itemView.findViewById(R.id.doc_imageview)
                number_of_pics_tv = itemView.findViewById(R.id.textView5)
                docNameTv = itemView.findViewById(R.id.doc_name_tv)
                //                optionsButton = itemView.findViewById(R.id.doc_options_button);
                indexNumberTextView = itemView.findViewById(R.id.index_number_text_view)
                //                deleteButton = itemView.findViewById(R.id.doc_rep_delete);
//                shareButton = itemView.findViewById(R.id.doc_rep_share);
//                detailsButton = itemView.findViewById(R.id.doc_rep_details);
//                optionsLayout = itemView.findViewById(R.id.doc_rep_options_layout);
                clickLayout = itemView.findViewById(R.id.doc_rep_click_layout)
                //                itemView.setOnClickListener(this);
            } /*   @Override
            public void onClick(View v) {
                TextView indexTv = v.findViewById(R.id.index_number_text_view);
                int i = Integer.parseInt(indexTv.getText().toString()) - 1;
                GotoDocumentView(i);
            }*/
        }
    }

    companion object {
        fun deleteCache(context: Context) {
            try {
                val dir = context.cacheDir
                val done = deleteDir(dir)
                if (done) {
                    Toast.makeText(context, "Cache cleared", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun deleteDir(dir: File?): Boolean {
            return if (dir != null && dir.isDirectory) {
                val children = dir.list()!!
                for (child in children) {
                    val success = deleteDir(File(dir, child))
                    if (!success) {
                        return false
                    }
                }
                dir.delete()
            } else if (dir != null && dir.isFile) {
                dir.delete()
            } else {
                false
            }
        }
    }
}