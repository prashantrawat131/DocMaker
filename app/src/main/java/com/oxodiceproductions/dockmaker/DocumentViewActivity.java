package com.oxodiceproductions.dockmaker;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.oxodiceproductions.dockmaker.Database.AppDatabase;
import com.oxodiceproductions.dockmaker.Database.Document;
import com.oxodiceproductions.dockmaker.Database.DocumentDao;
import com.oxodiceproductions.dockmaker.Database.Image;
import com.oxodiceproductions.dockmaker.Database.ImageDao;
import com.oxodiceproductions.dockmaker.databinding.ActivityDocumentViewBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class DocumentViewActivity extends AppCompatActivity {
    ArrayList<String> ImagePaths = new ArrayList<>();
    ArrayList<Boolean> ImagePathsChecker = new ArrayList<>();

    long DocId;
    String DocName;

    ArrayList<Uri> galleryImagesUris = new ArrayList<>();
    ArrayList<String> galleryImagesPaths = new ArrayList<>();


    public static boolean emptyAvailable = false;

    ActivityDocumentViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InitialWork();

        DocId = getIntent().getExtras().getLong("DocId", -1);

        if (DocId == -1) {
            binding.progressBarDocView.setVisibility(View.VISIBLE);
            GoToAllDocs();
        }

       /* MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        DocName = myDatabase.GetDocumentName(DocId);
        myDatabase.close();*/
        new Thread(() -> {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            DocumentDao documentDao = appDatabase.documentDao();
            ArrayList<Document> documents = (ArrayList<Document>) documentDao.getDocById(DocId);
            DocName = documents.get(0).getName();
            runOnUiThread(() -> binding.docNameTvDocView.setText(DocName));
        }).start();


        binding.swipeRefreshDocView.setOnRefreshListener(this::Initializer);

        binding.backButtonDocView.setOnClickListener(view -> {
            binding.progressBarDocView.setVisibility(View.VISIBLE);
            onBackPressed();
        });

        binding.shareDocButton.setOnClickListener(view -> {
            binding.progressBarDocView.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
                    String filepath = pdfMaker.MakeTempPDF(ImagePaths, DocName);
                    if (!filepath.equals("")) {
                        File fileToShare = new File(filepath);
                        Uri contentUri = getUriForFile(getApplicationContext(), "com.oxodiceproductions.dockmaker", fileToShare);
                        grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("application/pdf");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        startActivity(Intent.createChooser(shareIntent, "Share with"));
                    } else {
                        Toast.makeText(getApplicationContext(), "File path not available", Toast.LENGTH_SHORT).show();
                    }
                    runOnUiThread(() -> binding.progressBarDocView.setVisibility(View.GONE));
                } catch (Exception e) {
                    runOnUiThread(() -> binding.progressBarDocView.setVisibility(View.GONE));
                }
            }).start();
        });

        binding.downloadDocButton.setOnClickListener(view -> {
//            createFile();
            NotificationModule notificationModule = new NotificationModule();
            notificationModule.generateNotification(getApplicationContext(), DocName, "Go to downloads.");
            try {
                File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                FileOutputStream fileOutputStream = new FileOutputStream(downloadsFolder + "/" + DocName + ".pdf");
                PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
                pdfMaker.downloadPdf(ImagePaths, fileOutputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        });


        binding.pdfPreviewDovView.setOnClickListener(view -> {
            binding.progressBarDocView.setVisibility(View.VISIBLE);
            Thread pdfCreationThread = new Thread(() -> {
                boolean emptyAvailable = false;
                try {
                    for (String imagePath : ImagePaths) {
                        File file = new File(imagePath);
                        if (!file.exists()) {
                            emptyAvailable = true;
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
                if (emptyAvailable) {
                    MyAlertCreator myAlertCreator = new MyAlertCreator();
                    myAlertCreator.createAlertForZeroSizeImages(DocumentViewActivity.this);
                } else {
                    PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
                    String path = pdfMaker.MakeTempPDF(ImagePaths, DocName);
                    runOnUiThread(() -> binding.progressBarDocView.setVisibility(View.GONE));
                    if (!path.equals("")) {
                        Uri uri = getUriForFile(getApplicationContext(), "com.oxodiceproductions.dockmaker", new File(path));
                        Intent in = new Intent(Intent.ACTION_VIEW);
                        in.setDataAndType(uri, "application/pdf");
                        in.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(in);
                    }
                }
            });

            pdfCreationThread.start();
        });

        binding.deleteSelectedImagesButton.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DocumentViewActivity.this);

            final View[] customView = {getLayoutInflater().inflate(R.layout.alert_box, findViewById(R.id.alert_main_layout), false)};
            alertDialogBuilder.setView(customView[0]);

            TextView textView = customView[0].findViewById(R.id.textView9);
            Button cancel_button = customView[0].findViewById(R.id.button);
            Button ok_button = customView[0].findViewById(R.id.button2);
            textView.setText(getText(R.string.docViewAlertText));

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            ok_button.setOnClickListener(view2 -> {
                for (int k = 0; k < ImagePaths.size(); k++) {
                    if (ImagePathsChecker.get(k)) {
                        delete(ImagePaths.get(k));
                    }
                }
                Initializer();
                alertDialog.dismiss();
            });

            cancel_button.setOnClickListener(view2 -> alertDialog.dismiss());
        });

        binding.gallerySelect.setOnClickListener(view -> {
            binding.progressBarDocView.setVisibility(View.VISIBLE);
            Intent fileManager = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            fileManager.setType("image/*");
            fileManager.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryImageSelectActivityLauncher.launch(fileManager);
//            startActivityForResult(fileManager, SelectPhotosRequestCode);
        });

        binding.clickNewImageButtonDocView.setOnClickListener(view -> {
            binding.progressBarDocView.setVisibility(View.VISIBLE);
            Intent in = new Intent(DocumentViewActivity.this, MyCamera.class);
            in.putExtra("DocId", DocId);
            startActivity(in);
            finish();
        });

        binding.docNameTvDocView.setOnClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.name_changer_dialog_box, null, false);
            EditText input = dialogView.findViewById(R.id.editTextTextPersonName2);
            Button okButton = dialogView.findViewById(R.id.button4);
            Button cancelButton = dialogView.findViewById(R.id.button3);
            input.setText(DocName);
            alert.setView(dialogView);

            AlertDialog alertDialog = alert.create();
            alertDialog.show();
            input.requestFocus();
            input.selectAll();
            okButton.setOnClickListener(view2 -> {
                if (!input.getText().toString().isEmpty()) {
                    DocName = input.getText().toString();
                    binding.docNameTvDocView.setText(DocName);
                    ChangeName();
                    binding.progressBarDocView.setVisibility(View.GONE);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(DocumentViewActivity.this, "Please fill something", Toast.LENGTH_SHORT).show();
                }
            });

            cancelButton.setOnClickListener(view2 -> alertDialog.dismiss());
        });
    }

    private void GoToAllDocs() {
        Intent in = new Intent(DocumentViewActivity.this, AllDocs.class);
        startActivity(in);
        finish();
    }

    void delete(String ImagePath) {
        new Thread(() -> {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            ImageDao imageDao = appDatabase.imageDao();
            imageDao.deleteImageByPath(ImagePath);
            CommonOperations.deleteFile(ImagePath);
        }).start();
       /* MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        myDatabase.DeleteImage(ImagePath, DocId);
        myDatabase.close();
        CommonOperations.deleteFile(ImagePath);*/
    }


    ActivityResultLauncher<Intent> galleryImageSelectActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    assert result.getResultCode() == RESULT_OK;
                    Intent data = result.getData();
                    assert data != null;
                    binding.progressBarDocView.setVisibility(View.VISIBLE);
                    if (data.getClipData() == null) {
//                    this means only single image
                        Uri uri = data.getData();
                        galleryImagesUris.add(uri);
                    } else {
//                    this is for multiple images
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            galleryImagesUris.add(data.getClipData().getItemAt(i).getUri());
                        }
                    }
                    saveSelectedImage();
                }
            }
    );

    void goForEditing() {

        //checking if there are images left to crop
        if (galleryImagesPaths.size() > 0) {
            String imagePathForEditing = galleryImagesPaths.get(0);
            galleryImagesPaths.remove(0);

            Intent in = new Intent(DocumentViewActivity.this, EditingImageActivity.class);
            in.putExtra("ImagePath", imagePathForEditing);
            editImageActivityLauncher.launch(in);
//            startActivityForResult(in, galleryImagesId);
        }
    }

    ActivityResultLauncher<Intent> editImageActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    assert data != null;
                    String ImagePath = data.getExtras().getString("ImagePath");
                    new Thread(() -> {
                        AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                        ImageDao imageDao = appDatabase.imageDao();
                        Image newImage = new Image(ImagePath, imageDao.getAll().size(), DocId);
                        imageDao.insert(newImage);
                        runOnUiThread(() -> {
                            goForEditing();
                        });
                    });
                    /*
                    MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                    myDatabase.InsertImage(DocId, ImagePath);
                    myDatabase.close();
                    goForEditing();*/
                }
            }
    );

    private void saveSelectedImage() {
        binding.progressBarDocView.setVisibility(View.VISIBLE);
        Runnable runnable = () -> {
            for (int i = 0; i < galleryImagesUris.size(); i++) {

                MyImageCompressor myImageCompressor = new MyImageCompressor(getApplicationContext());
                String filePath = myImageCompressor.compress(galleryImagesUris.get(i));

                if (!filePath.equals("-1")) {
                    galleryImagesPaths.add(filePath);
                }
            }

            galleryImagesUris.clear();
            goForEditing();
        };

        Thread t = new Thread(runnable);
        t.start();
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

    void Initializer() {
        binding.progressBarDocView.setVisibility(View.VISIBLE);
        binding.deleteSelectedImagesButton.setVisibility(View.GONE);
        binding.docViewOptionsLayout.setVisibility(View.VISIBLE);
        binding.gallerySelect.setVisibility(View.VISIBLE);
        binding.clickNewImageButtonDocView.setVisibility(View.VISIBLE);
        ImagePathsChecker.clear();
        ImagePaths.clear();

        try {
            AppDatabase appDatabase=AppDatabase.getInstance(getApplicationContext());
            ImageDao imageDao=appDatabase.imageDao();
            ArrayList<Image> images= (ArrayList<Image>) imageDao.getImagesByDocId(DocId);
//            Cursor cc = myDatabase.LoadImagePaths(DocId);
//            cc.moveToFirst();
            for (Image image:images){
                ImagePaths.add(image.getImagePath());
                ImagePathsChecker.add(false);
            }
            /*do {
                ImagePaths.add(cc.getString(0));
                ImagePathsChecker.add(false);
            } while (cc.moveToNext());*/

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
        }

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
//        first_time = false;
        binding.progressBarDocView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Initializer();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ChangeName();
        Intent in = new Intent(DocumentViewActivity.this, AllDocs.class);
        startActivity(in);
        finish();
    }

    void ChangeName() {
        new Thread(() -> {
            AppDatabase appDatabase=AppDatabase.getInstance(getApplicationContext());
            DocumentDao documentDao=appDatabase.documentDao();
            documentDao.updateName(DocId,DocName);
//            MyDatabase myDatabase = new MyDatabase(getApplicationContext());
//            myDatabase.SetDocumentName(DocId, DocName);
//            myDatabase.close();
        }).start();
    }

    void InitialWork() {
        binding.emptyDocTvDocView.setVisibility(View.GONE);
        binding.progressBarDocView.setVisibility(View.GONE);
    }

    public class DocViewRecyclerViewAdapter extends RecyclerView.Adapter<DocViewRecyclerViewAdapter.MyDocViewHolder> {
        int numberOfCheckedImages = 0;

        @NonNull
        @Override
        public MyDocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.single_image_rep, parent, false);
            return new MyDocViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyDocViewHolder holder, int position) {

            File imageFile = new File(ImagePaths.get(position));
            holder.positionTextView.setText(getString(R.string.docViewImagePosition, position + 1));
            float size = (float) imageFile.length() / (1024 * 1024);

            DocumentViewActivity.emptyAvailable = (size == 0.0f);

            String sizeString = "Size:\n" + String.format(Locale.getDefault(), "%.2f MB", size);
            holder.sizeTextView.setText(sizeString);

            RequestOptions options = new RequestOptions().sizeMultiplier(0.5f);
            options = options.centerCrop();
            Glide.with(getApplicationContext()).applyDefaultRequestOptions(options)
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
           /* Runnable runnable = () -> {
                //database operations must be performed in separate thread
                MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                myDatabase.updateDoc(ImagePaths.get(p1), ImagePaths.get(p2), DocId);
                myDatabase.close();
            };
            Thread thread = new Thread(runnable);
            thread.start();*/

            new Thread(()->{
                AppDatabase appDatabase=AppDatabase.getInstance(getApplicationContext());
                ImageDao imageDao=appDatabase.imageDao();

                Image image1=imageDao.getImageByIndex(DocId,p1);
                Image image2=imageDao.getImageByIndex(DocId,p2);

                image1.setImageIndex(p2);
                image2.setImageIndex(p1);

                imageDao.update(image1);
                imageDao.update(image2);
            }).start();

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
                binding.deleteSelectedImagesButton.setVisibility(View.GONE);
                binding.clickNewImageButtonDocView.setVisibility(View.VISIBLE);
                binding.docViewOptionsLayout.setVisibility(View.VISIBLE);
                binding.gallerySelect.setVisibility(View.VISIBLE);
            } else {
                binding.docViewOptionsLayout.setVisibility(View.GONE);
                binding.deleteSelectedImagesButton.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotation_left_right_repeat);
                binding.deleteSelectedImagesButton.startAnimation(animation);
                binding.clickNewImageButtonDocView.setVisibility(View.GONE);
                binding.gallerySelect.setVisibility(View.GONE);
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
            binding.progressBarDocView.setVisibility(View.VISIBLE);
            Runnable runnable = () -> {
                Intent in = new Intent(getApplicationContext(), SingleImage.class);
                in.putExtra("ImagePath", ImagePaths.get(single_image_position));
                in.putExtra("DocId", DocId);
                startActivity(in);
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }
}
