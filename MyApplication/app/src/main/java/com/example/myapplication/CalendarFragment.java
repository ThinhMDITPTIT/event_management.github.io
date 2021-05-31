package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.CalendarEventAdapter;
import com.example.myapplication.Model.Event;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class CalendarFragment extends Fragment {
    private static final String TBL_PROFILE = "Users";
    private static final String PROP_MYEVENT = "userEvents";
    private static final String TBL_EVENTS = "Events";
    private static final String PROP_START_DATE = "start_date";
    private  View view;
    private RecyclerView rcvEvent;
    private CalendarView calendarView;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    //reference to TBl_event
    private DatabaseReference profileRef;
    private DatabaseReference eventsRef;
    private DatabaseReference likesRef;
    private DatabaseReference joinedEventsRef;
    private CalendarEventAdapter eventAdapter;
    private List<String> myEventsID ;
    private List<Event> myEventList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        profileRef = FirebaseDatabase.getInstance().getReference().child(TBL_PROFILE);
        eventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_EVENTS);
        myEventsID = new ArrayList<String>();
        myEventList = new ArrayList<Event>();
        rcvEvent = view.findViewById(R.id.EventRCV);
        calendarView = view.findViewById(R.id.calendarView);

        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(currentDate);
        getEventByDate(strDate);
        builRecyclerView();
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            final Calendar calendar= Calendar.getInstance();
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int yyyy, int MM, int dd) {
                calendar.set(Calendar.YEAR,yyyy);
                calendar.set(Calendar.MONTH,MM);
                calendar.set(Calendar.DAY_OF_MONTH, dd);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String pickedDate = simpleDateFormat.format(calendar.getTime());
                getEventByDate(pickedDate);
            }
        });
        return view;
    }



    protected static class EventsViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSearchEventName, txtSearchEventDescription;
        private CircleImageView civSearchEventImage;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSearchEventName = itemView.findViewById(R.id.txtSearchEventName);
            txtSearchEventDescription = itemView.findViewById(R.id.txtSearchEventDescription);
            civSearchEventImage = itemView.findViewById(R.id.civSearchEventImage);
        }
    }
    private void getEventByDate(String pickedDate){
        eventsRef.orderByChild("uid").equalTo(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myEventList.clear();
                eventAdapter.setData(myEventList);
                myEventsID.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    String lastDay;
                    String beginDay;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String start_dateFromDBStr = (String) postSnapshot.child("start_date").getValue();
                    String end_dateFromDBStr = (String) postSnapshot.child("end_date").getValue();

                    if(pickedDate != ""){

                        beginDay = pickedDate+" 00:00";
                        lastDay = pickedDate+" 23:59";
                        try {
                            Date start_date = simpleDateFormat.parse(start_dateFromDBStr);
                            Date end_date = simpleDateFormat.parse(end_dateFromDBStr);
                            Date beginDayDate = simpleDateFormat.parse(beginDay);
                            Date lastDayDate = simpleDateFormat.parse(lastDay);
                            if(start_date.before(lastDayDate) && end_date.after(beginDayDate) ){
                                Event event = postSnapshot.getValue(Event.class);
                                myEventList.add(event);
                                eventAdapter.setData(myEventList);
                                myEventsID.add(postSnapshot.getKey());
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void builRecyclerView(){
        eventAdapter = new CalendarEventAdapter(getContext());
        rcvEvent.setHasFixedSize(true);
        rcvEvent.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvEvent.setAdapter(eventAdapter);
        eventAdapter.setOnItemClickListener(new CalendarEventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                toDetailEvent(position);
            }
        });
    }
    private void toDetailEvent(int position){
        String clickedEventId = myEventsID.get(position);
        Intent clickPostIntent = new Intent(getContext(), ClickEventActivity.class);
        clickPostIntent.putExtra("EventKey", clickedEventId);
        startActivity(clickPostIntent);
    }

}