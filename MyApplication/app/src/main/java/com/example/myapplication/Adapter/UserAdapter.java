package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ContentApp.BottomNavbarActivity;
import com.example.myapplication.Model.User;
import com.example.myapplication.ProfileFragment;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//Library show image
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

//Library show image in circle
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Author: Me Duc Thinh
 * Modified date: 08/05/2021
 * Description:
 * 1. Create package: Adapter -> UserAdapter.
 * 2. Create package: Model -> User
 * 3. Add action see profile & edit profile to two class: ProfileFragment & EditProfileActivity
 * 4. Add action search user and see friend profile to class: SearchFragment
 * 5. Design the XML: activity_edit_profile, fragment_profile, fragment_search.
 * 6. Add some activity & user_permission to AndroidManifest.xml
 * 7. Add some dependencies to app build.gradle
 */

public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isFragment) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item , parent , false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);
        holder.btnFollow.setVisibility(View.VISIBLE);

        holder.userFullName.setText(user.getUserFullName());
        holder.userEmail.setText(user.getUserEmail());

        Picasso.get().load(user.getUserImageUrl()).placeholder(R.mipmap.ic_launcher).into(holder.userImageProfile);

        isFollowed(user.getUserID() , holder.btnFollow);

        if (user.getUserID().equals(firebaseUser.getUid())){
            holder.btnFollow.setVisibility(View.GONE);
        }

        // Check follow status to update Database
        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btnFollow.getText().toString().equals(("follow"))){
                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child((firebaseUser.getUid())).child("following").child(user.getUserID()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child(user.getUserID()).child("followers").child(firebaseUser.getUid()).setValue(true);
                    // Notify when user have follow
                  addNotification(user.getUserID());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child((firebaseUser.getUid())).child("following").child(user.getUserID()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child(user.getUserID()).child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        // Set context userProfileID to show friend profile when click on each elements in user search list
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFragment) {
                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("userProfileID", user.getUserID()).apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ProfileFragment()).commit();
                } else {
                    Intent intent = new Intent(mContext, BottomNavbarActivity.class);
                    intent.putExtra("publisherID", user.getUserID());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    // Check follow status to change button follow text
    private void isFollowed(final String id, final Button btnFollow) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(id).exists())
                    btnFollow.setText("following");
                else
                    btnFollow.setText("follow");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    // Define ViewHolder of each element to use in RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView userImageProfile;
        public TextView userFullName;
        public TextView userEmail;
        public Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImageProfile = itemView.findViewById(R.id.image_profile);
            userFullName = itemView.findViewById(R.id.full_name_profile);
            userEmail = itemView.findViewById(R.id.user_email_profile);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }

    // Add Notification to Notify fragment and database
    private void addNotification(String userId) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("userID", userId);
        map.put("textNotifications", "started following you.");
        map.put("eventID", "");
        map.put("isEvent", false);

        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid()).push().setValue(map);
    }
}
