package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Author: Me Duc Thinh
 * Modified date: 23/05/2021
 * Description:
 * 1. Fix code search user by name.
 * 2. Click user item show detail profile
 * 3. Create Follow Table in DATA
 * 4. Get list followers of user to show in recycler view
 *
 */

public class ProfileFragment extends Fragment {

    private static final String TBL_JOINED_EVENTS = "JoinedEvents";
    private static final String TBL_EVENTS = "Events";
    private static final String TBL_USERS = "Users";
    private static final String TBL_FOLLOW = "Follow";

    // Define JAVA UI
    private RecyclerView rvAllMyEvents; // tuan
    private RecyclerView rvAllJoinedEvents; // tuan
    private RecyclerView recyclerViewFollowers; // Thinh_MD

    private ImageView optionToolBar;
    private CircleImageView userImageProfile;
    private TextView userFullName;
    private TextView userBio;
    private TextView userEmail;

    private TextView userAllEvents;
    private TextView userYourEvents;
    private TextView userFollowers;

    private Button editProfile;

    private FirebaseUser firebaseUser;
    private String userProfileID;

    private DatabaseReference eventsRef;
    private DatabaseReference joinedEventsRef;
    private DatabaseReference usersRef;
    private DatabaseReference followRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userProfileID = firebaseUser.getUid();

        eventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_EVENTS);
        joinedEventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_JOINED_EVENTS);
        usersRef = FirebaseDatabase.getInstance().getReference().child(TBL_USERS);
        followRef = FirebaseDatabase.getInstance().getReference().child(TBL_USERS).child(userProfileID).child(TBL_FOLLOW).child("Followers");

        // Bind Java to XML
        optionToolBar = view.findViewById(R.id.profile_Options);
        userImageProfile = view.findViewById(R.id.profile_User_Image);
        userFullName = view.findViewById(R.id.profile_User_Full_Name);
        userBio = view.findViewById(R.id.profile_User_Bio);
        userEmail = view.findViewById(R.id.profile_User_Email);
        userAllEvents = view.findViewById(R.id.profile_All_Events);
        userYourEvents = view.findViewById(R.id.profile_Your_Events);
        userFollowers = view.findViewById(R.id.profile_Followers);
        editProfile = view.findViewById(R.id.profile_Edit_Button);

        rvAllMyEvents = view.findViewById(R.id.rvAllMyEvents);
        rvAllJoinedEvents = view.findViewById(R.id.rvAllJoinedEvents);
        recyclerViewFollowers = view.findViewById(R.id.profile_Recycler_View_Followers); //Thinh_MD

        userInfo();
        //Thinh_MD
        recyclerViewFollowers.setHasFixedSize(true);
        recyclerViewFollowers.setLayoutManager(new LinearLayoutManager(getContext()));
        displayFollowers();
        // end Thinh_MD

        optionToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), OptionActivity.class));
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });

        // Tuan
        rvAllMyEvents.setHasFixedSize(true);
        rvAllMyEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        displayAllMyEvents();

        rvAllJoinedEvents.setHasFixedSize(true);
        rvAllJoinedEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        displayAllMyJoinedEvents();
        // end Tuan

        rvAllMyEvents.setVisibility(View.VISIBLE);
        rvAllJoinedEvents.setVisibility(View.GONE);
        recyclerViewFollowers.setVisibility(View.GONE);

        userAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvAllMyEvents.setVisibility(View.VISIBLE);
                rvAllJoinedEvents.setVisibility(View.GONE);
                recyclerViewFollowers.setVisibility(View.GONE);
            }
        });

        userYourEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvAllMyEvents.setVisibility(View.GONE);
                rvAllJoinedEvents.setVisibility(View.VISIBLE);
                recyclerViewFollowers.setVisibility(View.GONE);
            }
        });

        userFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvAllMyEvents.setVisibility(View.GONE);
                rvAllJoinedEvents.setVisibility(View.GONE);
                recyclerViewFollowers.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void userInfo() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userProfileID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Picasso.get().load(user.getUserImageUrl()).into(userImageProfile);
                userFullName.setText(user.getUserFullName());
                userEmail.setText(user.getUserEmail());
                userBio.setText(user.getUserBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Tuan
     */
    private void displayAllMyEvents() {
        FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(eventsRef, Event.class)
                .build();

        FirebaseRecyclerAdapter<Event, ProfileFragment.FindEventHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<Event, ProfileFragment.FindEventHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProfileFragment.FindEventHolder holder, int position, @NonNull Event model) {

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
                                Intent clickPostIntent = new Intent(getContext(), ClickEventActivity.class);
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
                    public ProfileFragment.FindEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_search_event_result_layout, parent, false);
                        ProfileFragment.FindEventHolder viewHolder = new ProfileFragment.FindEventHolder(view);
                        return viewHolder;
                    }
                };
        rvAllMyEvents.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
    }

    /**
     * Tuan
     */
    private void displayAllMyJoinedEvents() {
        FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(eventsRef, Event.class)
                .build();

        FirebaseRecyclerAdapter<Event, ProfileFragment.FindEventHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<Event, ProfileFragment.FindEventHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProfileFragment.FindEventHolder holder, int position, @NonNull Event model) {

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
                                            Intent clickPostIntent = new Intent(getContext(), ClickEventActivity.class);
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
                    public ProfileFragment.FindEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_search_event_result_layout, parent, false);
                        ProfileFragment.FindEventHolder viewHolder = new ProfileFragment.FindEventHolder(view);
                        return viewHolder;
                    }
                };
        rvAllJoinedEvents.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
    }

    /**
     * Modified by Tuan
     */
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

    /**
     * Thinh_MD
     */
    private void displayFollowers() {
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(usersRef, User.class)
                .build();

        FirebaseRecyclerAdapter<User, ProfileFragment.FindUserHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<User, FindUserHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProfileFragment.FindUserHolder holder, int position, @NonNull User model) {

                        final String clickedUserId = getRef(position).getKey();

                        followRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (snapshot.hasChild(model.getUserID())) {
                                        holder.txtSearchUserName.setText(model.getUserFullName());
                                        holder.txtSearchUserEmail.setText(model.getUserEmail());

                                        String allImagesUri = model.getUserImageUrl();
                                        Picasso.get().load(allImagesUri).into(holder.civSearchUserImage);

                                        holder.itemView.setOnClickListener(v -> {
                                            // changing the activity and send the user ID along with the intent
                                            Intent clickPostIntent = new Intent(getContext(), ClickUserAcitivity.class);
                                            clickPostIntent.putExtra("UserKey", clickedUserId);
                                            clickPostIntent.putExtra("FriendID", model.getUserID());
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
                    public ProfileFragment.FindUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
                        ProfileFragment.FindUserHolder viewHolder = new ProfileFragment.FindUserHolder(view);
                        return viewHolder;
                    }
                };
        recyclerViewFollowers.setAdapter(firebaseAdapter);
        firebaseAdapter.startListening();
    }

    /**
     * Modified by Thinh_MD
     */
    protected static class FindUserHolder extends RecyclerView.ViewHolder {
        private TextView txtSearchUserName, txtSearchUserEmail;
        private CircleImageView civSearchUserImage;

        public FindUserHolder(@NonNull View itemView) {
            super(itemView);

            txtSearchUserName = itemView.findViewById(R.id.full_name_profile);
            txtSearchUserEmail = itemView.findViewById(R.id.user_email_profile);
            civSearchUserImage = itemView.findViewById(R.id.image_profile);
        }
    }

}