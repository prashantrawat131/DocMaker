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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class document_view extends AppCompatActivity {
    ArrayList<String> ImagePaths = new ArrayList<>();
    ArrayList<Boolean> ImagePathsChecker = new ArrayList<>();
    String DocId, DocName;
    RecyclerView recyclerView;
    ArrayList<Uri> galleryImagesUris = new ArrayList<>();
    ArrayList<String> galleryImagesPaths = new ArrayList<>();
    //    boolean first_time = false;
    TextView doc_name_tv;
    LinearLayout docViewOptionsLinearLayout;
    SwipeRefreshLayout swipeRefreshLayout;
    ConstraintLayout mainLayout;
    FrameLayout emptyListFrameLayout;
    FloatingActionButton clickPhotosButton, selectPhotosButton;
    ProgressBar progressBar;
    int SelectPhotosRequestCode = 10;
    ImageButton checkedPhotosDeleteButton, backButton, sharePdfButton, downloadDocButton, deleteDocButton, pdfPreviewButton;

    // Request code for creating a PDF document.
//    private static final int CREATE_FILE = 1;

    private static final int galleryImagesId = 1800;

    public static boolean emptyAvailable = false;

    /*
     * 1)ImagePaths is an arraylist which is used to
     * store the image paths for listView.
     * 2)ImagePathsChecker is an boolean arraylist
     * which stores the checked state of an image
     * and it helps in collective deletion of images in a document.
     * 3)galleryImagesUris is an Uri arrayList which is used to
     * store the uris which are provided by the file manager
     * when user selects them.
     * 4)galleyImagesPaths is an String arrayList which stores the
     * path to the file manager image which were selected by user but
     * now these images are already copied into apps dir and the path which
     * this arrayList stores are the paths to apps dir not to the original files
     * which were selected by the user.This is because the images which are selected by the
     * user are being copied into the app dir as they are going to be copied at some point
     * so why not now.
     * 5)guideViewSharedPreference is a shared preference object which is used to
     * store the data of the guide provided to user initially.
     * 6)emptyListFrameLayout is the layout to show when there are zero images in
     * the document.
     * 7)clickPhotosButton is button to click image to add in the document.
     * 8)selectPhotosButton is use to fire an intent to send user to select photos
     * from file manager to insert in document
     * 9)SelectPhotosRequestCode is the request code when user comes from file manager
     * with selected images.
     * 10)checkedPhotosDeleteButton is used to delete the checked images.
     * 11)CREATE_FILE is a request code to create a new document in iText
     * 12) is used to check whether there are empty images or not
     * 13)emptyAvailable is used for making sure that before pdf preview or saving if there are any empty images or not.
     * Empty images cause error while making pdf file
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_view);

        InitialWork();

//        first_time = getIntent().getExtras().getBoolean("first_time", true);
        DocId = getIntent().getExtras().getString("DocId", "-1");

        if (DocId.equals("-1")) {
            progressBar.setVisibility(View.VISIBLE);
            GoToAllDocs();
        }

        MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        DocName = myDatabase.GetDocumentName(DocId);
        myDatabase.close();
        doc_name_tv.setText(DocName);

        swipeRefreshLayout.setOnRefreshListener(this::Initializer);

        backButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            onBackPressed();
        });

        sharePdfButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
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
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                } catch (Exception e) {
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                }
            }).start();
        });

        downloadDocButton.setOnClickListener(view -> {
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

        deleteDocButton.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(document_view.this);

            final View[] customView = {getLayoutInflater().inflate(R.layout.alert_box, null, false)};
            alertDialogBuilder.setView(customView[0]);

            TextView textView = customView[0].findViewById(R.id.textView9);
            Button cancel_button = customView[0].findViewById(R.id.button);
            Button ok_button = customView[0].findViewById(R.id.button2);
            textView.setText(getResources().getText(R.string.t14));

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            ok_button.setOnClickListener(view1 -> {
                progressBar.setVisibility(View.VISIBLE);
                Runnable runnable = () -> {
                    MyDatabase myDatabase2 = new MyDatabase(getApplicationContext());
                    Cursor cc = myDatabase2.LoadImagePaths(DocId);
                    try {
                        cc.moveToFirst();
                        do {
                            CommonOperations.deleteFile(cc.getString(0));
                        } while (cc.moveToNext());
                    } catch (Exception ignored) {
                    }
                    myDatabase2.DeleteTable(DocId);
                    myDatabase2.close();
                    Intent in = new Intent(document_view.this, AllDocs.class);
                    startActivity(in);
                    finish();
                };
                Thread thread = new Thread(runnable);
                thread.start();
            });
            cancel_button.setOnClickListener(view12 -> alertDialog.dismiss());
            progressBar.setVisibility(View.GONE);
        });

        pdfPreviewButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
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
                    myAlertCreator.createAlertForZeroSizeImages(document_view.this);
                } else {
                    PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
                    String path = pdfMaker.MakeTempPDF(ImagePaths, DocName);
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    if (!path.equals("")) {
                        Uri uri = getUriForFile(getApplicationContext(), "com.oxodiceproductions.dockmaker", new File(path));
                        Intent in = new Intent(Intent.ACTION_VIEW);
                        in.setDataAndType(uri, "application/pdf");
                        in.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(in);
//                        Intent intent=new Intent(document_view.this,PdfPReview.class);
//                        intent.putExtra("path",path);
//                        startActivity(intent);
                    }
                }
            });

            pdfCreationThread.start();
        });

        checkedPhotosDeleteButton.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(document_view.this);

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

        selectPhotosButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent fileManager = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            fileManager.setType("image/*");
            fileManager.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(fileManager, SelectPhotosRequestCode);
        });

        clickPhotosButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent in = new Intent(document_view.this, MyCamera.class);
            in.putExtra("DocId", DocId);
            startActivity(in);
            finish();
        });

        doc_name_tv.setOnClickListener(view -> {
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
                    doc_name_tv.setText(DocName);
                    ChangeName();
                    progressBar.setVisibility(View.GONE);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(document_view.this, "Please fill something", Toast.LENGTH_SHORT).show();
                }
            });

            cancelButton.setOnClickListener(view2 -> alertDialog.dismiss());
        });
    }

    private void GoToAllDocs() {
        Intent in = new Intent(document_view.this, AllDocs.class);
        startActivity(in);
        finish();
    }

    void delete(String ImagePath) {
        MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        myDatabase.DeleteImage(ImagePath, DocId);
        myDatabase.close();
        CommonOperations.deleteFile(ImagePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CREATE_FILE) {
//            if (resultCode == RESULT_OK) {
//                assert data != null;
//                Uri resultUri = data.getData();
//                try {
//                    ParcelFileDescriptor p = getContentResolver().openFileDescriptor(resultUri, "w");
////                    Save(p);
//                    PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
//                    pdfMaker.Save(p, ImagePaths, progressBar);
//                    p.close();
////                    first_time = false;
//                } catch (Exception ignored) {
//                }
//                progressBar.setVisibility(View.GONE);
//            }
//        }
        if (requestCode == SelectPhotosRequestCode) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                progressBar.setVisibility(View.VISIBLE);
                if (data.getClipData() == null) {
//                    this means only single image
                    Uri uri = data.getData();
                    galleryImagesUris.add(uri);
                } else {
//                    this is for multiple paragraphs
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        galleryImagesUris.add(data.getClipData().getItemAt(i).getUri());
                    }
                }
                saveSelectedImage();
            }
        }

        if (requestCode == galleryImagesId) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                String ImagePath = data.getExtras().getString("ImagePath");
                MyDatabase myDatabase=new MyDatabase(getApplicationContext());
                myDatabase.InsertImage(DocId,ImagePath);
                myDatabase.close();

                goForEditing();
            }
        }
    }

    void goForEditing() {
//        if (galleryImagesPaths.size() > 0) {
//            Intent in = new Intent(document_view.this, EditImageActivity.class);
//            in.putExtra("ImagePath", galleryImagesPaths.get(0));
//            in.putExtra("DocId", DocId);
//            in.putExtra("fromGallery", true);
//            in.putExtra("galleryImagesPaths", galleryImagesPaths);
//            startActivity(in);
//        }


        //checking if there are images left to crop
        if (galleryImagesPaths.size() > 0) {
            String imagePathForEditing = galleryImagesPaths.get(0);
            galleryImagesPaths.remove(0);

            Intent in = new Intent(document_view.this, EditingImageActivity.class);
            in.putExtra("ImagePath", imagePathForEditing);
            startActivityForResult(in, galleryImagesId);
        }
    }

    private void saveSelectedImage() {
        progressBar.setVisibility(View.VISIBLE);
        Runnable runnable = () -> {
            for (int i = 0; i < galleryImagesUris.size(); i++) {
//                runOnUiThread(() -> );

                MyImageCompressor myImageCompressor = new MyImageCompressor(getApplicationContext());
                String filePath = myImageCompressor.compress(galleryImagesUris.get(i));

                if (!filePath.equals("-1")) {
                    galleryImagesPaths.add(filePath);
                }
            }
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
        progressBar.setVisibility(View.VISIBLE);
        checkedPhotosDeleteButton.setVisibility(View.GONE);
        docViewOptionsLinearLayout.setVisibility(View.VISIBLE);
        selectPhotosButton.setVisibility(View.VISIBLE);
        clickPhotosButton.setVisibility(View.VISIBLE);
        ImagePathsChecker.clear();
        ImagePaths.clear();

        try (MyDatabase myDatabase = new MyDatabase(getApplicationContext())) {
            Cursor cc = myDatabase.LoadImagePaths(DocId);
            cc.moveToFirst();
            do {
                ImagePaths.add(cc.getString(0));
                ImagePathsChecker.add(false);
            } while (cc.moveToNext());

            //recycler view setup
            DocViewRecyclerViewAdapter adapter = new DocViewRecyclerViewAdapter(docViewOptionsLinearLayout, progressBar, selectPhotosButton, clickPhotosButton, ImagePathsChecker, checkedPhotosDeleteButton, recyclerView, DocId, ImagePaths, getApplicationContext(), document_view.this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);

//            ItemTouchHelper helper = new ItemTouchHelper(new MyTouches(adapter));
//            helper.attachToRecyclerView(recyclerView);

            swipeRefreshLayout.setRefreshing(false);
            recyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            emptyListFrameLayout.setVisibility(View.GONE);
            if (emptyAvailable) {
                Toast.makeText(getApplicationContext(), "Empty images available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            emptyListFrameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
//        first_time = false;
        progressBar.setVisibility(View.GONE);
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
        Intent in = new Intent(document_view.this, AllDocs.class);
        startActivity(in);
        finish();
    }

    void ChangeName() {
        new Thread(() -> {
            MyDatabase myDatabase = new MyDatabase(getApplicationContext());
            myDatabase.SetDocumentName(DocId, DocName);
            myDatabase.close();
        }).start();
    }

    void InitialWork() {
        recyclerView = findViewById(R.id.docRecyclerView);
        clickPhotosButton = findViewById(R.id.floatingActionButton3);
        doc_name_tv = findViewById(R.id.doc_name);
        swipeRefreshLayout = findViewById(R.id.swipe_doc_view);
        progressBar = findViewById(R.id.progressBar3);
        checkedPhotosDeleteButton = findViewById(R.id.deleteSelectedDocumentsButton);
        selectPhotosButton = findViewById(R.id.gallery_select);
        emptyListFrameLayout = findViewById(R.id.empty_document_id);
        emptyListFrameLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        docViewOptionsLinearLayout = findViewById(R.id.doc_view_options);
        mainLayout = findViewById(R.id.document_view_id);

        backButton = findViewById(R.id.imageButton9);
        sharePdfButton = findViewById(R.id.imageButton6);
        pdfPreviewButton = findViewById(R.id.imageButton);
        downloadDocButton = findViewById(R.id.save_doc_imageButton);
        deleteDocButton = findViewById(R.id.imageButton7);
    }
}
