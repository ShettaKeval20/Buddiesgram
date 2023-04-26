package com.kevalshetta.shetta.Profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.shetta.Post.EditPost;
import com.kevalshetta.shetta.Post.MainActivity;
import com.kevalshetta.shetta.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profile_user_image;
    private TextView profile_user_name, profile_full_name, profile_caption_name, profile_country_name;
    private Button editProfile, myPosts;

    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference UsersRef, PostsRef;

    private int countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile Activity");

        profile_user_image = findViewById(R.id.profile_user_image);
        profile_user_name = findViewById(R.id.profile_user_name);
        profile_full_name = findViewById(R.id.profile_full_name);
        profile_caption_name = findViewById(R.id.profile_caption_name);
        profile_country_name = findViewById(R.id.profile_country_name);

        editProfile = findViewById(R.id.editProfile);
        myPosts = findViewById(R.id.myPosts);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });
        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,MyPosts.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserID);


        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("profileimage")) && (snapshot.hasChild("fullname"))) {
                    String userName = snapshot.child("username").getValue().toString();
                    String countryName = snapshot.child("countryname").getValue().toString();
                    String fullName = snapshot.child("fullname").getValue().toString();
                    String userDescription = snapshot.child("description").getValue().toString();
                    String userProfileImage = snapshot.child("profileimage").getValue().toString();


                    profile_user_name.setText(userName);
                    profile_full_name.setText(fullName);
                    profile_country_name.setText(countryName);
                    profile_caption_name.setText(userDescription);
                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(profile_user_image);
                }
                else  if ((snapshot.exists()) && (snapshot.hasChild("fullname"))){
                    String userName = snapshot.child("username").getValue().toString();
                    String countryName = snapshot.child("countryname").getValue().toString();
                    String fullName = snapshot.child("fullname").getValue().toString();
                    String userDescription = snapshot.child("description").getValue().toString();

                    profile_user_name.setText(userName);
                    profile_full_name.setText(fullName);
                    profile_country_name.setText(countryName);
                    profile_caption_name.setText(userDescription);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}