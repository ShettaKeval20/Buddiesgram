package com.kevalshetta.shetta.Start;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kevalshetta.shetta.R;

import org.jetbrains.annotations.NotNull;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;
    private ImageView visibility;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";

    private TextView alreadyUser;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserEmail.addTextChangedListener(new TextWatcher() {
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

        UserPassword = (EditText) findViewById(R.id.register_password);
        UserPassword.addTextChangedListener(new TextWatcher() {
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

        UserConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        UserConfirmPassword.addTextChangedListener(new TextWatcher() {
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

        CreateAccountButton = (Button) findViewById(R.id.register_create_account);
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailAndPassword();
            }
        });

        alreadyUser = findViewById(R.id.already_account);
        alreadyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        loadingBar = new ProgressDialog(this);
    }

    private void checkEmailAndPassword() {
        if (UserEmail.getText().toString().matches(emailPattern)){
            if (UserPassword.getText().toString().equals(UserConfirmPassword.getText().toString())){
                mAuth.createUserWithEmailAndPassword(UserEmail.getText().toString(),UserPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    SendEmailverificationMessage();
                                    loadingBar.dismiss();
                                }else{
                                    CreateAccountButton.setEnabled(true);
                                    CreateAccountButton.setTextColor(Color.rgb(0,0,0));

                                    String message = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
            }else{
                UserPassword.setError("Password Doesn't match ");
            }
        }else{
            UserEmail.setError("Invalid Email");
        }
    }

    private void SendEmailverificationMessage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registration Successful, we've send you a mail. Please check and verify your account in login...", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        SendUserToLoginActivity();
                    }else{
                        String error = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this,"Error occurred:" + error,Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }

    private void SendUserToLoginActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(mainIntent);
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(UserEmail.getText().toString())){
            if (!TextUtils.isEmpty(UserPassword.getText()) && UserPassword.length()>=8){
                if (!TextUtils.isEmpty(UserConfirmPassword.getText().toString())){
                    CreateAccountButton.setEnabled(true);
                    CreateAccountButton.setTextColor(Color.rgb(0,0,0));
                }else{
                    CreateAccountButton.setEnabled(false);
                    CreateAccountButton.setTextColor(Color.argb(50,0,0,0));
                }
            }else{
                CreateAccountButton.setEnabled(false);
                CreateAccountButton.setTextColor(Color.argb(50,0,0,0));
            }
        }else{
            CreateAccountButton.setEnabled(false);
            CreateAccountButton.setTextColor(Color.argb(50,0,0,0));
        }
    }
}