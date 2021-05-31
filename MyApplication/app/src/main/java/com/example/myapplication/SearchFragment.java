package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.Adapter.UserAdapter;
import com.example.myapplication.Model.Event;
import com.example.myapplication.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Author: Me Duc Thinh
 * Modified date: 23/05/2021
 * Description:
 * 1. Fix code search user by name.
 * 2. Click user item show detail profile
 * 3. Create Follow Table in DATA
 * 4. Get list followers of user to show in recycler view
 * 5. Adjust some XML properties
 *
 */

public class SearchFragment extends Fragment {

    // reference to Events node in Database (Tuan)
    private static final String NODE_EVENTS = "Events";
    private DatabaseReference eventsRef;
    // reference to Users node in Database (Thinh_MD)
    private static final String NODE_USERS = "Users";
    private DatabaseReference usersRef;

    // Define RecyclerView to list user allow search user's full name action
    private RecyclerView recyclerViewUser;

    // search event results recycler view (Tuan)
    private RecyclerView rvSearchEventResults;

    // Define search bar
    private SocialAutoCompleteTextView searchBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // reference to the Users node in Database (Thinh_MD)
        usersRef = FirebaseDatabase.getInstance().getReference().child(NODE_USERS);
        // Bind JAVA to XML
        recyclerViewUser = view.findViewById(R.id.recycler_view_users);
        // Optimize performance when scrolling
        recyclerViewUser.setHasFixedSize(true);
        // Attach layout manager to the RecyclerView
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(getContext()));

        searchBar = view.findViewById(R.id.search_bar);

        // reference to the Events node in Database (Tuan)
        eventsRef = FirebaseDatabase.getInstance().getReference().child(NODE_EVENTS);

        // binding the Recycler View (Tuan)
        rvSearchEventResults = view.findViewById(R.id.rvSearchEventResults);

        // set some properties for search event Recycler (Tuan)
        rvSearchEventResults.setHasFixedSize(true);
        rvSearchEventResults.setLayoutManager(new LinearLayoutManager(getContext()));


        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString()); // Thinh_MD
                searchEvents(s.toString()); // Tuan
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchUser(s.toString()); // Thinh_MD
                searchEvents(s.toString()); // Tuan
            }
        });

        return view;
    }

    /** Modified by Tuan
     * Search event based on event name
     * @param eventName the passed in event name
     */
    private void searchEvents(String eventName) {
        Query searchEventQuery = eventsRef.orderByChild("event_name").startAt(eventName).endAt(eventName + "\uf8ff");

        FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(searchEventQuery, Event.class)
                .build();

        FirebaseRecyclerAdapter<Event, FindEventHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<Event, FindEventHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull SearchFragment.FindEventHolder holder, int position, @NonNull Event model) {

                        final String clickedEventId = getRef(position).getKey();

                        holder.txtSearchEventName.setText(model.getEvent_name());
                        holder.txtSearchEventDescription.setText(model.getDescription());

                        ArrayList<String> allImagesUri = model.getImgUri_list();
                        Picasso.get().load(allImagesUri.get(0)).into(holder.civSearchEventImage);

                        holder.itemView.setOnClickListener(v -> {
                            // changing the activity and send the user ID along with the intent
                            Intent clickPostIntent = new Intent(getContext(), ClickEventActivity.class);
                            clickPostIntent.putExtra("EventKey", clickedEventId);
                            startActivity(clickPostIntent);
                        });

                    }

                    @NonNull
                    @Override
                    public FindEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_search_event_result_layout, parent, false);
                        SearchFragment.FindEventHolder viewHolder = new SearchFragment.FindEventHolder(view);
                        return viewHolder;
                    }
                };
        rvSearchEventResults.setAdapter(firebaseAdapter);
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

    /** Modified by Thinh_MD
     * Search user based on user name
     * @param userName the passed in user name
     */
    private void searchUser(String userName) {
        Query searchUserQuery = usersRef.orderByChild("userFullName").startAt(userName).endAt(userName + "\uf8ff");

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(searchUserQuery, User.class)
                .build();

        FirebaseRecyclerAdapter<User, FindUserHolder> firebaseAdapter =
                new FirebaseRecyclerAdapter<User, FindUserHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull SearchFragment.FindUserHolder holder, int position, @NonNull User model) {

                        final String clickedUserId = getRef(position).getKey();

                        holder.txtSearchUserName.setText(model.getUserFullName());
                        holder.txtSearchUserEmail.setText(model.getUserEmail());

                        String allImagesUri = model.getUserImageUrl();
                        Picasso.get().load(allImagesUri).into(holder.civSearchUserImage);

                        holder.itemView.setOnClickListener(v -> {
                            // changing the activity and send the user ID along with the intent
                            Intent clickPostIntent = new Intent(getContext(), ClickUserAcitivity.class);
                            clickPostIntent.putExtra("UserKey", clickedUserId);
                            startActivity(clickPostIntent);
                        });

                    }

                    @NonNull
                    @Override
                    public FindUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
                        SearchFragment.FindUserHolder viewHolder = new SearchFragment.FindUserHolder(view);
                        return viewHolder;
                    }
                };
        recyclerViewUser.setAdapter(firebaseAdapter);
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