package com.kevalshetta.shetta.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kevalshetta.shetta.Post.MainActivity;
import com.kevalshetta.shetta.R;
import com.kevalshetta.shetta.Start.SetupActivity;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yesterselga.countrypicker.CountryPicker;
import com.yesterselga.countrypicker.CountryPickerListener;
import com.yesterselga.countrypicker.Theme;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView edit_profile_user_image;
    private EditText edit_profile_user_name, edit_profile_full_name, edit_profile_caption_name, edit_profile_country_name;
    private Button edit_save_profile;

    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    private ProgressDialog loadingBar;

    private Uri imageUri;

    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        edit_profile_user_image = findViewById(R.id.edit_profile_user_image);
        edit_profile_user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(EditProfileActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        edit_profile_user_name = findViewById(R.id.edit_profile_user_name);
        edit_profile_user_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edit_profile_full_name = findViewById(R.id.edit_profile_full_name);
        edit_profile_full_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edit_profile_caption_name = findViewById(R.id.edit_profile_caption_name);
        edit_profile_caption_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edit_profile_country_name = findViewById(R.id.edit_profile_country_name);
        edit_profile_country_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountryPicker picker = CountryPicker.newInstance("Select Country", Theme.DARK);  // dialog title and theme
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {

                        // Implement your code here
                        edit_profile_country_name.setText(name);
                        picker.dismiss();
                    }
                });
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });

        loadingBar = new ProgressDialog(this);

        edit_save_profile = findViewById(R.id.edit_save_profile);
        edit_save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("profileimage")) && (snapshot.hasChild("fullname"))) {
                    String userName = snapshot.child("username").getValue().toString();
                    String countryName = snapshot.child("countryname").getValue().toString();
                    String fullName = snapshot.child("fullname").getValue().toString();
                    String userDescription = snapshot.child("description").getValue().toString();
                    String userProfileImage = snapshot.child("profileimage").getValue().toString();


                    edit_profile_user_name.setText(userName);
                    edit_profile_full_name.setText(fullName);
                    edit_profile_country_name.setText(countryName);
                    edit_profile_caption_name.setText(userDescription);
                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(edit_profile_user_image);
                }
                else  if ((snapshot.exists()) && (snapshot.hasChild("fullname"))){
                    String userName = snapshot.child("username").getValue().toString();
                    String countryName = snapshot.child("countryname").getValue().toString();
                    String fullName = snapshot.child("fullname").getValue().toString();
                    String userDescription = snapshot.child("description").getValue().toString();

                    edit_profile_user_name.setText(userName);
                    edit_profile_full_name.setText(fullName);
                    edit_profile_country_name.setText(countryName);
                    edit_profile_caption_name.setText(userDescription);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                    ActivityCompat.requestPermissions(EditProfileActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                }
            });

            extraInfo.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(EditProfileActivity.this, "Some Feature of App Might not Work.", Toast.LENGTH_SHORT).show();
                }
            });
            extraInfo.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri resultUri = data.getData();
        final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
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
                    UsersRef.child("profileimage").setValue(downloadUrl).
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(EditProfileActivity.this, "Profile Image stored to Database Successfully...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(EditProfileActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void ValidateAccountInfo() {
        String username =  edit_profile_user_name.getText().toString();
        String fullname =  edit_profile_full_name.getText().toString();
        String caption =  edit_profile_caption_name.getText().toString();
        String country =  edit_profile_country_name.getText().toString();

        if (!TextUtils.isEmpty(username)){
            if (!TextUtils.isEmpty(fullname)){
                if (!TextUtils.isEmpty(caption)){
                    if (!TextUtils.isEmpty(country)){
                        loadingBar.setTitle("Saving Information");
                        loadingBar.setMessage("Please wait, while we are creating your new Account...");
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(true);

                        HashMap userMap = new HashMap();
                        userMap.put("username", username);
                        userMap.put("fullname", fullname);
                        userMap.put("countryname", country);
                        userMap.put("description", caption);
                        userMap.put("currentuserid", currentUserID);

                        UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task task) {
                                if (task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(EditProfileActivity.this, "your Account is created Successfully.", Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }else{
                                    edit_save_profile.setEnabled(true);
                                    edit_save_profile.setTextColor(Color.rgb(0,0,0));

                                    String message =  task.getException().getMessage();
                                    Toast.makeText(EditProfileActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

                    }else{
                        Toast.makeText(EditProfileActivity.this,"Please enter country name",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(EditProfileActivity.this,"Please enter caption",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(EditProfileActivity.this,"Please enter full name",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(EditProfileActivity.this,"Please enter user name",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(edit_profile_user_name.getText().toString())){
            if (!TextUtils.isEmpty(edit_profile_full_name.getText().toString())){
                if (!TextUtils.isEmpty(edit_profile_caption_name.getText().toString())){
                    edit_save_profile.setEnabled(true);
                    edit_save_profile.setTextColor(Color.rgb(0,0,0));
                }else{
                    edit_save_profile.setEnabled(false);
                    edit_save_profile.setTextColor(Color.argb(50,0,0,0));
                }
            }else{
                edit_save_profile.setEnabled(false);
                edit_save_profile.setTextColor(Color.argb(50,0,0,0));
            }
        }else{
            edit_save_profile.setEnabled(false);
            edit_save_profile.setTextColor(Color.argb(50,0,0,0));
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
        Intent mainIntent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(mainIntent);
    }
}