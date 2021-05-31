package com.example.myapplication.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.Event;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.EventViewHolder>{
    private Context mContext;
    private List<Event> mListEvent;
    private OnItemClickListener mClickListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public CalendarEventAdapter(Context mContext){
        this.mContext = mContext;
    }
    public void setData(List<Event> list){
        this.mListEvent = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_calendar_event_layout,parent,false);
        EventViewHolder eventViewHolder = new EventViewHolder(view, mListEvent, mClickListener);
        return eventViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = mListEvent.get(position);
        String ImgUri = event.getImgUri_list().get(0);
        Uri uri = Uri.parse(ImgUri);
        Picasso.get().load(ImgUri).into(holder.imageView);
        holder.eventDescriptionTW.setText(event.getDescription());
        holder.eventDateTW.setText("From: "+event.getStart_date()+" To: "+event.getEnd_date());
        holder.eventNameTW.setText(event.getEvent_name());
    }

    @Override
    public int getItemCount() {
        if(mListEvent == null){
            return 0;
        }else {
            return mListEvent.size();
        }
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView eventNameTW, eventDescriptionTW, eventDateTW;
        public CardView cardCalendarItem;
        public EventViewHolder(@NonNull View itemView, List<Event> mListEvent, final CalendarEventAdapter.OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.civCalendarEventImage);
            eventNameTW = itemView.findViewById(R.id.txtCalendarEventName);
            eventDescriptionTW = itemView.findViewById(R.id.txtCalendarEventDescription);
            eventDateTW = itemView.findViewById(R.id.txtCalendarEventDate);
            cardCalendarItem = itemView.findViewById(R.id.cardCalendarItem);
            cardCalendarItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener !=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
