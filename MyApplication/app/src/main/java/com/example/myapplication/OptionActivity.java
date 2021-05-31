package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Author: Me Duc Thinh
 * Modified date: 09/05/2021
 * Description:
 * 1. Add drawable xml for profile/edit_profile/setting screen.
 * 2. Format ID XML and replace in JAVA code
 * 3. Rework with Google Sign in method
 *
 */

public class OptionActivity extends AppCompatActivity {
    // Define JAVA
    private ImageView btnLogout;
    private Toolbar btnOption;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        // Bind JAVA to XML
        btnLogout = findViewById(R.id.logout);
        btnOption = findViewById(R.id.optionToolbar);

        // Prepare for google sign out method
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Get last google account sign in to logout
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        // Setting action bar
        setSupportActionBar(btnOption);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // When click back icon on action bar
        btnOption.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // When click SIGN OUT option
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(account != null){
                    signOutGoogle();
                } else {
                    FirebaseAuth.getInstance().signOut();
                    // log out facebook
                    LoginManager.getInstance().logOut();
                    goToHome();
                }
            }
        });
    }

    // Function to sign out google account
    private void signOutGoogle(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                goToHome();
            }
        });
    }

    // Function to go to MAIN view
    private void goToHome(){
        startActivity(new Intent(OptionActivity.this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}