package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.Adapter.SliderAdapter;
import com.example.myapplication.Model.Event;
import com.example.myapplication.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Author: Me Duc Thinh
 * Modified date: 23/05/2021
 * Description: This class to handle data from previous activity
 * 1. Fix code search user by name.
 * 2. Click user item show detail profile
 * 3. Create Follow Table in DATA
 * 4. Get list followers of user to show in recycler view
 * 5. Show List Event/Joined events of Friend in Friend Profile
 *
 */

public class ClickUserAcitivity extends AppCompatActivity {

    private static final String TBL_JOINED_EVENTS = "JoinedEvents";
    private static final String TBL_EVENTS = "Events";
    private static final String TBL_USERS = "Users";

    // Define JAVA UI
    private RecyclerView recyclerViewAllMyEvents;
    private RecyclerView recyclerViewJoinedEvents;

    private CircleImageView userImageProfile;
    private TextView userFullName;
    private TextView userBio;
    private TextView userEmail;
    private TextView userAllEvents;
    private TextView userJoinedEvents;
    private Button followBtn;

    private DatabaseReference eventsRef;
    private DatabaseReference joinedEventsRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private String userProfileID;
    private String friendProfileID; // Get this ID to preference firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_user_acitivity);

        // get user data from previous activity
        userProfileID = getIntent().getStringExtra("UserKey");
        friendProfileID = getIntent().getStringExtra("FriendID");
        System.out.println("friend ID: "+userProfileID);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child(TBL_USERS);
        eventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_EVENTS);
        joinedEventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_JOINED_EVENTS);

        // Bind JAVA to XML;
        userImageProfile = findViewById(R.id.click_user_image);
        userFullName = findViewById(R.id.click_user_full_name);
        userBio = findViewById(R.id.click_user_bio);
        userEmail = findViewById(R.id.click_user_email);
        userAllEvents = findViewById(R.id.click_all_events);
        userJoinedEvents = findViewById(R.id.click_joined_events);
        followBtn = findViewById(R.id.click_follow_button);

        recyclerViewAllMyEvents = findViewById(R.id.click_recycler_view_all_events);
        recyclerViewJoinedEvents = findViewById(R.id.click_recycler_view_joined_events);

        // Load data user
        userInfo();
        // Check Follow status
        checkFollowingStatus();

        // Button Follow
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnText = followBtn.getText().toString();
                if (btnText.equals("Follow")) {
                    addFollowers(userProfileID, currentUserId);
                    addFollowing(currentUserId, userProfileID);
                    followBtn.setText("Following");
                } else {
                    removeFollowers(userProfileID, currentUserId);
                    removeFollowing(currentUserId, userProfileID);
                    followBtn.setText("Follow");
                }
            }
        });

        userAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewAllMyEvents.setVisibility(View.VISIBLE);
                recyclerViewJoinedEvents.setVisibility(View.GONE);
            }
        });

        userJoinedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewAllMyEvents.setVisibility(View.GONE);
                recyclerViewJoinedEvents.setVisibility(View.VISIBLE);
            }
        });

        recyclerViewAllMyEvents.setHasFixedSize(true);
        recyclerViewAllMyEvents.setLayoutManager(new LinearLayoutManager(ClickUserAcitivity.this));
        displayMyEvents();

        recyclerViewJoinedEvents.setHasFixedSize(true);
        recyclerViewJoinedEvents.setLayoutManager(new LinearLayoutManager(ClickUserAcitivity.this));
        displayJoinedEvents();
    }

    public void userInfo() {
        usersRef.child(userProfileID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    Picasso.get().load(user.getUserImageUrl()).into(userImageProfile);
                    userFullName.setText(user.getUserFullName());
                    userEmail.setText(user.getUserEmail());
                    userBio.setText(user.getUserBio());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void displayMyEvents() {
        FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(eventsRef, Event.class)
                .build();

        FirebaseRecyclerAdapter<Event, ClickUserAcitivity.FindEventHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<Event, ClickUserAcitivity.FindEventHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ClickUserAcitivity.FindEventHolder holder, int position, @NonNull Event model) {

                        final String clickedEventId = getRef(position).getKey();

                        String creatorId = model.getUid();
                        if (creatorId.equals(userProfileID)) {
                            ArrayList<String> allImagesUri = model.getImgUri_list();
                            Picasso.get().load(allImagesUri.get(0)).into(holder.civSearchEventImage);

                            holder.itemView.setVisibility(View.VISIBLE);

                            holder.txtSearchEventName.setText(model.getEvent_name());
                            holder.txtSearchEventDescription.setText(model.getDescription());

                            holder.itemView.setOnClickListener(v -> {
                                // changing the activity and send the user ID along with the intent
                                Intent clickPostIntent = new Intent(ClickUserAcitivity.this, ClickEventActivity.class);
                                clickPostIntent.putExtra("EventKey", clickedEventId);
                                startActivity(clickPostIntent);
                            });
                        } else {
                            holder.itemView.setVisibility(View.GONE);
                            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                            params.height = 0;
                            params.width = 0;
                            holder.itemView.setLayoutParams(params);
                        }
                    }

                    @NonNull
                    @Override
                    public ClickUserAcitivity.FindEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_search_event_result_layout, parent, false);
                        ClickUserAcitivity.FindEventHolder viewHolder = new ClickUserAcitivity.FindEventHolder(view);
                        return viewHolder;
                    }
                };
        recyclerViewAllMyEvents.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
    }

    private void displayJoinedEvents() {
        FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(eventsRef, Event.class)
                .build();

        FirebaseRecyclerAdapter<Event, ClickUserAcitivity.FindEventHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<Event, ClickUserAcitivity.FindEventHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ClickUserAcitivity.FindEventHolder holder, int position, @NonNull Event model) {

                        final String clickedEventId = getRef(position).getKey();


                        joinedEventsRef.child(clickedEventId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (snapshot.hasChild(userProfileID)) {

                                        ArrayList<String> allImagesUri = model.getImgUri_list();
                                        Picasso.get().load(allImagesUri.get(0)).into(holder.civSearchEventImage);

                                        holder.itemView.setVisibility(View.VISIBLE);

                                        holder.txtSearchEventName.setText(model.getEvent_name());
                                        holder.txtSearchEventDescription.setText(model.getDescription());

                                        holder.itemView.setOnClickListener(v -> {
                                            // changing the activity and send the user ID along with the intent
                                            Intent clickPostIntent = new Intent(ClickUserAcitivity.this, ClickEventActivity.class);
                                            clickPostIntent.putExtra("EventKey", clickedEventId);
                                            startActivity(clickPostIntent);
                                        });
                                    } else {
                                        holder.itemView.setVisibility(View.GONE);
                                        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                        params.height = 0;
                                        params.width = 0;
                                        holder.itemView.setLayoutParams(params);
                                    }
                                } else {
                                    holder.itemView.setVisibility(View.GONE);
                                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                    params.height = 0;
                                    params.width = 0;
                                    holder.itemView.setLayoutParams(params);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                        });
                    }

                    @NonNull
                    @Override
                    public ClickUserAcitivity.FindEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_search_event_result_layout, parent, false);
                        ClickUserAcitivity.FindEventHolder viewHolder = new ClickUserAcitivity.FindEventHolder(view);
                        return viewHolder;
                    }
                };
        recyclerViewJoinedEvents.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
    }

    protected static class FindEventHolder extends RecyclerView.ViewHolder {
        private TextView txtSearchEventName, txtSearchEventDescription;
        private CircleImageView civSearchEventImage;

        public FindEventHolder(@NonNull View itemView) {
            super(itemView);

            txtSearchEventName = itemView.findViewById(R.id.txtSearchEventName);
            txtSearchEventDescription = itemView.findViewById(R.id.txtSearchEventDescription);
            civSearchEventImage = itemView.findViewById(R.id.civSearchEventImage);
        }
    }

    private void checkFollowingStatus() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUserId).child("Following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userProfileID).exists()) {
                    followBtn.setText("Following");
                } else {
                    followBtn.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Add friend follower
    private void addFollowers(String friendID, String userID) {
        FirebaseDatabase.getInstance().getReference().child("Users").
                child(friendID).child("Follow").child("Followers").child(userID).setValue(true);
    }

    // Remove friend follower
    private void removeFollowers(String friendID, String userID) {
        FirebaseDatabase.getInstance().getReference().child("Users").
                child(friendID).child("Follow").child("Followers").child(userID).removeValue();
    }

    // Add user following
    private void addFollowing(String userID, String friendID) {
        FirebaseDatabase.getInstance().getReference().child("Users").
                child(userID).child("Follow").child("Following").child(friendID).setValue(true);
    }

    // Remove user following
    private void removeFollowing(String userID, String friendID) {
        FirebaseDatabase.getInstance().getReference().child("Users").
                child(userID).child("Follow").child("Following").child(friendID).removeValue();
    }
}