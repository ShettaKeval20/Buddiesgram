package com.kevalshetta.shetta.Post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kevalshetta.shetta.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class EditPost extends AppCompatActivity {

    private ImageView postImage;
    private TextView postDescription;
    private Button deletePost, editPost;

    private DatabaseReference ClickPostRef;

    private String PostKey, currentUserId, databaseUserId, age;
    private FirebaseAuth mAuth;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Post");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        PostKey = getIntent().getExtras().get("PostKey").toString();
        age = getIntent().getExtras().get("age").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        postImage = findViewById(R.id.edit_post);
        postDescription = findViewById(R.id.editPostDesc);

        deletePost = findViewById(R.id.deletePost);
        editPost = findViewById(R.id.editPost);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String des = snapshot.child("description").getValue().toString();
                    String image = snapshot.child("postimage").getValue().toString();
                    String uid = snapshot.child("uid").getValue().toString();

                    postDescription.setText(des);
                    Picasso.get().load(image).into(postImage);

                    if (currentUserId.equals(uid)){
                        deletePost.setVisibility(View.VISIBLE);
                        editPost.setVisibility(View.VISIBLE);
                    }else{
                        deletePost.setVisibility(View.INVISIBLE);
                        editPost.setVisibility(View.INVISIBLE);
                    }

                    editPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(des);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });

    }

    private void EditCurrentPost(String des) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPost.this, R.style.AlertDialogTheme);
        builder.setTitle("Edit Post:");

        final EditText inputField = new EditText(EditPost.this);
        inputField.setText(des);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(EditPost.this, "Post updated successfully...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.darker_gray);
    }

    private void DeleteCurrentPost() {
        ClickPostRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                SendUserToMainActivity();
                Toast.makeText(EditPost.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(age);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("Picture","#deleted");
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
        Intent mainIntent = new Intent(EditPost.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}