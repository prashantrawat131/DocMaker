package com.oxodiceproductions.dockmaker;

import static androidx.core.content.FileProvider.getUriForFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class AllDocsRecyclerViewAdapter extends RecyclerView.Adapter<AllDocsRecyclerViewAdapter.MyDocViewHolder> {

    ArrayList<document_model> arrayList;
    Context context;
    Activity activity;
    RecyclerView recyclerView;
    int selected = 0;
    FloatingActionButton createDocumentFAB;
    ImageButton selected_delete_button;

    public AllDocsRecyclerViewAdapter(FloatingActionButton createDocumentFAB, ImageButton selected_delete_button, ArrayList<document_model> arrayList, Context context, Activity activity, RecyclerView recyclerView) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.selected_delete_button = selected_delete_button;
        this.createDocumentFAB = createDocumentFAB;
    }


    @NonNull
    @Override
    public MyDocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doc_rep, parent, false);
        return new MyDocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyDocViewHolder holder, int position) {
        holder.toolbar.setTitle(arrayList.get(position).getDocName());
        holder.date_created_tv.setText("Date: " + arrayList.get(position).getDateCreated());
        holder.time_created_tv.setText("Time: " + arrayList.get(position).getTimeCreated());

        //size calculations
        float size = Float.parseFloat(arrayList.get(position).getSize());
        size = size / (1048576f);//1024 * 1024 = 1048576
        holder.size_tv.setText("Size: " + String.format("%.2f MB", size));

        holder.number_of_pics_tv.setText("Pics: " + arrayList.get(position).getNumberOfPics());
        holder.indexNumberTextView.setText("" + (position + 1));

        //thumbnail extraction
        try {
            MyDatabase myDatabase = new MyDatabase(context);
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
        }

        holder.checkBox.setOnClickListener(view2 -> {
            CheckBox checkBox = view2.findViewById(R.id.checkBox);
            if (checkBox.isChecked()) {
                //this means after user clicked the checkbox become checked
                selectImage(position);
            } else {
                //this means after user clicked the checkbox become unchecked
                deselectImage(position);
            }
        });

        holder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String DocId = arrayList.get(holder.getAdapterPosition()).getDocId();
                String DocName = arrayList.get(holder.getAdapterPosition()).getDocName();
                int position = holder.getAdapterPosition();
                if (item.getItemId() == R.id.overflow_doc_delete) {
                    DeleteDocButtonListener(DocId, DocName, position);
                }
                if (item.getItemId() == R.id.overflow_doc_share) {
                    SharePdfButtonListener(DocId);
                }
                return true;
            }
        });

        //checking if already checked or not, to show it as checked if it was checked
        if (arrayList.get(position).isCheck()) {
            holder.checkBox.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    void selectImage(int i) {
        selected++;
        arrayList.get(i).setCheck(true);
        lookUp();
    }

    void deselectImage(int i) {
        selected--;
        arrayList.get(i).setCheck(false);
        lookUp();
    }

    void lookUp() {
        /*this function looks up whether any image is selected
		or not then it will perform the respective tasks
		*/
        if (selected == 0) {
            selected_delete_button.setVisibility(View.GONE);
            createDocumentFAB.setVisibility(View.VISIBLE);
        } else {
            selected_delete_button.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotation_left_right_repeat);
            selected_delete_button.startAnimation(animation);
            createDocumentFAB.setVisibility(View.GONE);
        }
    }

    void GotoDocumentView(int i) {
        Intent in = new Intent(context, document_view.class);
        in.putExtra("DocId", arrayList.get(i).getDocId());
        in.putExtra("first_time", false);
        activity.startActivity(in);
    }

    private void SharePdfButtonListener(String DocId) {
        new Thread(() -> {
            try {
                ArrayList<String> ImagePaths = new ArrayList<>();
                MyDatabase myDatabase = new MyDatabase(context);
                Cursor cc = myDatabase.LoadImagePaths(DocId);
                cc.moveToFirst();
                do {
                    ImagePaths.add(cc.getString(0));
                } while (cc.moveToNext());
                if (!ImagePaths.isEmpty()) {
                    PDFMaker pdfMaker = new PDFMaker(context);
                    String filepath = pdfMaker.MakeTempPDF(null, ImagePaths);
                    if (!filepath.equals("")) {
                        File fileToShare = new File(filepath);
                        Uri contentUri = getUriForFile(context, "com.oxodiceproductions.dockmaker", fileToShare);
                        context.grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("application/pdf");
                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        activity.startActivity(Intent.createChooser(shareIntent, "Share with"));
                    } else {
                        Toast.makeText(context, "File path not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Empty document", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d("tagJi", "SharePdfButtonListener: " + e.getMessage());
            }
        }).start();
    }

    public void DeleteDocButtonListener(String DocId, String DocName, int position) {
        new AlertDialog.Builder(activity).setTitle("Do you want to delete this document")
                .setMessage(DocName)
                .setCancelable(true)
                .setPositiveButton("delete", (dialog, which) -> {
                    Runnable runnable = () -> {
                        MyDatabase myDatabase = new MyDatabase(context);
                        Cursor cc = myDatabase.LoadImagePaths(DocId);
                        cc.moveToFirst();
                        try {
                            do {
                                CommonOperations.deleteFile(cc.getString(0));
                            } while (cc.moveToNext());
                        } catch (Exception e) {
                        }
                        myDatabase.DeleteTable(DocId);
                        arrayList.remove(position);
                        notifyAll();
                        dialog.dismiss();
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    class MyDocViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView date_created_tv, time_created_tv, size_tv, number_of_pics_tv;
        ImageView sample_image;
        CheckBox checkBox;
        TextView indexNumberTextView;
        Toolbar toolbar;

        public MyDocViewHolder(@NonNull View itemView) {
            super(itemView);
            date_created_tv = itemView.findViewById(R.id.textView3);
            time_created_tv = itemView.findViewById(R.id.textView2);
            size_tv = itemView.findViewById(R.id.textView);
            sample_image = itemView.findViewById(R.id.doc_imageview);
            number_of_pics_tv = itemView.findViewById(R.id.textView5);
            checkBox = itemView.findViewById(R.id.checkBox);
            indexNumberTextView = itemView.findViewById(R.id.index_number_text_view);
            toolbar = itemView.findViewById(R.id.doc_toolbar);
            toolbar.inflateMenu(R.menu.doc_overflow_menu);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TextView indexTv = v.findViewById(R.id.index_number_text_view);
            int i = Integer.parseInt(indexTv.getText().toString()) - 1;
            if (selected == 0) {
                //this means no document is selected and the user wants to preview the document
                GotoDocumentView(i);
            } else {
                CheckBox checkBox = v.findViewById(R.id.checkBox);
                if (checkBox.isChecked()) {
                    //this means the user wants to remove the selection
                    checkBox.setChecked(false);
                    deselectImage(i);
                } else {
                    //this means the user wants to select the image
                    checkBox.setChecked(true);
                    selectImage(i);
                }
            }
        }
    }
}
