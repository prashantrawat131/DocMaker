package com.oxodiceproductions.dockmaker;

import static androidx.core.content.FileProvider.getUriForFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.oxodiceproductions.dockmaker.databinding.ActivityAllDocsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class AllDocs extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    /*This activity show all the documents present in the database*/
    ArrayList<DocumentDataModel> arrayList = new ArrayList<>();
    AllDocsRecyclerViewAdapter adapter;
    ActivityAllDocsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllDocsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        IDProvider();

        binding.toolBarAllDocs.inflateMenu(R.menu.toolbar_menu);
        binding.toolBarAllDocs.setTitleTextColor(Color.BLACK);

        binding.clearCacheImage.setVisibility(View.GONE);

        binding.swipeRefreshAllDoc.setOnRefreshListener(this::Initializer);

        //Navigation Drawer setup
        binding.navView.setNavigationItemSelectedListener(this);

        Initializer();

        binding.toolBarAllDocs.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(AllDocs.this, MySettings.class));
            }
            if (item.getItemId() == R.id.action_share_app) {
                shareApp();
            }
            if (item.getItemId() == R.id.action_clear_cache) {
                clearCache();
            }
            return false;
        });

        binding.toolBarAllDocs.setOnClickListener((view) -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.addDocButton.setOnClickListener(view -> {
            binding.progressBarAllDocs.setVisibility(View.VISIBLE);

            /* Here the document is created.
             * Each document has a unique name given by the calender function.
             * After creating the name for the document then it is stored in the database
             * and an intent is fired which change the activity to that document*/
            MyDatabase myDatabase = new MyDatabase(getApplicationContext());
            Calendar c = Calendar.getInstance();
            String DocId = "DocMaker" + "_" + c.get(Calendar.DATE) + "_" + c.get(Calendar.MONTH) + "_" + c.get(Calendar.YEAR) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND);//date.getSeconds()+"_"+date.getDate()+"_"+(date.getMonth()+1)+"_"+date.getHours()+"_"+date.getMinutes();
            String date_s = c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR);
            String time_s = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
            myDatabase.InsertDocument(DocId, DocId, date_s, time_s, DocId);
            myDatabase.CreateTable(DocId);
            myDatabase.close();
            Intent in = new Intent(getApplicationContext(), DocumentViewActivity.class);
            in.putExtra("DocId", DocId);
            in.putExtra("first_time", false);
            startActivity(in);
        });
    }


    public void deleteUnusedFiles() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.memory_free_anim);
        binding.clearCacheImage.setVisibility(View.VISIBLE);
        binding.clearCacheImage.setAnimation(animation);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> binding.clearCacheImage.setVisibility(View.GONE));
            }
        }, 3000);
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
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            boolean done = deleteDir(dir);
            if (done) {
                Toast.makeText(context, "Cache cleared", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            assert children != null;
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public void Initializer() {
        /* This function fill the arrayList which documents to show in the listView.
         * Here the database is used to fetch all the documents.
         * ListView adapter is used to insert documents in the listView*/
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

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    /* void IDProvider() {
     *//*Here all the ui elements are provides ids.
     * Since the objects are global so they can be provide is anywhere and it will kept there along the execution of the activity*//*
        recyclerView = findViewById(R.id.all_docs_recycler_view);
        clearAnimationImageView = findViewById(R.id.clear_cache_image);
        binding.progressBarAllDocs = findViewById(R.id.progress_bar_all_docs);
        binding.toolBarAllDocs = findViewById(R.id.toolBarAllDocs);
        empty_home_textview = findViewById(R.id.empty_home_tv_all_docs);
        binding.progressBarAllDocs.setVisibility(View.GONE);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_all_doc);
        addNewDocFloatingActionButton = findViewById(R.id.add_doc_button);
        empty_home_textview.setVisibility(View.GONE);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
    }*/

    private void clearCache() {
        binding.progressBarAllDocs.setVisibility(View.VISIBLE);
        deleteUnusedFiles();
        deleteCache(getApplicationContext());
        binding.progressBarAllDocs.setVisibility(View.GONE);
    }

    private void shareApp() {
        //firing intent for app link share to download the app
        Intent in = new Intent(Intent.ACTION_SEND);
        in.setType("text/plain");
        String message = "DocMaker\n\nA light weight app to turn\nimages into pdf files.\n\nLink:-\nhttps://play.google.com/store/apps/details?id=com.oxodiceproductions.docmaker";
        in.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(in, "Share this app with friends and family."));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        if (id == R.id.nav_share_app) {
            shareApp();
        } else if (id == R.id.nav_about_app) {
//            GoToAboutApp();
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(AllDocs.this, MySettings.class));
        }
        return true;
    }

    private class AllDocsRecyclerViewAdapter extends RecyclerView.Adapter<AllDocsRecyclerViewAdapter.MyDocViewHolder> {

        private final String TAG = "tagJi";
        ArrayList<DocumentDataModel> arrayList;
        Context context;
        Activity activity;
        FloatingActionButton createDocumentFAB;

        public AllDocsRecyclerViewAdapter(FloatingActionButton createDocumentFAB, ArrayList<DocumentDataModel> arrayList, Context context, Activity activity) {
            this.arrayList = arrayList;
            this.context = context;
            this.activity = activity;
            this.createDocumentFAB = createDocumentFAB;
        }


        @NonNull
        @Override
        public AllDocsRecyclerViewAdapter.MyDocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.doc_rep, parent, false);
            return new AllDocsRecyclerViewAdapter.MyDocViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AllDocsRecyclerViewAdapter.MyDocViewHolder holder, int position) {
//            holder.binding.toolBarAllDocs.setTitle(arrayList.get(position).getDocName());
            holder.docNameTv.setText(arrayList.get(position).getDocName());
            holder.time_created_tv.setText(context.getString(R.string.time, arrayList.get(position).getTimeCreated()));
            holder.date_created_tv.setText(context.getString(R.string.date, arrayList.get(position).getDateCreated()));
            holder.number_of_pics_tv.setText(context.getString(R.string.pics, arrayList.get(position).getNumberOfPics()));
            holder.indexNumberTextView.setText("" + (position + 1));

            //thumbnail extraction
            MyDatabase myDatabase = null;
            try {
                myDatabase = new MyDatabase(context);
                Cursor cc = myDatabase.LoadImagePaths(arrayList.get(position).getDocId());
                cc.moveToFirst();
                File file = new File(cc.getString(0));
                RequestOptions options = new RequestOptions().fitCenter().sizeMultiplier(0.2f);
                if (file.exists()) {
                    Glide.with(context).applyDefaultRequestOptions(options)
                            .load(file)
                            .into(holder.sample_image);
                }
            } catch (Exception e) {
                RequestOptions options = new RequestOptions().fitCenter().sizeMultiplier(0.2f);
                Glide.with(context).applyDefaultRequestOptions(options)
                        .load(R.drawable.ic_baseline_broken_image_24)
                        .into(holder.sample_image);
            } finally {
                myDatabase.close();
            }


            //click listeners
            holder.optionsButton.setOnClickListener(v -> {
                int visibility = holder.optionsLayout.getVisibility();
                if (visibility == View.GONE) {
                    holder.optionsLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.optionsLayout.setVisibility(View.GONE);
                }
            });

            holder.shareButton.setOnClickListener(v -> {
                String DocId = arrayList.get(holder.getAdapterPosition()).getDocId();
                SharePdfButtonListener(DocId);
            });

            holder.deleteButton.setOnClickListener(v -> {
                String DocId = arrayList.get(holder.getAdapterPosition()).getDocId();
                String DocName = arrayList.get(holder.getAdapterPosition()).getDocName();
                int position1 = holder.getAdapterPosition();
                DeleteDoc(DocId, DocName, position1);
            });

            holder.detailsButton.setOnClickListener(v -> ShowDocDetails(arrayList.get(position)));

        }

        private void ShowDocDetails(DocumentDataModel doc) {
            MyAlertCreator myAlertCreator = new MyAlertCreator();
            //size calculations
            float size = Float.parseFloat(doc.getSize());
            size = size / (1048576f);//1024 * 1024 = 1048576
//        Log.d(TAG, "ShowDocDetails: " + Float.parseFloat(doc.getSize()));
            String text = context.getString(R.string.docDetails, doc.getDocName(), doc.getDateCreated(), doc.getTimeCreated(), doc.getNumberOfPics(), String.format("%.2f MB", size));
            myAlertCreator.showDialog(activity, text);
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        void GotoDocumentView(int i) {
            Intent in = new Intent(context, DocumentViewActivity.class);
            in.putExtra("DocId", arrayList.get(i).getDocId());
            in.putExtra("first_time", false);
            activity.startActivity(in);
        }

        private void SharePdfButtonListener(String DocId) {
            new Thread(() -> {
                MyDatabase myDatabase = null;
                try {
                    ArrayList<String> ImagePaths = new ArrayList<>();
                    myDatabase = new MyDatabase(context);
                    Cursor cc = myDatabase.LoadImagePaths(DocId);
                    try {
                        cc.moveToFirst();
                        do {
                            ImagePaths.add(cc.getString(0));
                        } while (cc.moveToNext());
                    } catch (Exception e) {
                    }
                    if (!ImagePaths.isEmpty()) {
                        PDFMaker pdfMaker = new PDFMaker(context);
                        String filepath = pdfMaker.MakeTempPDF(ImagePaths, myDatabase.getDocName(DocId));
                        if (!filepath.equals("")) {
                            File fileToShare = new File(filepath);
                            Uri contentUri = getUriForFile(context, "com.oxodiceproductions.dockmaker", fileToShare);
                            context.grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/pdf");
                            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            activity.startActivity(Intent.createChooser(shareIntent, "Share with"));
                        }
                    }
                } catch (Exception e) {
                    Log.d("tagJi", "SharePdfButtonListener: " + e.getMessage());
                } finally {
                    myDatabase.close();
                }
            }).start();
        }

        public void DeleteDoc(String DocId, String DocName, int position) {
            //do not put notifyItemRemoved in a thread because it will not work there properly.
            new AlertDialog.Builder(activity).setTitle("Do you want to delete this document")
                    .setMessage(DocName)
                    .setCancelable(true)
                    .setPositiveButton("delete", (dialog, which) -> {

                        CommonOperations.deleteDocument(context, DocId);

                        arrayList.remove(position);
                        notifyItemRemoved(position);
                        dialog.dismiss();
                    }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        class MyDocViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView date_created_tv, time_created_tv, number_of_pics_tv, docNameTv;
            ImageView sample_image;
            TextView indexNumberTextView;
            ImageButton optionsButton, deleteButton, shareButton, detailsButton;
            LinearLayout optionsLayout;

            public MyDocViewHolder(@NonNull View itemView) {
                super(itemView);
                date_created_tv = itemView.findViewById(R.id.textView3);
                time_created_tv = itemView.findViewById(R.id.textView2);
                sample_image = itemView.findViewById(R.id.doc_imageview);
                number_of_pics_tv = itemView.findViewById(R.id.textView5);
                docNameTv = itemView.findViewById(R.id.doc_name_tv);
                optionsButton = itemView.findViewById(R.id.doc_options_button);
                indexNumberTextView = itemView.findViewById(R.id.index_number_text_view);
                deleteButton = itemView.findViewById(R.id.doc_rep_delete);
                shareButton = itemView.findViewById(R.id.doc_rep_share);
                detailsButton = itemView.findViewById(R.id.doc_rep_details);
                optionsLayout = itemView.findViewById(R.id.doc_rep_options_layout);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                TextView indexTv = v.findViewById(R.id.index_number_text_view);
                int i = Integer.parseInt(indexTv.getText().toString()) - 1;
                GotoDocumentView(i);
            }
        }
    }


}