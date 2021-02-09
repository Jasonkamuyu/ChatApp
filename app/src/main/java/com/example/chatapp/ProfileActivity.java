package com.example.chatapp;

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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String ReceiverUserID,senderuserID,current_state;

    private CircleImageView userprofileimage;
    private TextView userprofilename,profilestatus;
    private Button sendmessagerequestbutton,declinerequestbutton;

    private DatabaseReference userRef,chatRequestRef,contactsRef,notificationRef;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ReceiverUserID= getIntent().getExtras().get("visit_user_id").toString();


        userprofileimage=findViewById(R.id.visit_profile_image);
        userprofilename=findViewById(R.id.visit_user_name);
        profilestatus=findViewById(R.id.user_profile_status);
        userprofileimage=findViewById(R.id.visit_profile_image);
        sendmessagerequestbutton=findViewById(R.id.send_message_request_button);
        declinerequestbutton=findViewById(R.id.decline_message_request_button);

        userRef= FirebaseDatabase.getInstance().getReference().child("users");
        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");
        mAuth=FirebaseAuth.getInstance();

        senderuserID=mAuth.getCurrentUser().getUid();
        current_state="new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {

        userRef.child(ReceiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() &&(snapshot.hasChild("image"))){
                    String userImage= snapshot.child("image").getValue().toString();
                    String userName= snapshot.child("name").getValue().toString();
                    String userStatus= snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userprofileimage);
                    userprofilename.setText(userName);
                    profilestatus.setText(userStatus);

                    ManageChatRequests();

                }

            else{

                    String userName= snapshot.child("name").getValue().toString();
                    String userStatus= snapshot.child("status").getValue().toString();

                    userprofilename.setText(userName);
                    profilestatus.setText(userStatus);

                    ManageChatRequests();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests()
    {

        chatRequestRef.child(senderuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.hasChild(ReceiverUserID)){
                     String requestType=snapshot.child(ReceiverUserID).child("request_type").getValue().toString();

                     if(requestType.equals("sent")){
                         current_state="request_sent";
                         sendmessagerequestbutton.setText("Cancel Chat Request");

                     }
                else if(requestType.equals("receieved")){
                    current_state="request_received";
                    sendmessagerequestbutton.setText("Accept Chat Request");
                    declinerequestbutton.setVisibility(View.VISIBLE);
                    declinerequestbutton.setEnabled(true);

                    declinerequestbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CancelChatRequest();
                        }
                    });




                     }
                }
                else{
                    contactsRef.child(senderuserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(ReceiverUserID)){
                                        current_state="friends";
                                        sendmessagerequestbutton.setText("Remove this Contact");

                                    }                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(!senderuserID.equals(ReceiverUserID)){

            sendmessagerequestbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    sendmessagerequestbutton.setEnabled(false);

                    if(current_state.equals("new")){
                        SendChatRequest();

                    }

                    if(current_state.equals("request_sent")){
                        CancelChatRequest();

                    }
                    if(current_state.equals("request_received")){
                        AcceptChatRequest();

                    }

                    if(current_state.equals("friends")){
                        RemoveSpecificContact();

                    }


                }
            });

        }

        else
            {
            sendmessagerequestbutton.setVisibility(View.INVISIBLE);
        }


    }

    private void RemoveSpecificContact()
    {
        contactsRef.child(senderuserID).child(ReceiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful()){
                    contactsRef.child(ReceiverUserID).child(senderuserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful()){
                                sendmessagerequestbutton.setEnabled(true);
                                current_state="new";
                                sendmessagerequestbutton.setText("Send Message");
                                declinerequestbutton.setVisibility(View.INVISIBLE);
                                declinerequestbutton.setEnabled(false);

                            }                        }
                    });

                }
            }
        });

    }

    private void AcceptChatRequest() {
        contactsRef.child(senderuserID).child(ReceiverUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(ReceiverUserID).child(senderuserID).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                chatRequestRef.child(senderuserID).child(ReceiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful()){

                                                            chatRequestRef.child(ReceiverUserID).child(senderuserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    sendmessagerequestbutton.setEnabled(true);
                                                                    current_state="friends";
                                                                    sendmessagerequestbutton.setText("Remove this Contact");
                                                                    declinerequestbutton.setVisibility(View.INVISIBLE);
                                                                    declinerequestbutton.setEnabled(false);


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
        chatRequestRef.child(senderuserID).child(ReceiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful()){
                    chatRequestRef.child(ReceiverUserID).child(senderuserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful()){
                                sendmessagerequestbutton.setEnabled(true);
                                current_state="new";
                                sendmessagerequestbutton.setText("Send Message");
                                declinerequestbutton.setVisibility(View.INVISIBLE);
                                declinerequestbutton.setEnabled(false);

                            }                        }
                    });

                }
            }
        });
    }

    private void SendChatRequest()
    {
        chatRequestRef.child(senderuserID).child(ReceiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(ReceiverUserID).child(senderuserID)
                                    .child("request_type").setValue("receieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful()){

                                                HashMap<String,String> chatNotificationMap= new HashMap<>();
                                                chatNotificationMap.put("from",senderuserID);
                                                chatNotificationMap.put("type","request");
                                                notificationRef.child(ReceiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful()){
                                                                    sendmessagerequestbutton.setEnabled(true);
                                                                    current_state="request_sent";
                                                                    sendmessagerequestbutton.setText("Cancel Chat Request");

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
