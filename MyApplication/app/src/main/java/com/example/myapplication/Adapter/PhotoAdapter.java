package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ContentApp.AddEventActivity;
import com.example.myapplication.ImgFullscreenActivity;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.IOException;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>{
    private Context mContext;
    private List<Uri> mListPhotos;
    private Button deletedButton;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public void setOnDeleteClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public PhotoAdapter(Context mContext) {
        this.mContext =mContext;
    }
    public void setData(List<Uri> list){
        this.mListPhotos = list;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo,parent,false);
        PhotoViewHolder photoViewHolder = new PhotoViewHolder(view, mListener);
        return photoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri uri = mListPhotos.get(position);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),uri);
            holder.imgViewPhoto.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if(mListPhotos == null){
            return 0;
        }else {
            return mListPhotos.size();
        }
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgViewPhoto;
        public PhotoViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imgViewPhoto = itemView.findViewById(R.id.img_photo);
            deletedButton = itemView.findViewById(R.id.DeleteItemBtn);
            imgViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            deletedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }
}
