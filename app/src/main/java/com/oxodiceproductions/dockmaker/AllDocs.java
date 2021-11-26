package com.oxodiceproductions.dockmaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class AllDocs extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    /*This activity show all the documents present in the database*/
    ArrayList<document_model> arrayList = new ArrayList<>();
    RecyclerView recyclerView;
    Toolbar toolbar;
    FloatingActionButton addNewDocFloatingActionButton;
    FrameLayout empty_home_frame_layout;
    private InterstitialAd mInterstitialAd;
    ProgressBar progressBar;
    AllDocsRecyclerViewAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView clearAnimationImageView;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ImageButton deleteSelectedDocumentsButton;

    /* arrayList :- it is a list of all the documents present in the database. It has type of document_model which is a custom java class for documents
     * listView:-It is the listView for all the documents
     * addNewDocFloatingActionButton:-It is used to create a new document and change the activity to that particular document
     * empty_home_frame_layout:-When no documents are present the it will show a empty document sign board
     * mInterstitialAd:-It it the object for ads
     * progressBar:-This is a simple progress bar
     * swipeRefreshLayout:-It is the object of the swipe refresh layout functionality for refreshing the listView
     * deleteSelectedDocumentsButton:-After the user selects all the documents in the listView the it must be able to collectively delete all the items selected.It is visible when any item is selected
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_docs);

        IDProvider();

        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setTitleTextColor(Color.BLACK);

        clearAnimationImageView.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(this::Initializer);

        //Navigation Drawer setup
        navigationView.setNavigationItemSelectedListener(this);

        //all the ads stuff starts here
        MobileAds.initialize(this, initializationStatus -> {
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getResources().getString(R.string.go_to_single_image_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
//                super.onAdShowedFullScreenContent();
                        mInterstitialAd = null;
//                mInterstitialAd is set to be null so that the same ad do not come again because it is already viewed
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                super.onAdFailedToLoad(loadAdError);
                mInterstitialAd = null;
            }
        });

        //ads stuff ends here

        deleteSelectedDocumentsButton.setOnClickListener(view -> AskToDelete());

        Initializer();

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                GoToSettings();
            }
            if (item.getItemId() == R.id.action_share_app) {
                shareApp();
            }
            if (item.getItemId() == R.id.action_clear_cache) {
                clearCache();
            }
            return false;
        });
    }

    public void toolBarClick(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void deleteUnusedFiles() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.memory_free_anim);
        clearAnimationImageView.setVisibility(View.VISIBLE);
        clearAnimationImageView.setAnimation(animation);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> clearAnimationImageView.setVisibility(View.GONE));
            }
        }, 3000);
        try {
            ArrayList<String> usefulImages = new ArrayList<>();
            MyDatabase myDatabase = new MyDatabase(getApplicationContext());
            Cursor cc = myDatabase.LoadDocuments();
            cc.moveToFirst();
            do {
                Cursor ccc = myDatabase.LoadImagePaths(cc.getString(0));
                ccc.moveToFirst();
                do {
                    usefulImages.add(ccc.getString(0));
                    Log.d("tagJi", "" + ccc.getString(0));
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
            progressBar.setVisibility(View.GONE);
            //Toast.makeText(AllDocs.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    void AskToDelete() {
    	/*When user tries to delete a document then it will ask for confirmation.
		It has a custom view for alert message.
		textView:-It shows a the alert message.
		Here the ads functionality is used. When the user clicks the delete button
		then it will show the ads and the documents are getting deleted in background
		 */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AllDocs.this);
        ViewGroup viewGroup = findViewById(R.id.alert_main_layout);
        final View[] customView = {getLayoutInflater().inflate(R.layout.alert_box, viewGroup, true)};
        alertDialogBuilder.setView(customView[0]);
        TextView textView = customView[0].findViewById(R.id.textView9);
        Button cancel_button = customView[0].findViewById(R.id.button);
        Button ok_button = customView[0].findViewById(R.id.button2);
        textView.setText(getText(R.string.allDocsAlertText));
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        ok_button.setOnClickListener(view -> {
            for (document_model doc : arrayList) {
                if (doc.isCheck()) {
                    delete(doc.getDocId());
                }
            }
            if (mInterstitialAd != null) {
                mInterstitialAd.show(AllDocs.this);
            }
            Initializer();
            alertDialog.dismiss();
        });
        cancel_button.setOnClickListener(view -> alertDialog.dismiss());
    }

    void delete(String DocId) {
        /* It is used to delete all the images of a particular document with DocId
         * It deletes the images from the database and also from the storage*/
        MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        Cursor cc = myDatabase.LoadImagePaths(DocId);
        cc.moveToFirst();
        try {
            do {
                File file = new File(cc.getString(0));
                boolean result = file.delete();
                if (!result) {
                    file.deleteOnExit();
                }
            } while (cc.moveToNext());
        } catch (Exception ignored) {
        }
        myDatabase.DeleteTable(DocId);
    }


    void Initializer() {
        /* This function fill the arrayList which documents to show in the listView.
         * Here the database is used to fetch all the documents.
         * ListView adapter is used to insert documents in the listView*/
        progressBar.setVisibility(View.VISIBLE);
        addNewDocFloatingActionButton.setVisibility(View.VISIBLE);
        deleteSelectedDocumentsButton.setVisibility(View.GONE);
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
                arrayList.add(new document_model(false, DocId, SampleImageId, DateCreated, TimeCreated, DocName, Size, NumberOfPics));
            } while (cc.moveToNext());

            Collections.sort(arrayList);

            //adapter setup
            adapter = new AllDocsRecyclerViewAdapter(addNewDocFloatingActionButton, deleteSelectedDocumentsButton, arrayList, getApplicationContext(), this, recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);

            empty_home_frame_layout.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            empty_home_frame_layout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        /*If back button is pressed then it will check whether any document is selected or not.
         * If selected then then user may want to clear the selection and do not want to exit
         * else it is the last activity and the user wants to exit the app.
         * finish Affinity is used to exit the app*/
        if (deleteSelectedDocumentsButton.isShown()) {
            Initializer();
        } else {
            finishAffinity();
        }
    }

    void IDProvider() {
        /*Here all the ui elements are provides ids.
         * Since the objects are global so they can be provide is anywhere and it will kept there along the execution of the activity*/
        recyclerView = findViewById(R.id.all_docs_recycler_view);
        clearAnimationImageView = findViewById(R.id.imageView4);
        progressBar = findViewById(R.id.progressBar2);
        toolbar = findViewById(R.id.toolBarAllDocs);
        deleteSelectedDocumentsButton = findViewById(R.id.selected_delete_button);
        empty_home_frame_layout = findViewById(R.id.empty_home_id);
        deleteSelectedDocumentsButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout = findViewById(R.id.swipe_all_doc);
        addNewDocFloatingActionButton = findViewById(R.id.floatingActionButton2);
        empty_home_frame_layout.setVisibility(View.GONE);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
    }

    public void GoToSettings() {
        Intent in = new Intent(AllDocs.this, MySettings.class);
        startActivity(in);
    }

    private void clearCache() {
        progressBar.setVisibility(View.VISIBLE);
        deleteUnusedFiles();
        deleteCache(getApplicationContext());
        progressBar.setVisibility(View.GONE);
    }

    private void shareApp() {
        //firing intent for app link share to download the app
        Intent in = new Intent(Intent.ACTION_SEND);
        in.setType("text/plain");
        String message = "DocMaker\n\nA light weight app to turn\nimages into pdf files.\n\nLink:-\nhttps://play.google.com/store/apps/details?id=com.oxodiceproductions.docmaker";
        in.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(in, "Share this app with friends and family."));
    }

    public void addNewDocument(View view) {
        progressBar.setVisibility(View.VISIBLE);

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
        Intent in = new Intent(getApplicationContext(), document_view.class);
        in.putExtra("DocId", DocId);
        in.putExtra("first_time", false);
        startActivity(in);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);
        if (id == R.id.all_photos) {
            Toast.makeText(getApplicationContext(), "All photos", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_share_app) {
            shareApp();
        } else if (id == R.id.nav_about_app) {
            Toast.makeText(getApplicationContext(), "About app", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_setting) {
            GoToSettings();
        }
        return true;
    }
}