package com.example.myapplication.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

//Register
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Author: Me Duc Thinh
 * Modified date: 09/05/2021
 * Description:
 * 1. Add drawable xml for profile/edit_profile/setting screen.
 * 2. Format ID XML and replace in JAVA code
 *
 */

public class SignUpActivity extends AppCompatActivity {
    // Define JAVA
    private EditText userFullName;
    private EditText userEmail;
    private EditText userPassword;
    private EditText userConfirmPassword;
    private Button btnSignUp;
    ProgressDialog progressDialog;
    private Button backToStartBtn;

    // Define Firebase
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Bind JAVA to XML
        userFullName = findViewById(R.id.editTextTextPersonName);
        userEmail = findViewById(R.id.editTextTextEmailAddress2);
        userPassword = findViewById(R.id.editTextTextPassword2);
        userConfirmPassword = findViewById(R.id.editTextTextPassword3);
        btnSignUp = findViewById(R.id.button2);
        backToStartBtn = findViewById(R.id.suBackToStart);
        progressDialog = new ProgressDialog(this);

        // Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // When click SIGN UP button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = userFullName.getText().toString();
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();
                String confirmPassword = userConfirmPassword.getText().toString();
                if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(SignUpActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 8){
                    Toast.makeText(SignUpActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)){
                    Toast.makeText(SignUpActivity.this, "Confirm password wrong", Toast.LENGTH_SHORT).show();
                } else if (!validEmail(email)){
                    Toast.makeText(SignUpActivity.this,"Email is invalid!",Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(fullName, email, password);
                }
            }
        });

        // When click BACK to start button
        backToStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToStart();
            }
        });
    }

    // Validate email
    private boolean validEmail(String userEmail){
        return Patterns.EMAIL_ADDRESS.matcher(userEmail).matches();
    }

    // Handle Register info and init User Data
    private void registerUser(String registerFullName, String registerEmail, String registerPassword){

        progressDialog.setMessage("Please wait");
        progressDialog.show();

        mFirebaseAuth.createUserWithEmailAndPassword(registerEmail, registerPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                HashMap<String, Object> map = new HashMap<>();
                map.put("userFullName", registerFullName);
                map.put("userEmail", registerEmail);
                map.put("userID", mFirebaseAuth.getCurrentUser().getUid());
                map.put("userBio", "");
                map.put("userImageUrl", "default");

                mRootRef.child("Users").child(mFirebaseAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Update the profile for better experience", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to go back to Start view
    public void backToStart(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}