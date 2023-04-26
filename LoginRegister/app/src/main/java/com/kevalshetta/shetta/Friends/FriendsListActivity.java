package com.kevalshetta.shetta.Friends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.shetta.Chat.ChatActivity;
import com.kevalshetta.shetta.Post.MainActivity;
import com.kevalshetta.shetta.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView friends_list;

    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private TextView totalFriends;
    private int countFriends = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Friend");

        friends_list = (RecyclerView) findViewById(R.id.friends_list);
        friends_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friends_list.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        totalFriends = findViewById(R.id.totalFriends);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(FriendsRef, Friends.class)
                        .build();
        final FirebaseRecyclerAdapter<Friends,FriendsListHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsListHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull FriendsListHolder holder, int position, @NonNull @NotNull Friends model) {
                final String userIDs = getRef(position).getKey();

                final String[] userImage = {"default_image"};

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild("profileimage"))
                            {
                                userImage[0] = snapshot.child("profileimage").getValue().toString();
                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile).into(holder.friends_profile_image);
                            }
                            String profileName = snapshot.child("username").getValue().toString();
                            String profilefName = snapshot.child("fullname").getValue().toString();
                            String profileStatus = snapshot.child("description").getValue().toString();

                            holder.friend_user_name.setText(profileName);
                            holder.friend_full_name.setText(profilefName);
                            holder.friend_caption.setText(profileStatus);

                            holder.friend_send_message.setVisibility(View.VISIBLE);

                            holder.friend_send_message.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(FriendsListActivity.this, ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",userIDs);
                                    chatIntent.putExtra("visit_user_name",profileName);
                                    chatIntent.putExtra("visit_image", userImage[0]);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

                FriendsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            countFriends = (int) snapshot.getChildrenCount();
                            totalFriends.setText(Integer.toString(countFriends) + " Friend");
                        }
                        else{
                            totalFriends.setText("0 Friends");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @NotNull
            @Override
            public FriendsListHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_display_layout, parent, false);
                return new FriendsListHolder(view);
            }
        };
        friends_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsListHolder extends RecyclerView.ViewHolder{

        TextView friend_user_name, friend_caption, friend_full_name;
        CircleImageView friends_profile_image;
        ImageView friend_send_message;

        public FriendsListHolder(@NonNull View itemView) {
            super(itemView);

            friend_user_name = itemView.findViewById(R.id.friend_user_name);
            friend_caption = itemView.findViewById(R.id.friend_caption);
            friend_full_name = itemView.findViewById(R.id.friend_full_name);
            friends_profile_image = itemView.findViewById(R.id.friends_profile_image);
            friend_send_message = itemView.findViewById(R.id.friend_send_message);
        }
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
        Intent mainIntent = new Intent(FriendsListActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}