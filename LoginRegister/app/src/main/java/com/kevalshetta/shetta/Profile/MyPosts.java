package com.kevalshetta.shetta.Profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.shetta.Post.CommentsActivity;
import com.kevalshetta.shetta.Post.MainActivity;
import com.kevalshetta.shetta.R;
import com.kevalshetta.shetta.Utils.Posts;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPosts extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView myPostList;
    private FirebaseAuth mAuth;
    String currentUserID, saveCurrentDate;
    private DatabaseReference PostsRef, UsersRef, LikeRef;
    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        myPostList = (RecyclerView) findViewById(R.id.myPostList);
        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);

        Date date = new Date();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy  HH:mm:ss");
        saveCurrentDate = currentDate.format(date);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query myPosts = PostsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(myPosts,Posts.class).build();

        FirebaseRecyclerAdapter<Posts, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position, @NonNull @NotNull Posts model) {
                holder.postDesc.setText(model.getDescription());
                String timeAgo = calculateTimeAgo(model.getDate());
                holder.timeAgo.setText(timeAgo);
                holder.profileUsername.setText(model.getUsername());
                holder.profile_username.setText(model.getUsername());
                Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.profileImage);
                Picasso.get().load(model.getPostimage()).into(holder.post_image);

                final String PostKey = getRef(position).getKey();

                holder.setLikesButtonStatus(PostKey);

                holder.likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;
                        LikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (LikeChecker.equals(true)) {
                                    if (snapshot.child(PostKey).hasChild(currentUserID)) {
                                        LikeRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                        notifyDataSetChanged();
                                    } else {
                                        LikeRef.child(PostKey).child(currentUserID).setValue("like");
                                        LikeChecker = false;
                                        notifyDataSetChanged();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                });

                holder.comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent comment = new Intent(MyPosts.this, CommentsActivity.class);
                        comment.putExtra("PostKey", PostKey);
                        comment.putExtra("url", model.getProfileimage());
                        startActivity(comment);
                    }
                });

                DatabaseReference commRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comment");

                holder.setCommentButtonStatus(PostKey, commRef);

                holder.sendComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if ((snapshot.exists()) && (snapshot.hasChild("profileimage")) && (snapshot.hasChild("fullname"))) {
                                    String userName = snapshot.child("fullname").getValue().toString();
                                    String userProfileImage = snapshot.child("profileimage").getValue().toString();

                                    String comment = holder.inputComments.getText().toString();
                                    if (comment.isEmpty()) {
                                        Toast.makeText(MyPosts.this, "Please write comment...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        final String randomkey = currentUserID + saveCurrentDate;

                                        HashMap hashMap = new HashMap();
                                        hashMap.put("username", userName);
                                        hashMap.put("profileimage", userProfileImage);
                                        hashMap.put("comment", comment);
                                        hashMap.put("date", saveCurrentDate);
                                        commRef.child(randomkey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(MyPosts.this, "Comment send", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(MyPosts.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @NotNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post, parent, false);
                return new MyViewHolder(view);
            }
        };
        myPostList.setAdapter(adapter);
        adapter.startListening();
    }

    private String calculateTimeAgo(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy  HH:mm:ss");
        try {
            long time = sdf.parse(date).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

            return ago+"";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private ImageView post_image, likes, comments, sendComments;
        private TextView profileUsername, timeAgo, postDesc, profile_username, likeCounter, commentsCounter, location;
        int countLikes, countComments;
        private EditText inputComments;
        String currentUserId;
        private DatabaseReference LikesRef;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            post_image = itemView.findViewById(R.id.post_image);
            likes = itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.comments);
            profileUsername = itemView.findViewById(R.id.profileUsername);
            timeAgo = itemView.findViewById(R.id.timeAgo);
            postDesc = itemView.findViewById(R.id.postDesc);
            profile_username = itemView.findViewById(R.id.profile_username);
            likeCounter = itemView.findViewById(R.id.likeCounter);
            commentsCounter = itemView.findViewById(R.id.commentsCounter);
//            location = itemView.findViewById(R.id.location);

            inputComments = itemView.findViewById(R.id.inputComments);
            sendComments = itemView.findViewById(R.id.sendComment);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        public void setLikesButtonStatus(final String PostKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.child(PostKey).hasChild(currentUserId)){
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        likes.setImageResource(R.drawable.like);
                        likeCounter.setText(Integer.toString(countLikes) + " Like");
                    }else{
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        likes.setImageResource(R.drawable.outlined);
                        likeCounter.setText(Integer.toString(countLikes) + " Like");
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });


        }

        public void setCommentButtonStatus(String postKey, DatabaseReference commRef) {
            commRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        countComments = (int) snapshot.getChildrenCount();
                        commentsCounter.setText(Integer.toString(countComments) + " Comment");
                    }else{
                        countComments = (int) snapshot.getChildrenCount();
                        commentsCounter.setText(Integer.toString(countComments) + " Comment");
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
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
        Intent mainIntent = new Intent(MyPosts.this, MainActivity.class);
        startActivity(mainIntent);
    }
}