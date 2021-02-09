package com.example.chatapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;

    private FirebaseAuth mAuth;

    private DatabaseReference ChatRequestRef,usersRef,contactsRef;
    private String currentUserID;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView= inflater.inflate(R.layout.fragment_requests, container, false);

        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        usersRef=FirebaseDatabase.getInstance().getReference().child("users");

        myRequestsList=RequestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options= new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
                    {
                       holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String listUser_ID= getRef(position).getKey();

                        DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists()){
                                    String type = snapshot.getValue().toString();

                                    if(type.equals("receieved"))

                                    {
                                        usersRef.child(listUser_ID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                if(snapshot.hasChild("image"))
                                                {

                                                    final String requestUsername =snapshot.child("name").getValue().toString();
                                                    final String requestUserStatus =snapshot.child("status").getValue().toString();
                                                    final String requestProfileImage =snapshot.child("image").getValue().toString();



                                                    holder.userName.setText(requestUsername);
                                                    holder.userStatus.setText("Wants to Connect with You");
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                                }

                                                else {
                                                    final String requestUsername = snapshot.child("name").getValue().toString();
                                                    final String requestUserStatus = snapshot.child("status").getValue().toString();

                                                    holder.userName.setText(requestUsername);
                                                    holder.userStatus.setText("Wants to Connect with You");

                                                }


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]= new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"

                                                        };

                                                        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                                                        builder.setTitle("New Chat Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {
                                                                if (which==0)
                                                                {
                                                                    contactsRef.child(currentUserID).child(listUser_ID).child("Contact")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful()){

                                                                                contactsRef.child(listUser_ID).child(currentUserID).child("Contact")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful()){

                                                                                            ChatRequestRef.child(currentUserID).child(listUser_ID)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){

                                                                                                                ChatRequestRef.child(listUser_ID).child(currentUserID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                if(task.isSuccessful()){

                                                                                                                                    Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                                    });


                                                                }

                                                                if(which==1)
                                                                {
                                                                    ChatRequestRef.child(currentUserID).child(listUser_ID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){

                                                                                        ChatRequestRef.child(listUser_ID).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if(task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                        }

                                                                                                    }
                                                                                                });

                                                                                    }
                                                                                }
                                                                            });


                                                                }


                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }

                                    else if(type.equals("sent"))
                                    {
                                        Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_sent_btn.setText("Request Sent");

                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                        usersRef.child(listUser_ID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                if(snapshot.hasChild("image"))
                                                {

                                                    final String requestUsername =snapshot.child("name").getValue().toString();
                                                    final String requestUserStatus =snapshot.child("status").getValue().toString();
                                                    final String requestProfileImage =snapshot.child("image").getValue().toString();



                                                    holder.userName.setText(requestUsername);
                                                    holder.userStatus.setText("you have sent a request to "+requestUsername);
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                                }

                                                else {
                                                    final String requestUsername = snapshot.child("name").getValue().toString();
                                                    final String requestUserStatus = snapshot.child("status").getValue().toString();

                                                    holder.userName.setText(requestUsername);
                                                    holder.userStatus.setText("Wants to Connect with You");

                                                }


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[]= new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"

                                                                };

                                                        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already Sent Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {


                                                                if(which==0)
                                                                {
                                                                    ChatRequestRef.child(currentUserID).child(listUser_ID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){

                                                                                        ChatRequestRef.child(listUser_ID).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if(task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(), "You have cancelled the chat Request", Toast.LENGTH_SHORT).show();
                                                                                                        }

                                                                                                    }
                                                                                                });

                                                                                    }
                                                                                }
                                                                            });


                                                                }


                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                      View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                      RequestViewHolder holder= new RequestViewHolder(view);
                      return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_btn);
            CancelButton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
