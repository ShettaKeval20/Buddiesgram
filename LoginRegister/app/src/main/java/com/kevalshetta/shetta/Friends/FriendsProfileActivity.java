package com.kevalshetta.shetta.Friends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.shetta.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, Current_State, saveCurrentDate;

    private DatabaseReference UserRef, requestRef, FriendRef;
    private FirebaseAuth mAuth;

    private TextView friends_username, friends_fullname, friend_countryName;
    private CircleImageView friends_profile_image;
    private Button decline_friend_request, send_friend_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile_actvity);

        receiverUserID = getIntent().getExtras().get("user").toString();

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        friends_username = findViewById(R.id.friends_username);
        friends_fullname = findViewById(R.id.friends_fullname);
        friend_countryName = findViewById(R.id.friend_countryName);

        send_friend_request = findViewById(R.id.send_friend_request);
        decline_friend_request = findViewById(R.id.decline_friend_request);

        friends_profile_image = findViewById(R.id.friends_profile_image);

        Current_State = "not_friends";

        decline_friend_request.setVisibility(View.INVISIBLE);
        decline_friend_request.setEnabled(false);

        loadUser();
    }

    private void UnfriendPerson() {
        FriendRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                send_friend_request.setEnabled(true);
                                                Current_State = "not_friends";
                                                send_friend_request.setText("Send Friend Request");

                                                decline_friend_request.setVisibility(View.INVISIBLE);
                                                decline_friend_request.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendRef.child(senderUserID).child(receiverUserID)
                .child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendRef.child(receiverUserID).child(senderUserID)
                                    .child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                requestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    requestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        send_friend_request.setEnabled(true);
                                                                                        Current_State = "friends";
                                                                                        send_friend_request.setText("Unfriend this Person");

                                                                                        decline_friend_request.setVisibility(View.INVISIBLE);
                                                                                        decline_friend_request.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });



    }

    private void CancelChatRequest() {
        requestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            requestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                send_friend_request.setEnabled(true);
                                                Current_State = "not_friends";
                                                send_friend_request.setText("Send Friend Request");

                                                decline_friend_request.setVisibility(View.INVISIBLE);
                                                decline_friend_request.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintananceButton() {
        requestRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)){

                    String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (request_type.equals("sent"))
                    {
                        Current_State = "request_sent";
                        send_friend_request.setText("Cancel Friend Request");

                        decline_friend_request.setVisibility(View.INVISIBLE);
                        decline_friend_request.setEnabled(false);
                    }
                    else if (request_type.equals("received"))
                    {
                        Current_State = "request_received";
                        send_friend_request.setText("Accept Friend Request");

                        decline_friend_request.setVisibility(View.VISIBLE);
                        decline_friend_request.setEnabled(true);

                        decline_friend_request.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    FriendRef.child(senderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    if (dataSnapshot.hasChild(receiverUserID))
                                    {
                                        Current_State = "friends";
                                        send_friend_request.setText("Unfriend this Person");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendChatRequest() {
        requestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            requestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                send_friend_request.setEnabled(true);
                                                Current_State = "request_sent";
                                                send_friend_request.setText("Cancel Friend Request");

                                                decline_friend_request.setVisibility(View.INVISIBLE);
                                                decline_friend_request.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void loadUser() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("profileimage")) && (snapshot.hasChild("fullname"))) {
                    String userName = snapshot.child("username").getValue().toString();
                    String userFullName = snapshot.child("fullname").getValue().toString();
                    String userCountryName = snapshot.child("countryname").getValue().toString();
                    String userProfileImage = snapshot.child("profileimage").getValue().toString();

                    friends_username.setText(userName);
                    friends_fullname.setText(userFullName);
                    friend_countryName.setText(userCountryName);
                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(friends_profile_image);

                    MaintananceButton();

                }
                else  if ((snapshot.exists()) && (snapshot.hasChild("fullname"))){
                    String userName = snapshot.child("username").getValue().toString();
                    String userFullName = snapshot.child("fullname").getValue().toString();
                    String userCountryName = snapshot.child("countryname").getValue().toString();

                    friends_username.setText(userName);
                    friends_fullname.setText(userFullName);
                    friend_countryName.setText(userCountryName);

                    MaintananceButton();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendsProfileActivity.this, ""+error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        if (!senderUserID.equals(receiverUserID)){
            send_friend_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send_friend_request.setEnabled(false);
                    if (Current_State.equals("not_friends"))
                    {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        UnfriendPerson();
                    }
                }
            });
        }else
        {
            decline_friend_request.setVisibility(View.INVISIBLE);
            send_friend_request.setVisibility(View.INVISIBLE);
        }
    }
}