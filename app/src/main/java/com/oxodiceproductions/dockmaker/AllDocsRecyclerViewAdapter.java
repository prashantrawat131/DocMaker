package com.oxodiceproductions.dockmaker;

import static androidx.core.content.FileProvider.getUriForFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
    FloatingActionButton createDocumentFAB;

    public AllDocsRecyclerViewAdapter(FloatingActionButton createDocumentFAB, ArrayList<document_model> arrayList, Context context, Activity activity, RecyclerView recyclerView) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity = activity;
        this.recyclerView = recyclerView;
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
        holder.date_created_tv.setText(context.getString(R.string.date,arrayList.get(position).getDateCreated()));
        holder.time_created_tv.setText(context.getString(R.string.time,arrayList.get(position).getTimeCreated()));
        holder.number_of_pics_tv.setText(context.getString(R.string.pics,arrayList.get(position).getNumberOfPics()));
        holder.indexNumberTextView.setText("" + (position + 1));

        //thumbnail extraction
        MyDatabase myDatabase=null;
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
        }
        finally {
            myDatabase.close();
        }

        holder.toolbar.setOnMenuItemClickListener(item -> {
            String DocId = arrayList.get(holder.getAdapterPosition()).getDocId();
            String DocName = arrayList.get(holder.getAdapterPosition()).getDocName();
            int position1 = holder.getAdapterPosition();
            if (item.getItemId() == R.id.overflow_doc_delete) {
                DeleteDoc(DocId, DocName, position1);
            }
            else if (item.getItemId() == R.id.overflow_doc_share) {
                SharePdfButtonListener(DocId);
            }
            else if(item.getItemId()==R.id.overflow_doc_details){
                ShowDocDetails(arrayList.get(position));
            }
            return true;
        });
    }

    private void ShowDocDetails(document_model doc){
        MyAlertCreator myAlertCreator=new MyAlertCreator();
        //size calculations
        float size = Float.parseFloat(doc.getSize());
        size = size / (1048576f);//1024 * 1024 = 1048576
        String text="Size: " + String.format("%.2f MB", size);
        myAlertCreator.showDialog(activity,text);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    void GotoDocumentView(int i) {
        Intent in = new Intent(context, document_view.class);
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
                    MyDatabase myDatabase = new MyDatabase(context);
                    Cursor cc = myDatabase.LoadImagePaths(DocId);
                    try {
                        cc.moveToFirst();
                        do {
                            CommonOperations.deleteFile(cc.getString(0));
                        } while (cc.moveToNext());
                    } catch (Exception e) { }

                    try{
                        myDatabase.DeleteTable(DocId);
                    }catch (Exception e){ }
                    finally {
                        myDatabase.close();
                    }

                    arrayList.remove(position);
                    notifyItemRemoved(position);
                    dialog.dismiss();
                }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    class MyDocViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView date_created_tv,size_tv, time_created_tv, number_of_pics_tv;
        ImageView sample_image;
        TextView indexNumberTextView;
        Toolbar toolbar;

        public MyDocViewHolder(@NonNull View itemView) {
            super(itemView);
            date_created_tv = itemView.findViewById(R.id.textView3);
            time_created_tv = itemView.findViewById(R.id.textView2);
            sample_image = itemView.findViewById(R.id.doc_imageview);
            number_of_pics_tv = itemView.findViewById(R.id.textView5);
            size_tv.
            indexNumberTextView = itemView.findViewById(R.id.index_number_text_view);
            toolbar = itemView.findViewById(R.id.doc_toolbar);
            toolbar.inflateMenu(R.menu.doc_overflow_menu);
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
