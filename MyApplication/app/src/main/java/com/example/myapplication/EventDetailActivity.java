package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.SliderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TBL_USERS = "Users";
    private static final String TBL_EVENTS = "Events";
    private static final String TBL_JOINED_EVENTS = "JoinedEvents";

    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference userRef;
    private DatabaseReference eventRef;
    private DatabaseReference joinedEventsRef;

    String eventKey, eventDescription, eventEndDate, eventName, eventPlace, eventStartDate, uid;
    long eventLimit;
    boolean isEventOnline;

    private CircleImageView civEventUserProfileImage;
    private TextView txtEventUserFullName;
    private TextView txtEventUserBio;
    private SliderView imageSliderEvent;
    private ImageView ivEventDetailLocation;
    private TextView txtEventDetailPlace;
    private Button btnEventDetailEditEvent;
    private TextView txtEventDetailName;
    private TextView txtEventDetailStartDate;
    private TextView txtEventDetailEndDate;
    private TextView txtEventDetailDescription;
    private TextView txtEventDetailLimit;
    private TextView txtEventDetailRemaining;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child(TBL_USERS);
        eventRef = FirebaseDatabase.getInstance().getReference().child(TBL_EVENTS);
        joinedEventsRef = FirebaseDatabase.getInstance().getReference().child(TBL_JOINED_EVENTS);

        // get the passed data
        eventKey = getIntent().getStringExtra("EventId");
        eventDescription = getIntent().getStringExtra("EventDescription");
        eventEndDate = getIntent().getStringExtra("EventEndDate");
        eventName = getIntent().getStringExtra("EventName");
        eventPlace = getIntent().getStringExtra("EventPlace");
        eventStartDate = getIntent().getStringExtra("EventStartDate");
        uid = getIntent().getStringExtra("uid");
        eventLimit = getIntent().getLongExtra("EventLimit", 0);
        isEventOnline = getIntent().getBooleanExtra("EventIsOnline", false);

        // binding Java UI to XML
        civEventUserProfileImage = findViewById(R.id.civEventUserProfileImage);
        txtEventUserFullName = findViewById(R.id.txtEventUserFullName);
        txtEventUserBio = findViewById(R.id.txtEventUserBio);
        imageSliderEvent = findViewById(R.id.imageSliderEvent);
        ivEventDetailLocation = findViewById(R.id.ivEventDetailLocation);
        txtEventDetailPlace = findViewById(R.id.txtEventDetailPlace);
        btnEventDetailEditEvent = findViewById(R.id.btnEventDetailEditEvent);
        txtEventDetailName = findViewById(R.id.txtEventDetailName);
        txtEventDetailStartDate = findViewById(R.id.txtEventDetailStartDate);
        txtEventDetailEndDate = findViewById(R.id.txtEventDetailEndDate);
        txtEventDetailDescription = findViewById(R.id.txtEventDetailDescription);
        txtEventDetailLimit = findViewById(R.id.txtEventDetailLimit);
        txtEventDetailRemaining = findViewById(R.id.txtEventDetailRemaining);

        if (!currentUserId.equals(uid)) {
            // if current user is not the one who host the event, then he cannot edit the event
            btnEventDetailEditEvent.setVisibility(View.GONE);
        } else {
            btnEventDetailEditEvent.setVisibility(View.VISIBLE);
        }

        // fetch owner, who published event, information
        fetchOwnerInfo(uid);

        showEventImages();

        ivEventDetailLocation.setOnClickListener(v -> {
            if (isEventOnline) {
                // TODO: có nên cho sang trình duyệt không?
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(eventPlace)));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + eventPlace)));
            }
        });

        txtEventDetailPlace.setText(eventPlace);

        btnEventDetailEditEvent.setOnClickListener(v -> {
            // TODO
            // Anh Tiến xem cái này gọi đến Activity nào
            Toast.makeText(this, "Sửa sự kiện", Toast.LENGTH_SHORT).show();
        });

        txtEventDetailName.setText(eventName);

        // start date, end date
        String[] splitStartString = eventStartDate.split(" ");
        String[] splitEndString = eventEndDate.split(" ");

        String startDate = splitStartString[1] + " " + getResources().getString(R.string.date) + " " + splitStartString[0];
        String endDate = splitEndString[1] + " "  + getResources().getString(R.string.date) + " " + splitEndString[0];

        txtEventDetailStartDate.setText(startDate);
        txtEventDetailEndDate.setText(endDate);
        txtEventDetailDescription.setText(eventDescription);

        if (eventLimit == 0) {
            txtEventDetailLimit.setText("No limit");
        } else {
            txtEventDetailLimit.setText("Limit: " + String.valueOf(eventLimit));
        }

        joinedEventsRef.child(eventKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long participants = snapshot.getChildrenCount();
                    long remaining = eventLimit - participants;
                    txtEventDetailRemaining.setText("Remaining: " + String.valueOf(remaining));
                } else {
                    txtEventDetailRemaining.setText("Remaining: " + String.valueOf(eventLimit));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });



    }

    private void fetchOwnerInfo(String ownerId) {
        userRef.child(ownerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String userProfileImage = snapshot.child("userImageUrl").getValue().toString();
                String userBio = snapshot.child("userBio").getValue().toString();
                String userFullName = snapshot.child("userFullName").getValue().toString();

                Picasso.get().load(userProfileImage).into(civEventUserProfileImage);
                txtEventUserFullName.setText(userFullName);
                txtEventUserBio.setText(userBio);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void showEventImages() {
        eventRef.child(eventKey).child("ImgUri_list").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                ArrayList<String> allImagesUri = (ArrayList<String>) snapshot.getValue();
                imageSliderEvent.setSliderAdapter(new SliderAdapter(allImagesUri));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }
}