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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference changeStatusRef;

    private Toolbar mToolbar;
    private EditText editStatusField;
    private Button changeStatusButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editStatusField = (EditText) findViewById(R.id.edit_status_field);
        changeStatusButton = (Button) findViewById(R.id.save_status_button);
        progressDialog = new ProgressDialog(this);

        String oldStatus = getIntent().getExtras().get("user_status").toString();
        editStatusField.setText(oldStatus);

        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = editStatusField.getText().toString();

                changeStatusUpdate(newStatus);
            }
        });
    }

    private void changeStatusUpdate(String newStatus) {
        if(TextUtils.isEmpty(newStatus)) {
            Toast.makeText(StatusActivity.this,
                    "Please write something!", Toast.LENGTH_SHORT).show();
        } else {

            progressDialog.setTitle("Changing status");
            progressDialog.setMessage("Please wait while we are changing your status update");
            progressDialog.show();

            changeStatusRef.child("user_status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        progressDialog.dismiss();
                        Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);

                        Toast.makeText(StatusActivity.this,
                                "Status updated!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(StatusActivity.this,
                                "Status update failed!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
