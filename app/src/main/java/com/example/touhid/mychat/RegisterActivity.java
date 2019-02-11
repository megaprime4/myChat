package com.example.touhid.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;
    private EditText registerUserName;
    private EditText registerUserEmail;
    private EditText registerUserPassword;
    private EditText registerConfPassword;
    private Button create_account_button;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerUserName = (EditText) findViewById(R.id.register_name_field);
        registerUserEmail = (EditText) findViewById(R.id.register_email_field);
        registerUserPassword = (EditText) findViewById(R.id.register_password_field);
        registerConfPassword = (EditText) findViewById(R.id.register_confPassword_field);
        create_account_button = (Button) findViewById(R.id.create_account_btn);
        progressDialog = new ProgressDialog(this);

        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = registerUserName.getText().toString();
                String email = registerUserEmail.getText().toString();
                String password = registerUserPassword.getText().toString();
                String confPassword = registerConfPassword.getText().toString();

                registerAccount(name, email, password, confPassword);
            }
        });
    }

    private void registerAccount(final String name, String email, String password, String confPassword) {
        if(TextUtils.isEmpty(name)) {
            //Toast.makeText(RegisterActivity.this, "Name is required!", Toast.LENGTH_SHORT).show();
            registerUserName.setText("name is required!");
        }
        if(TextUtils.isEmpty(email)) {
            //Toast.makeText(RegisterActivity.this, "Email is required!", Toast.LENGTH_SHORT).show();
            registerUserEmail.setText("email address is required!");
        }
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Password is required!", Toast.LENGTH_LONG).show();
            //register_user_password.setText("Password is required!");
        }

        if(TextUtils.isEmpty(confPassword)) {
            Toast.makeText(RegisterActivity.this,
                    "Confirm password field is empty!", Toast.LENGTH_SHORT).show();
        } else {

            if(password.equals(confPassword)) {

                progressDialog.setTitle("Creating new account");
                progressDialog.setMessage("Please wait..");
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            String currentUserID = mAuth.getCurrentUser().getUid();
                            storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(currentUserID);
                            storeUserDefaultDataReference.child("user_name").setValue(name);
                            storeUserDefaultDataReference.child("user_status").setValue("Hey there, I'm using myChat!");
                            storeUserDefaultDataReference.child("user_image").setValue("default_profile_img");
                            storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

            } else {

                Toast.makeText(RegisterActivity.this,
                        "Password didn't match!", Toast.LENGTH_SHORT).show();

            }

        }
    }
}
