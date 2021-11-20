package com.oxodiceproductions.dockmaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class DocViewRecyclerViewAdapter extends RecyclerView.Adapter<DocViewRecyclerViewAdapter.MyDocViewHolder> {
    ArrayList<String> ImagePaths;
    ArrayList<Boolean> ImagePathsChecker;
    ImageButton checkedPhotosDeleteButton;
    Context context;
    Activity activity;
    String DocId;
    ImageButton selectImagesButton;
    RecyclerView recyclerView;
    //    boolean firstTime;
    LinearLayout docViewOptionsLinearLayout;
    FloatingActionButton selectPhotosButton, clickPhotosButton;
    int numberOfCheckedImages = 0;
    ProgressBar progressBar;

    public DocViewRecyclerViewAdapter(LinearLayout docViewOptionsLinearLayout, ProgressBar progressBar, FloatingActionButton selectPhotosButton, FloatingActionButton clickPhotosButton, ArrayList<Boolean> ImagePathsChecker, ImageButton checkedPhotosDeleteButton, RecyclerView recyclerView, String DocId, ArrayList<String> ImagePaths, Context context, Activity activity) {
        this.ImagePaths = ImagePaths;
        this.clickPhotosButton = clickPhotosButton;
        this.selectPhotosButton = selectPhotosButton;
        this.DocId = DocId;
        this.ImagePathsChecker = ImagePathsChecker;
        this.checkedPhotosDeleteButton = checkedPhotosDeleteButton;
        this.recyclerView = recyclerView;
        this.context = context;
        this.activity = activity;
        this.progressBar = progressBar;
//        this.firstTime = firstTime;
        this.docViewOptionsLinearLayout = docViewOptionsLinearLayout;
        this.selectImagesButton = selectPhotosButton;
    }

    @NonNull
    @Override
    public MyDocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.simple_image_layout, parent, false);
        return new MyDocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyDocViewHolder holder, int position) {

        File imageFile = new File(ImagePaths.get(position));
        holder.positionTextView.setText(activity.getString(R.string.docViewImagePosition, position + 1));
        float size = (float) imageFile.length() / (1024 * 1024);

        document_view.emptyAvailable = (size == 0.0f);

        String sizeString = "Size:\n" + String.format(Locale.getDefault(), "%.2f MB", size);
        holder.sizeTextView.setText(sizeString);

        RequestOptions options = new RequestOptions().sizeMultiplier(0.5f);
        options = options.centerCrop();
        Glide.with(context).applyDefaultRequestOptions(options)
                .load(imageFile)
                .placeholder(R.drawable.image_placeholder).into(holder.imageView);

        holder.checkBox.setChecked(ImagePathsChecker.get(position));

        holder.checkBox.setOnClickListener(view2 -> {
            CheckBox checkBox = view2.findViewById(R.id.single_image_checkbox);
            if (checkBox.isChecked()) {
                //this means after user clicked the checkbox become checked
                selectImage(position);
            } else {
                //this means after user clicked the checkbox become unchecked
                deselectImage(position);
            }
        });

        upDownButtonControls(position, holder.upButton, holder.downButton);

    }

    @Override
    public int getItemCount() {
        return ImagePaths.size();
    }

    class MyDocViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        CheckBox checkBox;
        TextView sizeTextView, positionTextView;
        ImageButton upButton, downButton;

        public MyDocViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView3);
            checkBox = itemView.findViewById(R.id.single_image_checkbox);
            sizeTextView = itemView.findViewById(R.id.textView6);
            positionTextView = itemView.findViewById(R.id.positionTextView);
            upButton = itemView.findViewById(R.id.up_button);
            downButton = itemView.findViewById(R.id.down_button);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            //this is a temporary method to get position i have to change this
            TextView textView = v.findViewById(R.id.positionTextView);
            int position = Integer.parseInt(textView.getText().toString()) - 1;

            if (numberOfCheckedImages == 0) {
                //this means no image is numberOfCheckedImages and the user wants to preview the image
                GoToSingleImage(position);
            } else {
                CheckBox checkBox = v.findViewById(R.id.single_image_checkbox);
                if (checkBox.isChecked()) {
                    //this means the user wants to remove the selection
                    checkBox.setChecked(false);
                    deselectImage(position);
                } else {
                    //this means the user wants to select the image
                    checkBox.setChecked(true);
                    selectImage(position);
                }
            }
        }
    }

    void Swap(int p1, int p2) {
        Runnable runnable = () -> {
            //database operations must be performed in separate thread
            MyDatabase myDatabase = new MyDatabase(context);
            myDatabase.updateDoc(ImagePaths.get(p1), ImagePaths.get(p2), DocId);
        };
        Thread thread = new Thread(runnable);
        thread.start();

        Collections.swap(ImagePaths, p1, p2);
        Collections.swap(ImagePathsChecker, p1, p2);
        this.notifyDataSetChanged();
    }

    void selectImage(int i) {
        numberOfCheckedImages++;
        ImagePathsChecker.set(i, true);
        lookUp();
    }

    void deselectImage(int i) {
        numberOfCheckedImages--;
        ImagePathsChecker.set(i, false);
        lookUp();
    }

    void lookUp() {
        /*this function looks up whether any image is numberOfCheckedImages
		or not then it will perform the respective tasks
		*/
        if (numberOfCheckedImages == 0) {
            checkedPhotosDeleteButton.setVisibility(View.GONE);
            clickPhotosButton.setVisibility(View.VISIBLE);
            docViewOptionsLinearLayout.setVisibility(View.VISIBLE);
            selectImagesButton.setVisibility(View.VISIBLE);
        } else {
            docViewOptionsLinearLayout.setVisibility(View.GONE);
            checkedPhotosDeleteButton.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotation_left_right_repeat);
            checkedPhotosDeleteButton.startAnimation(animation);
            clickPhotosButton.setVisibility(View.GONE);
            selectImagesButton.setVisibility(View.GONE);
        }
    }

    void upDownButtonControls(int i, ImageButton upButton, ImageButton downButton) {

        //removes up button from first image
        if (i == 0) {
            upButton.setVisibility(View.GONE);
            downButton.setVisibility(View.VISIBLE);
        }

        //removes down button from last image
        if (i == ImagePaths.size() - 1) {
            downButton.setVisibility(View.GONE);
            upButton.setVisibility(View.VISIBLE);
        }

        //setting up click listeners
        upButton.setOnClickListener(view22 -> Swap(i, i - 1));
        downButton.setOnClickListener(view23 -> Swap(i, i + 1));

        upButton.setFocusable(false);//for clicking the recyclerView item
        downButton.setFocusable(false);//for clicking the recyclerView item
    }

    void GoToSingleImage(int single_image_position) {
        progressBar.setVisibility(View.VISIBLE);
        Runnable runnable = () -> {
            Intent in = new Intent(context, SingleImage.class);
            in.putExtra("ImagePath", ImagePaths.get(single_image_position));
            in.putExtra("DocId", DocId);
            activity.startActivity(in);
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }


}
