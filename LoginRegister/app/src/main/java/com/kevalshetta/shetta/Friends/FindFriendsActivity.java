package com.kevalshetta.shetta.Friends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.kevalshetta.shetta.Post.MainActivity;
import com.kevalshetta.shetta.R;
import com.kevalshetta.shetta.Utils.Users;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DatabaseReference UserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private RecyclerView findFriendsRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        findFriendsRv = findViewById(R.id.findFriendsRv);
        findFriendsRv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        findFriendsRv.setLayoutManager(linearLayoutManager);

        LoadUsers("");
    }

    private void LoadUsers(String s) {

        Query searchFriends = UserRef.orderByChild("username").startAt(s).endAt(s + "\uf8ff");

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(searchFriends, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users,FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Users, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull FindFriendViewHolder holder, int position, @NonNull @NotNull Users model) {

                if (!currentUserId.equals(getRef(position).getKey().toString())){
                    holder.find_friend_username.setText(model.getUsername());
                    holder.find_friend_fullname.setText(model.getFullname());

                    Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.find_friend_profile_image);
                }else{
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this, FriendsProfileActivity.class);
                        profileIntent.putExtra("user", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @NotNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friends, parent, false);
                return new FindFriendViewHolder(view);
            }
        };
        findFriendsRv.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView find_friend_profile_image;
        private TextView find_friend_username, find_friend_fullname;

        public FindFriendViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            find_friend_profile_image = itemView.findViewById(R.id.find_friend_profile_image);
            find_friend_username = itemView.findViewById(R.id.find_friend_username);
            find_friend_fullname = itemView.findViewById(R.id.find_friend_fullname);
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
        Intent mainIntent = new Intent(FindFriendsActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_friend);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                LoadUsers(s);
                return false;
            }
        });
        return true;
    }
}