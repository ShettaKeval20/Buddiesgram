package com.kevalshetta.shetta.Post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.kevalshetta.shetta.R;
import com.kevalshetta.shetta.Utils.Comments;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commRV;
    private EditText commInputComments;
    private ImageView commentSend;

    private DatabaseReference UsersRef, commRef;
    private FirebaseAuth mAuth;

    private String postKey, currentUserID, saveCurrentDate;

    private long countComments = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postKey = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        commRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("Comment");

        commRV = findViewById(R.id.commRV);
        commRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commRV.setLayoutManager(linearLayoutManager);

        commInputComments = findViewById(R.id.commInputComments);
        commentSend = findViewById(R.id.commentSend);

        Date date = new Date();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy  HH:mm:ss");
        saveCurrentDate = currentDate.format(date);

        commentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if ((snapshot.exists()) && (snapshot.hasChild("profileimage")) && (snapshot.hasChild("fullname"))) {
                            String userName = snapshot.child("fullname").getValue().toString();
                            String userProfileImage = snapshot.child("profileimage").getValue().toString();

                            ValidateComment(userName, userProfileImage);

                            commInputComments.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void ValidateComment(String userName, String userProfileImage) {
        commRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    countComments = snapshot.getChildrenCount();
                }else{
                    countComments = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        String comment = commInputComments.getText().toString();
        if (comment.isEmpty()){
            Toast.makeText(CommentsActivity.this, "Please write comment...", Toast.LENGTH_SHORT).show();
        }else{
            final String randomkey = currentUserID + saveCurrentDate;

            HashMap hashMap = new HashMap();
            hashMap.put("username", userName);
            hashMap.put("profileimage", userProfileImage);
            hashMap.put("comment", comment);
            hashMap.put("date", saveCurrentDate);
            hashMap.put("counter", countComments);

            commRef.child(randomkey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull @NotNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "Comment send", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(CommentsActivity.this, ""+task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query sortComment = commRef.orderByChild("counter");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(sortComment,Comments.class).build();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull CommentsViewHolder holder, int position, @NonNull @NotNull Comments model) {

                holder.comment_username.setText(model.getUsername());
                holder.commentsTv.setText(model.getComment());
                Picasso.get().load(model.getProfileimage()).into(holder.profileImage_comment);

            }

            @NonNull
            @NotNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_comment, parent, false);
                return new CommentsViewHolder(view);
            }
        };
        commRV.setAdapter(adapter);
        adapter.startListening();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage_comment;
        TextView comment_username, commentsTv, time;

        public CommentsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            profileImage_comment = itemView.findViewById(R.id.profileImagecomment);
            comment_username = itemView.findViewById(R.id.comment_username);
            commentsTv = itemView.findViewById(R.id.commentsTv);
            time = itemView.findViewById(R.id.commentTime);
        }
    }
}