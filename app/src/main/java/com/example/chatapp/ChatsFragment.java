package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ChatsFragment extends Fragment {
    private View PrivateChatsView;
    private RecyclerView ChatList;
    private DatabaseReference ChatsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;



    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView=inflater.inflate(R.layout.fragment_chats, container, false);

        ChatList=PrivateChatsView.findViewById(R.id.chats_List);
        ChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        CurrentUserId=mAuth.getCurrentUser().getUid();
        ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserId);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("users");

        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options= new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter= new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
            {
                final String usersIds= getRef(position).getKey();
                final String[] retImage = {"default_image"};

                UsersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                       if(snapshot.exists()){
                           if(snapshot.hasChild("image"))
                           {
                               retImage[0] =snapshot.child("image").getValue().toString();
                               Picasso.get().load(retImage[0]).into(holder.profileImage);
                           }

                           final String retName=snapshot.child("name").getValue().toString();
                           final String userStatus=snapshot.child("status").getValue().toString();

                           holder.userName.setText(retName);


                           if(snapshot.child("userState").hasChild("state")){
                               String state=snapshot.child("userState").child("state").getValue().toString();
                               String date=snapshot.child("userState").child("date").getValue().toString();
                               String time=snapshot.child("userState").child("time").getValue().toString();

                               if(state.equals("online")){
                                   holder.UserStatus.setText("online");

                               }
                               else if(state.equals("offline")){
                                   holder.UserStatus.setText("Last Seen: "+date+ ""+time);

                               }
                           }
                           else
                               {
                                   holder.UserStatus.setText("offline");

                           }


                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v)
                               {
                                   Intent chatsIntent= new Intent(getContext(),ChatActivity.class);
                                   chatsIntent.putExtra("visit_user_id",usersIds);
                                   chatsIntent.putExtra("visit_user_name",retName);
                                   chatsIntent.putExtra("visit_image", retImage[0]);

                                   startActivity(chatsIntent);

                               }
                           });

                       }                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };
        ChatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{


        CircleImageView profileImage;
        TextView UserStatus,userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.users_profile_image);
            UserStatus=itemView.findViewById(R.id.user_status);
            userName=itemView.findViewById(R.id.user_profile_name);
        }
    }
}
