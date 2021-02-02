package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        rootRef=FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();
    }




    private void InitializeFields() {
        updateAccountSettings=findViewById(R.id.update_settings_button);
        userName=findViewById(R.id.set_user_name);
        userStatus=findViewById(R.id.set_profile_status);
        userProfileImage=findViewById(R.id.set_profile_image);

    }

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

                rootRef.child("users").child(currentUserID).setValue(ProfileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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