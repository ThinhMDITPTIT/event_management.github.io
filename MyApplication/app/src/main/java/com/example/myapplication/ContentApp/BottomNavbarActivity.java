package com.example.myapplication.ContentApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import com.example.myapplication.CalendarFragment;
import com.example.myapplication.HomeFragment;
import com.example.myapplication.MainActivity;
import com.example.myapplication.ProfileFragment;
import com.example.myapplication.R;
import com.example.myapplication.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BottomNavbarActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navbar);
        Intent intent = getIntent();
        Boolean isLogin = intent.getBooleanExtra("isLogin",false);
        BottomNavigationView toolbar = findViewById(R.id.bottomnavigation);
        toolbar.setOnNavigationItemSelectedListener(navlistener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new HomeFragment()).commit();
    }

    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Log.i("User","User is unAuthenticated!");
            backToStart();
        }else {
            Log.i("User","User is logged in App!");
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navlistener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.navigation_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.navigation_search:
                            selectedFragment = new SearchFragment();
                            break;
                        case R.id.navigation_add_event:
                            selectedFragment = null;
                            toAddEvent();
                            break;
                        case R.id.navigation_calendar:
                            selectedFragment = new CalendarFragment();
                            break;
                        case R.id.navigation_profile:
                            selectedFragment = new ProfileFragment();
                            break;

                    }
                    if(selectedFragment != null){
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,selectedFragment).commit();
                    }
                    return true;
                }
            };

    //When click on ImageUser Event post or comment => change view to profile user (include profileID in Context)
//    Bundle intent = getIntent().getExtras();
//    if (intent != null){
//        String profileID = intent.getString("publisherID");
//
//        getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileID", profileID).apply();
//
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view_tag, new ProfileFragment()).commit();
//        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
//    } else {
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view_tag, new HomeFragment()).commit();
//    }


    private void backToStart(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void toAddEvent(){
        Intent intent = new Intent(this, AddEventActivity.class);
        startActivity(intent);
        finish();
    }
}