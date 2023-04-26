package com.kevalshetta.shetta.Post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kevalshetta.shetta.Chat.ChatActivity;
import com.kevalshetta.shetta.Chat.ChatUserActivity;
import com.kevalshetta.shetta.Friends.FindFriendsActivity;
import com.kevalshetta.shetta.Friends.FriendsListActivity;
import com.kevalshetta.shetta.Profile.ProfileActivity;
import com.kevalshetta.shetta.R;
import com.kevalshetta.shetta.Request.FriendRequestActivity;
import com.kevalshetta.shetta.Start.LoginActivity;
import com.kevalshetta.shetta.Start.SetupActivity;
import com.kevalshetta.shetta.Utils.Posts;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    String currentUserID;

    private StorageReference PostsImagesRefrence;
    private DatabaseReference UsersRef, PostsRef, LikeRef, commentRef, RootRef;

    private Toolbar mToolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;

    private ImageView post, next;
    private EditText caption;
    private String Description;

    private Uri imageUri, resultUri;

    final static int Gallery_Pick = 1;

    private ProgressDialog loadingBar;

    private String saveCurrentDate, postRandomName;

    FirebaseRecyclerAdapter<Posts, MyViewHolder> adapter;
    FirebaseRecyclerOptions<Posts> options;

    private RecyclerView recyclerView;

    Boolean LikeChecker = false;

    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostsImagesRefrence = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);

        FirebaseMessaging.getInstance().subscribeToTopic(currentUserID);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        post = findViewById(R.id.add_post);
        caption = findViewById(R.id.post_description);
        next = findViewById(R.id.add_send);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView =  findViewById(R.id.navView);

        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        NavProfileImage = (CircleImageView) view.findViewById(R.id.profile_image_header);
        NavProfileUserName = (TextView) view.findViewById(R.id.username_header);

        loadingBar = new ProgressDialog(this);

        Date date = new Date();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy  HH:mm:ss");
        saveCurrentDate = currentDate.format(date);

        postRandomName = saveCurrentDate;

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });
    }

    private void DisplayAllUsersPosts() {
        Query sortPost = PostsRef.orderByChild("counter");

        options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(sortPost,Posts.class).build();
        adapter = new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull @NotNull Posts model) {

                final String PostKey = getRef(position).getKey();

                UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild("profileimage"))
                            {
                                String profileImage = snapshot.child("profileimage").getValue().toString();
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(holder.profileImage);
                            }
                        }

                        holder.postDesc.setText(model.getDescription());
                        String timeAgo = calculateTimeAgo(model.getDate());
                        holder.timeAgo.setText(timeAgo);
                        holder.profileUsername.setText(model.getUsername());
                        holder.profile_username.setText(model.getUsername());

                        Picasso.get().load(model.getPostimage()).into(holder.post_image);
                        Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.profileImage);

                        final String PostKey = getRef(position).getKey();

                        holder.post_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final CharSequence[] items = {"Edit", "Full Screen", "Cancel"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Select");
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        if (items[i].equals("Edit")) {
                                            Intent click = new Intent(MainActivity.this, EditPost.class);
                                            click.putExtra("PostKey", PostKey);
                                            click.putExtra("age", model.getPostimage());
                                            startActivity(click);
                                        } else if (items[i].equals("Full Screen")) {
                                            Intent intent = new Intent(MainActivity.this, FullScreenActivity.class);
                                            intent.putExtra("url", model.getPostimage());
                                            startActivity(intent);
                                        } else if (items[i].equals("Cancel")) {
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

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
                                Intent comment = new Intent(MainActivity.this, CommentsActivity.class);
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
                                                Toast.makeText(MainActivity.this, "Please write comment...", Toast.LENGTH_SHORT).show();
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
                                                            Toast.makeText(MainActivity.this, "Comment send", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
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

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

//                loadComments(PostKey, commRef);

            }

            @NonNull
            @NotNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post, parent, false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
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

        public static RecyclerView commentsRv;

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

            commentsRv = itemView.findViewById(R.id.recyclerViewComments);

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

    private void ValidatePostInfo() {
        Description = caption.getText().toString();
        if(resultUri == null)
        {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        StorageReference filePath = PostsImagesRefrence.child("Post Images").child(resultUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Uri> task) {
                Uri downUri = task.getResult();

                final String downloadUrl = downUri.toString();
                SavingPostInformationToDatabase(downloadUrl);
            }
        });
    }

    private void SavingPostInformationToDatabase(String downloadUrl) {
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    countPosts = snapshot.getChildrenCount();
                }else{
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userName = dataSnapshot.child("username").getValue().toString();

                    if (dataSnapshot.hasChild("profileimage")){
                        String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                        HashMap postsMap = new HashMap();
                        postsMap.put("uid", currentUserID);
                        postsMap.put("date", saveCurrentDate);
                        postsMap.put("description", Description);
                        postsMap.put("postimage", downloadUrl);
                        postsMap.put("profileimage", userProfileImage);
                        postsMap.put("fullname", userFullName);
                        postsMap.put("username", userName);
                        postsMap.put("counter", countPosts);
                        PostsRef.child(currentUserID + postRandomName).updateChildren(postsMap)
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            SendUserToMainActivity();
                                            Toast.makeText(MainActivity.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else
                                        {
                                            Toast.makeText(MainActivity.this, "Error Occured while updating your post.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                    }else{
                        HashMap postsMap = new HashMap();
                        postsMap.put("uid", currentUserID);
                        postsMap.put("date", saveCurrentDate);
                        postsMap.put("description", Description);
                        postsMap.put("postimage", downloadUrl);
                        postsMap.put("fullname", userFullName);
                        postsMap.put("username", userName);
                        postsMap.put("counter", countPosts);
                        PostsRef.child(currentUserID + postRandomName).updateChildren(postsMap)
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            SendUserToMainActivity();
                                            Toast.makeText(MainActivity.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else
                                        {
                                            Toast.makeText(MainActivity.this, "Error Occured while updating your post.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void SendUserToMainActivity() {
        startActivity(new Intent(MainActivity.this,MainActivity.class));
    }

    private boolean hasPermission(Context context, String permission){
        return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 102){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                // permission is granted
                Toast.makeText(this, "Permission is Granted.", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permission is Denied.", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 103){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                // permission is granted
                Toast.makeText(this, "Permission is Granted.", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this, "Permission is Denied.", Toast.LENGTH_SHORT).show();
            }
        }
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            // alert dialog

            AlertDialog.Builder extraInfo = new AlertDialog.Builder(this);
            extraInfo.setTitle("Storage Permission is Required.");
            extraInfo.setMessage("To Run this app, App needs access to storage to save the file.");

            extraInfo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                }
            });

            extraInfo.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Some Feature of App Might not Work.", Toast.LENGTH_SHORT).show();
                }
            });
            extraInfo.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        resultUri = data.getData();
        post.setImageURI(resultUri);
        loadingBar.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        else {
            CheckUserExistence();

            UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("profileimage")) && (dataSnapshot.hasChild("fullname"))) {
                        String userName = dataSnapshot.child("fullname").getValue().toString();
                        String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                        NavProfileUserName.setText(userName);
                        Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(NavProfileImage);

                    }
                    else  if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("fullname"))){
                        String userName = dataSnapshot.child("fullname").getValue().toString();

                        NavProfileUserName.setText(userName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DisplayAllUsersPosts();
        }
    }

    private void CheckUserExistence() {
        {
            final String current_user_id = mAuth.getCurrentUser().getUid();

            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(!dataSnapshot.hasChild(current_user_id))
                    {
                        SendUserToSetupActivity();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void SendUserToSetupActivity() {
        startActivity(new Intent(MainActivity.this, SetupActivity.class));
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent fIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(fIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return true;
    }

    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.home:
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.add_new_post:
                startActivity(new Intent(MainActivity.this, AddNewPost.class));
                Toast.makeText(this, "Add new post", Toast.LENGTH_SHORT).show();
                break;

            case R.id.profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.friend:
                startActivity(new Intent(MainActivity.this, FriendsListActivity.class));
                Toast.makeText(this, "My Friend", Toast.LENGTH_SHORT).show();
                break;

            case R.id.find_friend:
                startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));
                Toast.makeText(this, "Find Friend", Toast.LENGTH_SHORT).show();
                break;

            case R.id.friend_request:
                startActivity(new Intent(MainActivity.this, FriendRequestActivity.class));
                Toast.makeText(this, "Friend Request", Toast.LENGTH_SHORT).show();
                break;

            case R.id.message:
                startActivity(new Intent(MainActivity.this, ChatUserActivity.class));
                Toast.makeText(this, "Chats", Toast.LENGTH_SHORT).show();
                break;

            case R.id.logout:

                mAuth.signOut();
                SendUserToLoginActivity();

                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
