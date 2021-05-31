package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.Auth.LoginActivity;
import com.example.myapplication.Auth.SignUpActivity;
import com.example.myapplication.ContentApp.BottomNavbarActivity;

//google
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

//firebase
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//facebook
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

//java
import java.util.HashMap;

/**
 * Author: Me Duc Thinh
 * Modified date: 10/05/2021
 * Description:
 * 1. Add drawable xml for profile/edit_profile/setting screen.
 * 2. Format ID XML and replace in JAVA code
 * 3. Rework with Google Sign in method: Done
 * 4. Rework with Facebook Sign in method: Done
 *
 */

public class MainActivity extends AppCompatActivity {
    // Define JAVA login/signUp
    private Button toLoginBtn;
    private Button toSignUpBtn;

    // Define JAVA Facebook sign in
    private LoginButton facebookSignInButton;
    private CallbackManager mCallbackManager;
    private static final String TAG = "FacebookAuthentication";

    // Define JAVA Google sign in
    private Button googleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;
    private String googleUserFullName;
    private String googleUserEmail;
    private String googleUserPhotoUrl;

    // Define Firebase
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mRootRef;
    private FirebaseUser firebaseUser;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind JAVA to XML
        toLoginBtn = findViewById(R.id.toLoginBtn);
        toSignUpBtn = findViewById(R.id.toSignupBtn);

        // Bind JAVA to XML google sign in button
        googleSignInButton = findViewById(R.id.withGGBtn);

        // Bind JAVA to facebook
        facebookSignInButton = (LoginButton) findViewById(R.id.withFBBtn);
        facebookSignInButton.setReadPermissions("email", "public_profile");

        // Initialize firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Prepare for google sign in method
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Set up Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        mCallbackManager = CallbackManager.Factory.create();

        // When click sign in by Facebook
        facebookSignInButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError" + error);
            }
        });

        // When click LOGIN button
        toLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });

        // When click SignUp button
        toSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSignupActivity();
            }
        });

        // When click google sign in button
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    // Function to handle Success Access Facebook Token
    private void handleFacebookToken(AccessToken token){
        Log.d(TAG, "handleFacebookToken" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Sign in with credential: successful");
                    Toast.makeText(MainActivity.this,"Authentication Success", Toast.LENGTH_SHORT).show();
                    firebaseUser = mFirebaseAuth.getCurrentUser();

                    // Create map user attribute
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("userFullName", firebaseUser.getDisplayName());
                    map.put("userEmail", firebaseUser.getEmail());
                    map.put("userID", mFirebaseAuth.getCurrentUser().getUid());
                    map.put("userBio", "");
                    map.put("userImageUrl", firebaseUser.getPhotoUrl().toString());

                    mRootRef.child("Users").child(mFirebaseAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Update the profile for better experience", Toast.LENGTH_SHORT).show();
                                goToHome();
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Sign in with credential: failure", task.getException());
                    Toast.makeText(MainActivity.this,"Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Function to show google sign in accounts selection when click btn google
    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Function to load data from GG/FB
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Facebook
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        // Google
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Function to check data google account end notify to user
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try{
            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            Toast.makeText(MainActivity.this,"Signed In Successfully",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(acc);
        }
        catch (ApiException e){
            Toast.makeText(MainActivity.this,"Sign In Failed",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }
    }

    // Function to create authentication Google account and init user Database to Firebase
    private void FirebaseGoogleAuth(GoogleSignInAccount acct) {
        if (acct != null) {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mFirebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mFirebaseAuth.getCurrentUser();
                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                        if(account !=  null){
                            googleUserFullName = account.getGivenName();
                            googleUserEmail = account.getEmail();
                            googleUserPhotoUrl = account.getPhotoUrl().toString();

                            // Create map user attribute
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("userFullName", googleUserFullName);
                            map.put("userEmail", googleUserEmail);
                            map.put("userID", mFirebaseAuth.getCurrentUser().getUid());
                            map.put("userBio", "");
                            map.put("userImageUrl", googleUserPhotoUrl);

                            mRootRef.child("Users").child(mFirebaseAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(MainActivity.this, "Update the profile for better experience", Toast.LENGTH_SHORT).show();
                                        goToHome();
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(MainActivity.this, "acc failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to open Login view
    public void openLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // Function to open SignUp View
    public void openSignupActivity(){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    // Function to go to Main view
    public void goToHome(){
        Intent intent = new Intent(this, BottomNavbarActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // When start app => check if account is exists then go to Main view
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            goToHome();
        }
    }
}