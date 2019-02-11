package com.example.touhid.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView profileName;
    private TextView profileStatus;
    private Button changeImageButton;
    private Button changeStatusButton;
    private ProgressDialog progressDialog;

    private final static int Gallery_Pick = 1;

    private StorageReference storeProfileImageStorageReference;
    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;
    private StorageReference thumbImageRef;

    Bitmap thumb_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        mAuth = FirebaseAuth.getInstance();
        String onlineUserID = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(onlineUserID);
        storeProfileImageStorageReference = FirebaseStorage.getInstance().getReference().child("Profile_images");
        thumbImageRef = FirebaseStorage.getInstance().getReference().child("thumb_images");



        profileImage = (CircleImageView) findViewById(R.id.profile_image);
        profileName = (TextView) findViewById(R.id.setting_username_field);
        profileStatus = (TextView) findViewById(R.id.setting_status_field);
        changeImageButton = (Button) findViewById(R.id.change_image_button);
        changeStatusButton = (Button) findViewById(R.id.change_status_button);
        progressDialog = new ProgressDialog(this);



        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();
                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);

                if(!image.equals("default_profile_img")) {
                    Picasso.with(SettingsActivity.this).load(thumbImage).placeholder(R.drawable.default_profile_img).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        changeImageButton.setOnClickListener(new View.OnClickListener() { //access gallery code
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");  //what type of file will be accessed
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);   //access phone gallery
                startActivityForResult(galleryIntent, Gallery_Pick);  //how many images can be selected
            }
        });



        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldStatus = profileStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("user_status", oldStatus);
                startActivity(statusIntent);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //image cropping code
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { //crop button clicked
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {  //if cropping done

                progressDialog.setTitle("Updating profile picture");
                progressDialog.setMessage("Please wait..");
                progressDialog.show();

                final String userID = mAuth.getCurrentUser().getUid();
                Uri resultUri = result.getUri();



                //compress the profile image - start
                File thumb_file_path_URI = new File(resultUri.getPath()); //getting the image path
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_file_path_URI);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray(); //image compressed
                //compress the profile image - finish




                final StorageReference filePath = storeProfileImageStorageReference.child(userID+".jpg");
                final StorageReference thumb_filePath = thumbImageRef.child(userID+".jpg");



                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_file_task) {

                                    if(thumb_file_task.isSuccessful()) {
                                        String thumb_downloadURL = thumb_file_task.getResult().getDownloadUrl().toString();

                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image", downloadUrl);
                                        update_user_data.put("user_thumb_image", thumb_downloadURL);

                                        getUserDataReference.updateChildren(update_user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> database_task) {
                                                        if(database_task.isSuccessful()) {
                                                            Toast.makeText(SettingsActivity.this,
                                                                    "Profile picture updated!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(SettingsActivity.this,
                                                                    "Database error!", Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();
                                                        }
                                                        progressDialog.dismiss();
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(SettingsActivity.this,
                                                "Server interaction problem in saving the thumb image!", Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });


                        } else {

                            Toast.makeText(SettingsActivity.this,
                                    "Server interaction problem in saving the image!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}
