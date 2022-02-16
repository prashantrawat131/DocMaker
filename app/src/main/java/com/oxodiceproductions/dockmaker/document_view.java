package com.oxodiceproductions.dockmaker;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
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
    ImageButton checkedPhotosDeleteButton, backButton, sharePdfButton, saveDocButton, deleteDocButton, pdfPreviewButton;
    ImageButton selectiveDeleteButton;

    // Request code for creating a PDF document.
    private static final int CREATE_FILE = 1;

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
            onBackPressed();
        });

        sharePdfButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            try {
//            String filepath = MakeTempPDF();
                PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
                String filepath = pdfMaker.MakeTempPDF(progressBar, ImagePaths, DocName);
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
            } catch (Exception e) {
            }
            progressBar.setVisibility(View.GONE);
        });

        saveDocButton.setOnClickListener(view -> {
            createFile();
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
                    } catch (Exception e) {
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
            //isse emptyAvailable karna hai baad mein
            if (false) {
                MyAlertCreator myAlertCreator = new MyAlertCreator();
                myAlertCreator.createAlertForZeroSizeImages(document_view.this);
            } else {
                PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
                String path = pdfMaker.MakeTempPDF(progressBar, ImagePaths, DocName);
                if (!path.equals("")) {
                    Uri uri = getUriForFile(getApplicationContext(), "com.oxodiceproductions.dockmaker", new File(path));
                    Intent in = new Intent(Intent.ACTION_VIEW);
                    in.setDataAndType(uri, "application/pdf");
                    in.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(in);
                }
            }
        });

        selectiveDeleteButton.setOnClickListener(view -> {
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
        if (requestCode == CREATE_FILE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Uri resultUri = data.getData();
                try {
                    ParcelFileDescriptor p = getContentResolver().openFileDescriptor(resultUri, "w");
//                    Save(p);
                    PDFMaker pdfMaker = new PDFMaker(getApplicationContext());
                    pdfMaker.Save(p, ImagePaths, progressBar);
                    p.close();
//                    first_time = false;
                } catch (Exception ignored) {
                }
                progressBar.setVisibility(View.GONE);
            }
        }
        if (requestCode == SelectPhotosRequestCode) {
            if (resultCode == RESULT_OK) {
                progressBar.setVisibility(View.VISIBLE);
//                first_time = true;
                assert data != null;
                progressBar.setVisibility(View.VISIBLE);
                if (data.getClipData() == null) {
                    Uri uri = data.getData();
                    galleryImagesUris.add(uri);
                } else {
//                    Log.d("tagJi", "Initial Uri paths starts here");
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
//                        Log.d("tagJi", data.getClipData().getItemAt(i).getUri().getPath());
                        galleryImagesUris.add(data.getClipData().getItemAt(i).getUri());
                    }
//                    Log.d("tagJi", "Initial Uri paths ends here");
                }
                saveSelectedImage();
            }
        }
    }

    void goForEditing() {
        if (galleryImagesPaths.size() > 0) {
            Intent in = new Intent(document_view.this, EditImageActivity.class);
            in.putExtra("ImagePath", galleryImagesPaths.get(0));
            in.putExtra("DocId", DocId);
            in.putExtra("fromGallery", true);
            in.putExtra("galleryImagesPaths", galleryImagesPaths);
            startActivity(in);
        }
    }

    private void saveSelectedImage() {
        Runnable runnable = () -> {
            for (int i = 0; i < galleryImagesUris.size(); i++) {
                runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

                //My methods starts here
                MyImageCompressor myImageCompressor = new MyImageCompressor(getApplicationContext());
                String filePath = myImageCompressor.compress(galleryImagesUris.get(i));
                //My method ends here

                if (!filePath.equals("-1")) {
                    galleryImagesPaths.add(filePath);
                }
            }
            goForEditing();
        };

        Thread t = new Thread(runnable);
        t.start();
    }

    private void createFile() {//Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String name = DocName + ".pdf";
        intent.putExtra(Intent.EXTRA_TITLE, name);
//            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        startActivityForResult(intent, CREATE_FILE);
    }

    void Initializer() {
        progressBar.setVisibility(View.VISIBLE);
        checkedPhotosDeleteButton.setVisibility(View.GONE);
        docViewOptionsLinearLayout.setVisibility(View.VISIBLE);
        selectPhotosButton.setVisibility(View.VISIBLE);
        clickPhotosButton.setVisibility(View.VISIBLE);
        ImagePathsChecker.clear();
        ImagePaths.clear();

        MyDatabase myDatabase = new MyDatabase(getApplicationContext());
        try {
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
        } finally {
            myDatabase.close();
        }
//        first_time = false;
        progressBar.setVisibility(View.GONE);
    }

   /* public class MyTouches extends ItemTouchHelper.SimpleCallback {



//        The real problem is with database
//                kyunki databse bahut slow kar deta hai
//        swipe aur move ke process ko isliye swipe and drag ko abhi nahi kar raha







        DocViewRecyclerViewAdapter adapter;

        public MyTouches(DocViewRecyclerViewAdapter adapter) {
            super(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int p1 = viewHolder.getAdapterPosition();
            int p2 = target.getAdapterPosition();


            Collections.swap(ImagePaths, p1, p2);
            Collections.swap(ImagePathsChecker, p1, p2);

            adapter.notifyItemMoved(p1, p2);

            return true;
        }

        @Override
        public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            MyDatabase myDatabase = new MyDatabase(getApplicationContext());
            myDatabase.updateDoc(ImagePaths.get(fromPos), ImagePaths.get(toPos), DocId);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            lastSwipedImagePath = ImagePaths.get(position);
            lastSwipedImagePosition = position;


            ImagePaths.remove(lastSwipedImagePosition);
            ImagePathsChecker.remove(lastSwipedImagePosition);
            adapter.notifyItemRemoved(lastSwipedImagePosition);

            //alert starts here
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(document_view.this);
            final View[] customView = {getLayoutInflater().inflate(R.layout.alert_box, null)};
            alertDialogBuilder.setView(customView[0]);
            TextView textView = customView[0].findViewById(R.id.textView9);
            Button cancel_button = customView[0].findViewById(R.id.button);
            Button ok_button = customView[0].findViewById(R.id.button2);
            textView.setText("Do you want to delete this image?");

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            ok_button.setOnClickListener(view -> {
                MyDatabase myDatabase = new MyDatabase(getApplicationContext());
                myDatabase.DeleteImage(lastSwipedImagePath, DocId);
                File file = new File(lastSwipedImagePath);
                file.delete();
                alertDialog.dismiss();
            });

            cancel_button.setOnClickListener(view -> {
                progressBar.setVisibility(View.GONE);
                ImagePaths.add(lastSwipedImagePosition, lastSwipedImagePath);
                ImagePathsChecker.add(lastSwipedImagePosition, false);
                adapter.notifyItemInserted(lastSwipedImagePosition);
                alertDialog.dismiss();
            });
        }
    }
*/

    @Override
    protected void onResume() {
        super.onResume();
//        first_time = true;
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
        saveDocButton = findViewById(R.id.save_doc_imageButton);
        deleteDocButton=findViewById(R.id.imageButton7);
        selectiveDeleteButton = findViewById(R.id.deleteSelectedDocumentsButton);

    }
}
