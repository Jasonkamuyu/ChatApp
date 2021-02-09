package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private static final int GalleryPick = 1;

    private StorageReference UserProfileImagesRef;

    private ProgressDialog loadingBar;

    private Toolbar SettingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });
    }


    private void InitializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        SettingsToolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       // Log.d("TAG", "onActivityResult: " + requestCode + " - " + CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
        //if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            //CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
                Uri ImageUri = data.getData();

                CropImage.activity(ImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,  1)
                        .start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    loadingBar.setTitle("Set Profile Image");
                    loadingBar.setMessage("Please wait, your profile image is updating...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    Uri resultUri = result.getUri();
                    final StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

                    filePath.putFile(resultUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String downloadUrl = uri.toString();

                                            rootRef.child("users").child(currentUserID).child("image")
                                                    .setValue(downloadUrl)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(SettingsActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                                loadingBar.dismiss();
                                                            } else {
                                                                String message = task.getException().toString();
                                                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                                loadingBar.dismiss();

                                                            }

                                                        }
                                                    });
                                        }
                                    });

                                }


                            });

                }
//                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                    Exception error = result.getError();
//                    Toast.makeText(SettingsActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
//                }
            }
           // Toast.makeText(this, "requestCode is not CROP_IMAGE_ACTIVITY_REQUEST_CODE", Toast.LENGTH_SHORT).show();

        }
    //}


    private void UpdateSettings() {

        String setName= userName.getText().toString();
        String setStatus= userStatus.getText().toString();

        if(TextUtils.isEmpty(setName)){
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();

        }

        else
            {
                HashMap<String,Object>ProfileMap= new HashMap<>();
                ProfileMap.put("uid",currentUserID);
                ProfileMap.put("name",setName);
                ProfileMap.put("status",setStatus);

                rootRef.child("users").child(currentUserID).updateChildren(ProfileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            SenderUserToMainActivity();
                            Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        }

                        else
                            {
                                String message=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();


                        }
                    }
                });
        }
    }

    private void RetrieveUserInfo() {
        rootRef.child("users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image"))))
                        {
                            String retrieveUserName= snapshot.child("name").getValue().toString();
                            String retrieveStatus= snapshot.child("status").getValue().toString();
                            String retrieveProfileImage= snapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);

                            //Glide.with(SettingsActivity.this).load(retrieveProfileImage).into(userProfileImage);

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);


                        }

                        else if((snapshot.exists()) && (snapshot.hasChild("name")))
                        {

                            String retrieveUserName= snapshot.child("name").getValue().toString();
                            String retrieveStatus= snapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                    }

                    else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please Set and Update your Profile Information", Toast.LENGTH_SHORT).show();
                        
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void SenderUserToMainActivity() {

        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
