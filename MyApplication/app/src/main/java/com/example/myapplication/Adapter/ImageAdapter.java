package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{

    private ArrayList<String> images;

    /* constructor */
    public ImageAdapter(ArrayList<String> list) {
        this.images = list;
    }

    /* ViewHolder class extending RecyclerView.ViewHolder */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivEventImage;

        /* constructor */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
        }
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_event_image_layout, parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(images.get(position));

        Picasso.get().load(images.get(position)).into(holder.ivEventImage);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
