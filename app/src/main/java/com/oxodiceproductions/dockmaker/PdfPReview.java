package com.oxodiceproductions.dockmaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.graphics.pdf.PdfRenderer.*;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class PdfPReview extends AppCompatActivity {
    private static String TAG = "tagJi";
    String path = "-1";
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);
        recyclerView=findViewById(R.id.pdf_preview_rv);

        path = getIntent().getExtras().getString("path", "-1");

        if (path.equals("-1")) {
            Log.d(TAG, "onCreate: ");
        } else {
            try {
                File pdfFile = new File(path);
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);

                // create a new renderer
                PdfRenderer renderer = new PdfRenderer(fd);

                // let us just render all pages
                final int pageCount = renderer.getPageCount();


                Bitmap[] bitmaps=new Bitmap[pageCount];
                for (int i = 0; i < pageCount; i++) {

                    Page page = renderer.openPage(i);

                    bitmaps[i]=Bitmap.createBitmap(page.getWidth(),page.getHeight(), Bitmap.Config.ARGB_4444);

                    // say we render for showing on the screen
                    page.render(bitmaps[i], null, null, Page.RENDER_MODE_FOR_DISPLAY);

                    // close the page
                    page.close();
                }

                // close the renderer
                renderer.close();

                //setting up recycler view
                PdfPreviewAdapter adapter=new PdfPreviewAdapter(bitmaps,getApplicationContext());
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(adapter);

            } catch (Exception e) {

            }
        }
    }


    public class PdfPreviewAdapter extends RecyclerView.Adapter<PdfPreviewAdapter.MyRecyclerViewHolder>{

        Bitmap[] bitmaps;
        Context context;

        public PdfPreviewAdapter(Bitmap[] bitmaps, Context context) {
            this.bitmaps = bitmaps;
            this.context = context;
        }

        @NonNull
        @Override
        public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(context).inflate(R.layout.pdf_preview_single_image,null,false);
            return new MyRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyRecyclerViewHolder holder, int position) {
            holder.imageView.setImageBitmap(bitmaps[position]);
        }

        @Override
        public int getItemCount() {
            return bitmaps.length;
        }

        public class MyRecyclerViewHolder extends RecyclerView.ViewHolder{
            ImageView imageView;

            public MyRecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView=itemView.findViewById(R.id.imageView5);
            }
        }
    }
}