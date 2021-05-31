package com.example.myapplication;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.SliderAdapter;
import com.example.myapplication.ContentApp.AddEventActivity;
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
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import com.example.myapplication.Model.Event;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Author: Le Anh Tuan
 * Modified Date: 17/5/2021
 * Description: Display all events
 */
public class HomeFragment extends Fragment {

    private static final String TBL_USERS = "Users";
    private static final String TBL_EVENTS = "Events";
    private static final String TBL_LIKES = "Likes";
    private static final String TBL_JOINED_EVENTS = "JoinedEvents";
    private final String CHILD_JOINED_DATE = "JoinedDate";

    private boolean alreadyLiked = false;   // already like the event or not

    // get the recycler view
    private RecyclerView rvAllEvents;
    private View view;

    // reference to TBL_EVENTS
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference likesRef;
    private DatabaseReference joinedEventsRef;
    private DatabaseReference userRef;

    public HomeFragment() {
        // required empty constructor
    }

    /**
     * Inflate the corresponding layout.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    /**
     * Display all events.
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(TBL_USERS);
        likesRef = FirebaseDatabase.getInstance().getReference().child(TBL_LIKES);
        eventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_EVENTS);
        joinedEventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_JOINED_EVENTS);

        // handles recycler view initialization
        rvAllEvents = view.findViewById(R.id.rvAllEvents);
        rvAllEvents.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvAllEvents.setLayoutManager(linearLayoutManager);

        displayAllEvents();
    }

    private void displayAllEvents() {
        Query sortEventsInDescendingOrder = eventsRef.orderByChild("createdAt"); // newest first

        FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(sortEventsInDescendingOrder, Event.class)
                .build();

        FirebaseRecyclerAdapter<Event, EventsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Event, EventsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull @NotNull EventsViewHolder holder,
                                                    int position, @NonNull @NotNull Event model) {

                        // get the event ID
                        final String eventKey = getRef(position).getKey();

                        // get user UID
                        String uid = model.getUid();
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(TBL_USERS).child(uid);

                        // get user profile image, full name and bio
                        usersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                String userProfileImage = snapshot.child("userImageUrl").getValue().toString();
                                String userBio = snapshot.child("userBio").getValue().toString();
                                String userFullName = snapshot.child("userFullName").getValue().toString();

                                Picasso.get().load(userProfileImage).into(holder.civUserProfileImage);
                                holder.txtUserFullName.setText(userFullName);
                                holder.txtUserBio.setText(userBio);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                        });

                        // get all event attributes
                        String eventDescription = model.getDescription();
                        String eventStartDate = model.getStart_date();
                        String eventEndDate = model.getEnd_date();
                        String eventName = model.getEvent_name();
                        String eventPlace = model.getPlace();
                        boolean isEventOnline = model.getIsOnline();
                        long eventLimit = model.getLimit();
                        // end getting al event's attributes

                        // display a list of event's images
                        eventsRef.child(eventKey).child("ImgUri_list").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                ArrayList<String> allImagesUri = (ArrayList<String>) snapshot.getValue();
                                holder.sliderView.setSliderAdapter(new SliderAdapter(allImagesUri));
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                        });

                        // event name
                        holder.txtEventName.setText(eventName);

                        // event start and end date
                        String[] splitStartString = eventStartDate.split(" ");
                        String[] splitEndString = eventEndDate.split(" ");

                        String startDate = splitStartString[1] + " " + getResources().getString(R.string.date) + " " + splitStartString[0];
                        String endDate = splitEndString[1] + " "  + getResources().getString(R.string.date) + " " + splitEndString[0];

                        holder.txtEventStartDate.setText(startDate);
                        holder.txtEventEndDate.setText(endDate);

                        // event place
                        holder.txtEventPlace.setText(eventPlace);

                        // set the like button
                        holder.setLikeButtonStatus(eventKey);

                        // if user unlike an event, then delete the corresponding row in Firebase
                        // database
                        holder.ivDropLike.setOnClickListener(v -> {
                            alreadyLiked = true;

                            likesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (alreadyLiked) {
                                        if (snapshot.child(eventKey).hasChild(currentUser.getUid())) {
                                            // if user has already liked this post, then he must be
                                            // unlike it this time, so remove the user who liked it.
                                            likesRef.child(eventKey).child(currentUser.getUid()).removeValue();
                                        } else {
                                            // if user like the post
                                            likesRef.child(eventKey).child(currentUser.getUid()).setValue("liked");
                                        }
                                        alreadyLiked = false;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        });

                        // set the join button
                        holder.setJoinEventButtonStatus(currentUser.getUid(), eventKey);

                        // user click the "join event" button
                        holder.ivJoinEvent.setOnClickListener(v -> {

                            if (holder.isAlreadyJoinedEvent) {
                                // user has already joined that event, if user click
                                // "join event" button again, it means user wants to
                                // cancel that event.

                                cancelJoiningEvent(currentUser.getUid(), eventKey);
                            } else {
                                // let user join the event
                                joinEvent(currentUser.getUid(), eventKey);
                            }
                        });

                        // redirect user to google map if the event is offline, redirect user to
                        // web browser if event is online
                        holder.ivEventLocation.setOnClickListener(v -> {

                            if (isEventOnline) {
                                // TODO: có nên cho sang trình duyệt không?
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getPlace())));
                            } else {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("geo:0,0?q=" + model.getPlace())));
                            }
                        });

                        // user click on an event, redirect to EventDetailActivity
                        holder.itemView.setOnClickListener(v -> {
                            Intent eventDetail = new Intent(getActivity(), EventDetailActivity.class);
                            eventDetail.putExtra("EventId", eventKey);
                            eventDetail.putExtra("EventLimit", eventLimit);
                            eventDetail.putExtra("EventDescription", eventDescription);
                            eventDetail.putExtra("EventEndDate", eventEndDate);
                            eventDetail.putExtra("EventName", eventName);
                            eventDetail.putExtra("EventIsOnline", isEventOnline);
                            eventDetail.putExtra("EventPlace", eventPlace);
                            eventDetail.putExtra("EventStartDate", eventStartDate);
                            eventDetail.putExtra("uid", uid);
                            startActivity(eventDetail);
                        });
                    }

                    @NonNull
                    @Override
                    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_event_layout, parent, false);
                        EventsViewHolder viewHolder = new EventsViewHolder(view);
                        return viewHolder;
                    }
                };
        rvAllEvents.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    protected static class EventsViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView civUserProfileImage;
        private TextView txtUserFullName, txtUserBio, txtEventName, txtEventStartDate, txtEventEndDate, txtEventPlace;
        private ImageView ivDropLike, ivJoinEvent, ivEventLocation;
        private SliderView sliderView;

        private boolean isAlreadyJoinedEvent;

        String currentUserId;
        DatabaseReference localLikesRef;
        DatabaseReference localJoinedEventsRef;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);

            civUserProfileImage = itemView.findViewById(R.id.civUserProfileImage);
            txtUserFullName = itemView.findViewById(R.id.txtUserFullName);
            txtUserBio = itemView.findViewById(R.id.txtUserBio);
            txtEventName = itemView.findViewById(R.id.txtEventName);
            txtEventStartDate = itemView.findViewById(R.id.txtEventStartDate);
            txtEventEndDate = itemView.findViewById(R.id.txtEventEndDate);
            txtEventPlace = itemView.findViewById(R.id.txtEventPlace);
            ivDropLike = itemView.findViewById(R.id.ivDropLike);
            ivJoinEvent = itemView.findViewById(R.id.ivJoinEvent);
            ivEventLocation = itemView.findViewById(R.id.ivEventLocation);
            sliderView = itemView.findViewById(R.id.imageSlider);

            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            localLikesRef = FirebaseDatabase.getInstance().getReference().child(TBL_LIKES);
            localJoinedEventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_JOINED_EVENTS);
        }

        /**
         * If user already like that event, then display the red heart. If not, display the
         * empty heart.
         * @param eventKey the event ID that user click like on
         */
        public void setLikeButtonStatus(final String eventKey) {
            localLikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.child(eventKey).hasChild(currentUserId)) {
                        ivDropLike.setImageResource(R.drawable.like);
                    } else {
                        ivDropLike.setImageResource(R.drawable.dislike);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        /**
         * If user already joined that event, then display the list icon with a tick. If not, display the
         * list icon with plus sign. Also, change the status of the variable `isAlreadyJoinedEvent`
         * @param currentUserId current user id.
         * @param eventId event id.
         */
        public void setJoinEventButtonStatus(final String currentUserId, final String eventId) {
            localJoinedEventsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(eventId)) {
                        ivJoinEvent.setImageResource(R.drawable.ic_joined_event);
                        isAlreadyJoinedEvent = true;
                    } else {
                        ivJoinEvent.setImageResource(R.drawable.join_event_icon);
                        isAlreadyJoinedEvent = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {}
            });
        }
    }

    private void cancelJoiningEvent(String currentUserId, String eventId) {
        // delete at user side
        joinedEventsRef.child(currentUserId).child(eventId).removeValue()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       // delete at event side
                       joinedEventsRef.child(eventId).child(currentUserId).removeValue()
                               .addOnCompleteListener(task1 -> {
                                   if (task1.isSuccessful()) {
                                       removeJoinedEventFromProfile(eventId);
                                       Toast.makeText(getActivity(), "Canceled event successfully", Toast.LENGTH_SHORT).show();
                                   } else {
                                       Toast.makeText(getActivity(), "ERROR: You canceled the event, but event still have you", Toast.LENGTH_LONG).show();
                                   }
                               });
                   } else {
                       Toast.makeText(getActivity(), "ERROR: You cannot cancel joining the event", Toast.LENGTH_LONG).show();
                   }
                });
    }

    /**
     * Let user join the event.
     * @param currentUserId current user id
     * @param eventId joining event id
     */
    private void joinEvent(String currentUserId, String eventId) {
        String dateFormat = getCurrentDate();

        joinedEventsRef.child(currentUserId).child(eventId).child(CHILD_JOINED_DATE)
                .setValue(dateFormat).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                joinedEventsRef.child(eventId).child(currentUserId).child(CHILD_JOINED_DATE)
                        .setValue(dateFormat).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        addJoinedEventToProfile(eventId);
                        Toast.makeText(getActivity(), "Joined event successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "ERROR: You joined the event, but event cannot see you",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getActivity(), "ERROR: You cannot join the event",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * get the current date in the format: yyyy-MM-dd
     * @return the current date in String
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCurrentDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        return sdfDate.format(now);
    }
    private void addJoinedEventToProfile(String eventId){
        userRef.child(currentUser.getUid()).child("JoinedEvents");
        HashMap<String, Object> userEventMap = new HashMap<>();
        userEventMap.put(eventId, "true");
        userRef.child(currentUser.getUid()).child("JoinedEvents").updateChildren(userEventMap);
    }
    private void removeJoinedEventFromProfile(String eventId){
        userRef.child(currentUser.getUid()).child("JoinedEvents").child(eventId).removeValue();
    }
}