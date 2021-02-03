package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
//import android.widget.Toolbar;



public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;

    private FirebaseAuth mAuth;

    private DatabaseReference usersRef,groupNameRef,groupMessageKeyRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);





        InitializeFields();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageInfoToDatabase();
                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @Override
    protected void onStart()
    {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }

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
    }



    private void InitializeFields() {

        mToolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton=findViewById(R.id.send_message_button);
        userMessageInput=findViewById(R.id.input_group_message);
        mScrollView=findViewById(R.id.my_scroll_view);
        displayTextMessages=findViewById(R.id.group_chat_text_display);

    }

    private void GetUserInfo()
    {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    currentUserName=snapshot.child("name").getValue().toString();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SendMessageInfoToDatabase()
    {
        String message= userMessageInput.getText().toString();
        String messageKey= groupNameRef.push().getKey();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please write Message First", Toast.LENGTH_SHORT).show();

        }
    else{
            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat currrentDateFormat= new SimpleDateFormat("MMM dd, yyyy");
            currentDate= currrentDateFormat.format(calForDate.getTime());

            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat currrentTimeFormat= new SimpleDateFormat("hh:mm a");
            currentTime= currrentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object>groupMessageKey= new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String,Object>MessageInfoMap=new HashMap<>();
            MessageInfoMap.put("name",currentUserName);
            MessageInfoMap.put("message",message);
            MessageInfoMap.put("date",currentDate);
            MessageInfoMap.put("time",currentTime);

            groupMessageKeyRef.updateChildren(MessageInfoMap);


        }
    }

    private void DisplayMessages(DataSnapshot snapshot)
    {
        Iterator iterator=snapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatDate= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime= (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName+ ":\n" +chatMessage+":\n"+chatTime+"  "+chatDate+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }    }
}
