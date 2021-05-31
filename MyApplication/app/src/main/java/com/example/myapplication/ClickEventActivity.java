package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.SliderAdapter;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Author: Le Anh Tuan
 * Modified Date: 22/5/2021
 * Description: this class handles data pretty much the same as HomeFragment
 */
public class ClickEventActivity extends AppCompatActivity {

    private static final String TBL_USERS = "Users";
    private static final String TBL_EVENTS = "Events";
    private static final String TBL_LIKES = "Likes";
    private static final String TBL_JOINED_EVENTS = "JoinedEvents";

    private final String CHILD_JOINED_DATE = "JoinedDate";

    private boolean alreadyLiked = false;   // already like the event or not

    private CircleImageView civSearchEventUserProfileImage;
    private TextView txtSearchEventUserFullName, txtSearchEventUserBio, txtSearchEventEventName,
            txtSearchEventEventStartDate, txtSearchEventEventEndDate, txtSearchEventEventPlace;
    private ImageView ivSearchEventDropLike, ivSearchEventJoinEvent, ivSearchEventEventLocation;
    private SliderView imageSliderSearchEvent;

    // reference to TBL_EVENTS
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference likesRef;
    private DatabaseReference joinedEventsRef;

    private String eventId;
    private boolean isAlreadyJoinedEvent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_event);

        eventId = getIntent().getStringExtra("EventKey");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child(TBL_LIKES);
        eventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_EVENTS);
        joinedEventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_JOINED_EVENTS);

        civSearchEventUserProfileImage = findViewById(R.id.civSearchEventUserProfileImage);
        txtSearchEventUserFullName = findViewById(R.id.txtSearchEventUserFullName);
        txtSearchEventUserBio = findViewById(R.id.txtSearchEventUserBio);
        txtSearchEventEventName = findViewById(R.id.txtSearchEventEventName);
        txtSearchEventEventStartDate = findViewById(R.id.txtSearchEventEventStartDate);
        txtSearchEventEventEndDate = findViewById(R.id.txtSearchEventEventEndDate);
        txtSearchEventEventPlace = findViewById(R.id.txtSearchEventEventPlace);
        ivSearchEventDropLike = findViewById(R.id.ivSearchEventDropLike);
        ivSearchEventJoinEvent = findViewById(R.id.ivSearchEventJoinEvent);
        ivSearchEventEventLocation = findViewById(R.id.ivSearchEventEventLocation);
        imageSliderSearchEvent = findViewById(R.id.imageSliderSearchEvent);

        setLikeButton(eventId);

        setJoinEventButton(currentUserId, eventId);

        eventsRef.child(eventId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String eventStartDate = snapshot.child("start_date").getValue().toString();
                    String eventEndDate = snapshot.child("end_date").getValue().toString();
                    String eventName = snapshot.child("event_name").getValue().toString();
                    String eventPlace = snapshot.child("place").getValue().toString();
                    Boolean isEventOnline = (Boolean) snapshot.child("isOnline").getValue();
                    String creatorId = snapshot.child("uid").getValue().toString();
                    ArrayList<String> allImagesUri = (ArrayList<String>) snapshot.child("ImgUri_list").getValue();

                    // set profile image, creator name and his bio
                    setCreatorFields(creatorId);

                    // display event's images
                    imageSliderSearchEvent.setSliderAdapter(new SliderAdapter(allImagesUri));

                    // event name
                    txtSearchEventEventName.setText(eventName);

                    // event start and end date
                    String[] splitStartString = eventStartDate.split(" ");
                    String[] splitEndString = eventEndDate.split(" ");

                    String startDate = splitStartString[1] + " " + getResources().getString(R.string.date) + " " + splitStartString[0];
                    String endDate = splitEndString[1] + " " + getResources().getString(R.string.date) + " " + splitEndString[0];

                    txtSearchEventEventStartDate.setText(startDate);
                    txtSearchEventEventEndDate.setText(endDate);

                    // event place
                    txtSearchEventEventPlace.setText(eventPlace);

                    redirectUserToEventLocation(isEventOnline, eventPlace);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        // if user unlike an event, then delete the corresponding row in Firebase
        // database
        ivSearchEventDropLike.setOnClickListener(v -> {
            alreadyLiked = true;

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (alreadyLiked) {
                        if (snapshot.child(eventId).hasChild(currentUserId)) {
                            // if user has already liked this post, then he must be
                            // unlike it this time, so remove the user who liked it.
                            likesRef.child(eventId).child(currentUserId).removeValue();
                        } else {
                            // if user like the post
                            likesRef.child(eventId).child(currentUserId).setValue("liked");
                        }
                        alreadyLiked = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        // user click the "join event" button
        ivSearchEventJoinEvent.setOnClickListener(v -> {

            if (isAlreadyJoinedEvent) {
                // user has already joined that event, if user click
                // "join event" button again, it means user wants to
                // cancel that event.

                cancelJoiningEvent(currentUserId, eventId);
            } else {
                // let user join the event
                joinEvent(currentUserId, eventId);
            }
        });
    }

    private void setLikeButton(String eventId) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(eventId).hasChild(currentUserId)) {
                    ivSearchEventDropLike.setImageResource(R.drawable.like);
                } else {
                    ivSearchEventDropLike.setImageResource(R.drawable.dislike);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setJoinEventButton(String currentUserId, String eventId) {
        joinedEventsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.hasChild(eventId)) {
                    ivSearchEventJoinEvent.setImageResource(R.drawable.ic_joined_event);
                    isAlreadyJoinedEvent = true;
                } else {
                    ivSearchEventJoinEvent.setImageResource(R.drawable.join_event_icon);
                    isAlreadyJoinedEvent = false;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void setCreatorFields(String creatorId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(TBL_USERS).child(creatorId);

        // get user profile image, full name and bio
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String userProfileImage = snapshot.child("userImageUrl").getValue().toString();
                String userBio = snapshot.child("userBio").getValue().toString();
                String userFullName = snapshot.child("userFullName").getValue().toString();

                Picasso.get().load(userProfileImage).into(civSearchEventUserProfileImage);
                txtSearchEventUserFullName.setText(userFullName);
                txtSearchEventUserBio.setText(userBio);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void redirectUserToEventLocation(boolean isEventOnline, String eventPlace) {
        ivSearchEventEventLocation.setOnClickListener(v -> {

            if (isEventOnline) {
                // TODO: có nên cho sang trình duyệt không?
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(eventPlace)));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + eventPlace)));
            }
        });
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
                                        Toast.makeText(this, "Canceled event successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "ERROR: You canceled the event, but event still have you", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "ERROR: You cannot cancel joining the event", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(this, "Joined event successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "ERROR: You joined the event, but event cannot see you",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "ERROR: You cannot join the event",
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
}