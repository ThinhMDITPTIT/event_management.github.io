package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

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

public class EditProfileActivity extends AppCompatActivity {
    private ImageView editClose;
    private CircleImageView editUserImageProfile;
    private ImageView editSave;
    private ImageView editChangePhoto;
    private MaterialEditText editUserFullName;
    private MaterialEditText editUserEmail;
    private MaterialEditText editUserBio;

    private FirebaseUser firebaseUser;

    private Uri mImageUri;
    private StorageTask storageTask;
    private StorageReference storageReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editClose = findViewById(R.id.edit_Profile_Close);
        editUserImageProfile = findViewById(R.id.edit_Profile_User_Image);
        editSave = findViewById(R.id.edit_Profile_Save);
        editChangePhoto = findViewById(R.id.edit_Profile_Change_Photo);
        editUserFullName = findViewById(R.id.edit_Profile_User_Full_Name);
        editUserEmail = findViewById(R.id.edit_Profile_User_Email);
        editUserBio = findViewById(R.id.edit_Profile_User_Bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("ImageUploads");

        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                editUserFullName.setText(user.getUserFullName());
                editUserEmail.setText(user.getUserEmail());
                editUserBio.setText(user.getUserBio());
                Picasso.get().load(user.getUserImageUrl()).into(editUserImageProfile);
                if(user.getUserImageUrl().equalsIgnoreCase("default")){
                    editChangePhoto.setVisibility(View.VISIBLE);
                } else {
                    editChangePhoto.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this);
            }
        });

        editUserImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this);
            }
        });

        editSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    private void updateProfile() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userFullName", editUserFullName.getText().toString());
        map.put("userEmail", editUserEmail.getText().toString());
        map.put("userBio", editUserBio.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).updateChildren(map);
        finish();
    }

    private void uploadEditImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (mImageUri != null) {
            final StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpeg");

            storageTask = fileRef.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return  fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();

                        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("userImageUrl").setValue(url);
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            uploadEditImage();
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}
