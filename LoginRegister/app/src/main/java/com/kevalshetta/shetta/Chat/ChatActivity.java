package com.kevalshetta.shetta.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.kevalshetta.shetta.Post.AddNewPost;
import com.kevalshetta.shetta.Post.MainActivity;
import com.kevalshetta.shetta.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, status;
    private CircleImageView userImage;
    private Toolbar ChatToolBar;
    private ImageView SendFilesButton;

    private String checker="null", myUrl ="null";
    private Uri imageUri, fileuri ;
    private StorageTask uploadTask;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    private EmojiconEditText MessageInputText;
    private EmojIconActions emojIconActions;
    private ImageView send,emoji;
    private View view;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime, saveCurrentDate;
    private StorageReference UserMessageImageRef;
    private StorageReference UserDocRef;

    String URL = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        UserMessageImageRef = FirebaseStorage.getInstance().getReference().child("Chat Images");
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        InitializeControllers();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile).into(userImage);

        requestQueue = Volley.newRequestQueue(this);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF Files",
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){
                            checker = "image";

                            ImagePicker.with(ChatActivity.this)
                                    .crop()	    			//Crop image(Optional), Check Customization for more option
                                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                                    .start();
                        }
                        if (i == 1){
                            checker = "pdf";

                            if(hasPermission(ChatActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                                Intent pdf = new Intent();
                                pdf.setAction(Intent.ACTION_GET_CONTENT);
                                pdf.setType("application/pdf");
                                startActivityForResult(pdf,3000);
                            }else {
                                ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                            }
                        }
                        if (i == 2){
//                            checker = "docx";
//
//                            if(hasPermission(ChatActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//                                Intent pdf = new Intent();
//                                pdf.setAction(Intent.ACTION_GET_CONTENT);
//                                pdf.setType("application/msword");
//                                startActivityForResult(pdf,3000);
//                            }else {
//                                ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
//                            }
                        }
                    }
                });
                builder.show();
            }
        });


        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages = snapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        loadingBar = new ProgressDialog(this);
    }

    private boolean hasPermission(Context context, String permission) {
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
                    ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                }
            });

            extraInfo.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(ChatActivity.this, "Some Feature of App Might not Work.", Toast.LENGTH_SHORT).show();
                }
            });
            extraInfo.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== SELECT_FILE && resultCode==RESULT_OK && data!=null){
            imageUri= data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        else if (requestCode==1000 && resultCode==RESULT_OK && data!=null){
            imageUri= data.getData();
        }
        else if (requestCode == 3000 && resultCode==RESULT_OK && data!=null){
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sending your file...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            imageUri = data.getData();

            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            final String messagePushID = userMessageKeyRef.getKey();

            final StorageReference filePath = UserDocRef.child(messagePushID + "." + checker);
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",downloadUrl);
                            messageImageBody.put("name",imageUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",messageSenderID);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);


                            Map messageBodyDetail = new HashMap();
                            messageBodyDetail.put(messageSenderRef+ "/" + messagePushID, messageImageBody);
                            messageBodyDetail.put(messageReceiverRef+ "/" + messagePushID, messageImageBody);

                            RootRef.updateChildren(messageBodyDetail);
                            loadingBar.dismiss();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingBar.dismiss();
                    Toast.makeText(ChatActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double p = (100.0*snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    loadingBar.setMessage((int) p + " % Uploading....");
                }
            });
        }
        Uri resultUri = data.getData();

        String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();

        final String messagePushID = userMessageKeyRef.getKey();

        final StorageReference filePath = UserMessageImageRef.child(messagePushID + "." + "jpg");
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
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downUri = task.getResult();
                    final String downloadUrl = downUri.toString();

                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message", downloadUrl);
                    messageTextBody.put("name", resultUri.getLastPathSegment());
                    messageTextBody.put("type", checker);
                    messageTextBody.put("from", messageSenderID);
                    messageTextBody.put("to", messageReceiverID);
                    messageTextBody.put("messageID", messagePushID);
                    messageTextBody.put("time", saveCurrentTime);
                    messageTextBody.put("date", saveCurrentDate);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                    messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Image Sent Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            MessageInputText.setText("");
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void SendMessage() {
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Can't send empty message...", Toast.LENGTH_SHORT).show();
        }else{
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("status", "unseen");
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        sendNotification(messageText);
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
    }

    private void sendNotification(String messageText) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics" +messageReceiverID);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("title","Message from"+messageReceiverName);
            jsonObject1.put("body",messageText);

            JSONObject jsonObject2 =new JSONObject();
            jsonObject2.put("userID", mAuth.getCurrentUser().getUid());
            jsonObject2.put("type", "messageText");

            jsonObject1.put("notification",jsonObject1);
            jsonObject1.put("data",jsonObject2);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("content-type","application/json");
                    map.put("authorization","key~AAAAppRPnbc:APA91bG5qFgrhrAgN04tpWDHRuIfI_aXb1f6dyMKJ75mdQ8ysSQ7MumHGSnrkYUOghFwFmObHVzZZKqjM_XBPYXNM9RuTj8EzhehXSlv002MDMoGt31u1t7wy3FDsjhNT-sHGgbIIE7j");
                    return map;
                }
            };
            requestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void InitializeControllers() {
        ChatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.app_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.username);
//        status =  findViewById(R.id.status);
        userImage =  findViewById(R.id.primage);


        MessageInputText = findViewById(R.id.emoji_edit_text);
        send = findViewById(R.id.send_icon);
        emoji = findViewById(R.id.emoji_icon);
        view = findViewById(R.id.root_view);
        SendFilesButton = findViewById(R.id.add_files);

        emojIconActions = new EmojIconActions(this,view,MessageInputText,emoji);
        emojIconActions.ShowEmojIcon();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        messageAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        UserMessageImageRef = FirebaseStorage.getInstance().getReference().child("Images Files");
        UserDocRef = FirebaseStorage.getInstance().getReference().child("Document Files");
    }

    private void DisplayLastSeen(){
        RootRef.child("Users").child(messageSenderID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                status.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                status.setText("Last Seen: " + date + " " + time);
                            }
                        }
                        else
                        {
                            status.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
        Intent mainIntent = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}