package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class ImgFullscreenActivity extends AppCompatActivity {
    private static final int RESULT_BACK = 2;
    private static final int RESULT_DELETE_IMAGE = 3;
    private ImageView fullScreenImg;
    private Button backBtn;
    private Button deleleDetailImgBtn;
    private int listPosition;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_fullscreen);
        fullScreenImg = findViewById(R.id.imgFullscreen);
        backBtn = findViewById(R.id.BackPostBtn);
        deleleDetailImgBtn = findViewById(R.id.DeleteDetailImg);
        loadImage();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToAddEvent();
            }
        });

        deleleDetailImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDeleteDialog();
                //toDeleleImg();
            }
        });
    }
    // load image from AddEventActivity
    private void loadImage(){
        Intent callingActivityIntent = getIntent();
        listPosition = callingActivityIntent.getIntExtra("listPosition",0);
        if (callingActivityIntent !=null){
            Uri uri = (Uri) callingActivityIntent.getParcelableExtra("imgUri");
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fullScreenImg.setImageBitmap(bitmap);
        }
    }

    private void backToAddEvent(){
        Intent backIntent = new Intent();
        setResult(RESULT_BACK,backIntent);
        finish();
    }
    private void toDeleleImg(){
        Intent deleteIntent = new Intent();
        deleteIntent.putExtra("uriListPosition", listPosition);
        setResult(RESULT_DELETE_IMAGE, deleteIntent);
        finish();
    }


    public void confirmDeleteDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to delete the Image")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        toDeleleImg();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}