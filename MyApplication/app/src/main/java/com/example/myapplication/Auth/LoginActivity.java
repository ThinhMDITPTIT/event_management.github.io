package com.example.myapplication.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.ContentApp.BottomNavbarActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

// Library confirm login
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    // Define JAVA
    private EditText userEmail;
    private EditText userPassword;
    private Button btnSignIn;
    private TextView registerUser;
    private FirebaseAuth mFirebaseAuth;
    private Button backToStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind JAVA to XML
        userEmail = findViewById(R.id.editTextTextEmailAddress);
        userPassword = findViewById(R.id.editTextTextPassword);
        registerUser = findViewById(R.id.textView3);
        btnSignIn = findViewById(R.id.doLoginBtn);
        backToStartBtn = findViewById(R.id.lBackToStartBtn);

        // Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();

        // When click on do not have account => Register
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        // When click Login
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
                } else if (!validEmail(email)){
                    Toast.makeText(LoginActivity.this,"Wrong Email!",Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(email, password);
                }
            }
        });

        // When click back to start
        backToStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToStart();
            }
        });
    }

    // Validate email
    private boolean validEmail(String validEmail){
        return Patterns.EMAIL_ADDRESS.matcher(validEmail).matches();
    }

    // Handle login info and go to MAIN view
    private void loginUser(String loginEmail, String loginPassword) {
        mFirebaseAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Update the profile for better experience", Toast.LENGTH_SHORT).show();
                    goToHome();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to back to start
    public void backToStart(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Function to go to MAIN view
    public void goToHome(){
        Intent intent = new Intent(this, BottomNavbarActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}