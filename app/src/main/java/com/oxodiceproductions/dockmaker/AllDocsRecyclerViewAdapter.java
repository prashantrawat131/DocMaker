package com.oxodiceproductions.dockmaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
        View view= LayoutInflater.from(context).inflate(R.layout.doc_rep,parent,false);
        return new MyDocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyDocViewHolder holder, int position) {
        holder.docName_tv.setText(arrayList.get(position).getDocName());
        holder.date_created_tv.setText("Date: " + arrayList.get(position).getDateCreated());
        holder.time_created_tv.setText("Time: " + arrayList.get(position).getTimeCreated());

        //size calculations
        float size = Float.parseFloat(arrayList.get(position).getSize());
        size = size / (1048576f);//1024 * 1024 = 1048576
        holder.size_tv.setText("Size: " + String.format("%.2f MB", size));

        holder.number_of_pics_tv.setText("Pics: " + arrayList.get(position).getNumberOfPics());
        holder.indexNumberTextView.setText(""+(position+1));

        //thumbnail extraction
        try {
            MyDatabase myDatabase = new MyDatabase(context);
            Cursor cc = myDatabase.LoadImagePaths(arrayList.get(position).getDocId());
            cc.moveToFirst();
            File file = new File(cc.getString(0));
            RequestOptions options = new RequestOptions().fitCenter().sizeMultiplier(0.2f);
            Glide.with(context).applyDefaultRequestOptions(options)
                    .load(file)
                    .into(holder.sample_image);
        } catch (Exception e) {
            RequestOptions options = new RequestOptions().centerCrop().sizeMultiplier(0.2f);
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
            Animation animation= AnimationUtils.loadAnimation(context,R.anim.rotation_left_right_repeat);
            selected_delete_button.startAnimation(animation);
            createDocumentFAB.setVisibility(View.GONE);
        }
    }
/*
    void clearSelected() {
        //It was used when the back button was pressed and checkboxes were selected
        selected = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            arrayList.get(i).setCheck(false);
        }
        this.notifyDataSetChanged();
        selected_delete_button.setVisibility(View.GONE);
        createDocumentFAB.setVisibility(View.VISIBLE);
    }*/

    void GotoDocumentView(int i) {
        Intent in = new Intent(context, document_view.class);
        in.putExtra("DocId", arrayList.get(i).getDocId());
        in.putExtra("first_time", false);
        activity.startActivity(in);
    }

    class MyDocViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView docName_tv, date_created_tv, time_created_tv, size_tv, number_of_pics_tv;
        ImageView sample_image;
        CheckBox checkBox;
        TextView indexNumberTextView;
        public MyDocViewHolder(@NonNull View itemView) {
            super(itemView);
            docName_tv = itemView.findViewById(R.id.textView4);
            date_created_tv = itemView.findViewById(R.id.textView3);
            time_created_tv = itemView.findViewById(R.id.textView2);
            size_tv = itemView.findViewById(R.id.textView);
            sample_image = itemView.findViewById(R.id.doc_imageview);
            number_of_pics_tv = itemView.findViewById(R.id.textView5);
            checkBox = itemView.findViewById(R.id.checkBox);
            indexNumberTextView=itemView.findViewById(R.id.index_number_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TextView indexTv=v.findViewById(R.id.index_number_text_view);
            int i=Integer.parseInt(indexTv.getText().toString())-1;
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
