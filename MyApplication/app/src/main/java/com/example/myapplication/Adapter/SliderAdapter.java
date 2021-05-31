package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Author: Le Anh Tuan
 * Modified Date: 17/5/2021.
 * Description: Adapter for the Image Slide (lib on Github)
 */
public class SliderAdapter extends
        SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    // a list of images Uri in Firebase realtime database
    private ArrayList<String> mSliderItems;

    /**
     * Constructor for the Adapter
     * @param sliderItems the images Uri from Realtime database.
     */
    public SliderAdapter(ArrayList<String> sliderItems) {
        this.mSliderItems = sliderItems;
    }

    /**
     * Binding to the corresponding layout, which is `each_event_image_layout`.
     * @param parent
     * @return
     */
    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_event_image_layout, null);
        return new SliderAdapterVH(inflate);
    }

    /**
     * Get the position of the image and load the image Uri to that place holder using Picasso.
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        String sliderItem = mSliderItems.get(position);

        Picasso.get().load(sliderItem).into(viewHolder.ivEventImage);
    }

    /**
     * Count how many items are in the Images Uri from Realtime database
     * @return
     */
    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    /**
     * View holder for Slider Adapter
     */
    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView ivEventImage;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            this.itemView = itemView;
        }
    }

}
