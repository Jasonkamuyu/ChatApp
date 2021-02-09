package com.example.chatapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private List<Messages> usermessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> usermessagesList){
        this.usermessagesList=usermessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText=itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_image_view);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);

        mAuth=FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messsageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = usermessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();


        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")) {
                    String receiverImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {

            if (fromUserID.equals(messsageSenderID)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.senders_messages_layout);
                holder.senderMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + "-" + messages.getDate());
            }

            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiever_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + "-" + messages.getDate());


            }
        }

        else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messsageSenderID)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);


            }
        }

        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserID.equals(messsageSenderID)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-7d5bb.appspot.com/o/file.png?alt=media&token=65e00551-8ad5-46e9-a843-4033935633a4")
                        .into(holder.messageSenderPicture);


            }

            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-7d5bb.appspot.com/o/file.png?alt=media&token=65e00551-8ad5-46e9-a843-4033935633a4")
                        .into(holder.messageReceiverPicture);


            }
        }


        if (fromUserID.equals(messsageSenderID)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (usermessagesList.get(position).getType().equals("pdf") || usermessagesList.get(position).getType().equals("docx")) ;
                    {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Delete For Me",
                                        "Download and View this Document",
                                        "Cancel",
                                        "Delete For Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                if (which == 0)
                                {
                                    DeleteSentMessage(position,holder);

                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if (which == 1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usermessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if (which == 3)
                                {
                                    DeleteSentMessageForEveryone(position,holder);

                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }

                   if (usermessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Delete For Me",
                                        "Cancel",
                                        "Delete For Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    DeleteSentMessage(position,holder);

                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (which == 2)
                                {
                                    DeleteSentMessageForEveryone(position,holder);

                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                }
                            }
                        });

                        builder.show();

                    }
                  else if (usermessagesList.get(position).getType().equals("image")) ;
                    {
                        CharSequence[] options = new CharSequence[]
                                {
                                        "Delete For Me",
                                        "View This Image",
                                        "Cancel",
                                        "Delete For Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0)
                                {
                                    DeleteSentMessage(position,holder);

                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (which == 1)
                                {
                                    Intent intent= new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",usermessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if (which == 3)
                                {
                                    DeleteSentMessageForEveryone(position,holder);

                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                }
                            }
                        });

                        builder.show();

                    }
                }
            });

        } else
            {


            if (fromUserID.equals(messsageSenderID)) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (usermessagesList.get(position).getType().equals("pdf") || usermessagesList.get(position).getType().equals("docx"))
                            ;
                        {
                            CharSequence[] options = new CharSequence[]
                                    {
                                            "Delete For Me",
                                            "Download and View this Document",
                                            "Cancel",
                                    };

                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0)
                                    {
                                        DeleteReceivedMessage(position,holder);

                                        Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);

                                    }

                                    else if (which == 1) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usermessagesList.get(position).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });

                            builder.show();

                        }

                        if (usermessagesList.get(position).getType().equals("text")) ;
                        {
                            CharSequence[] options = new CharSequence[]
                                    {
                                            "Delete For Me",
                                            "Cancel",

                                    };

                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0)
                                    {
                                        DeleteReceivedMessage(position,holder);

                                        Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);


                                    }
                                }
                            });

                            builder.show();

                        }
                        if (usermessagesList.get(position).getType().equals("image")) ;
                        {
                            CharSequence[] options = new CharSequence[]
                                    {
                                            "Delete For Me",
                                            "View This Image",
                                            "Cancel",
                                    };

                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Delete Message?");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0)
                                    {
                                        DeleteReceivedMessage(position,holder);

                                        Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);


                                    }

                                    else if (which == 1)
                                    {

                                        Intent intent= new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                        intent.putExtra("url",usermessagesList.get(position).getMessage());
                                        holder.itemView.getContext().startActivity(intent);

                                    }
                                }
                            });

                            builder.show();

                        }
                    }
                });

            }

        }
    }



    @Override
    public int getItemCount() {
        return usermessagesList.size();
    }

    private void DeleteSentMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootref= FirebaseDatabase.getInstance().getReference();
        rootref.child("Messages")
                .child(usermessagesList.get(position).getFrom())
                .child(usermessagesList.get(position).getTo())
                .child(usermessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void>   task)
            {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();


                }
            }
        });

    }

    private void DeleteReceivedMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootref= FirebaseDatabase.getInstance().getReference();
        rootref.child("Messages")
                .child(usermessagesList.get(position).getTo())
                .child(usermessagesList.get(position).getFrom())
                .child(usermessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void>   task)
            {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();


                }
            }
        });

    }

    private void DeleteSentMessageForEveryone(final int position, final MessageViewHolder holder)
    {
        final DatabaseReference rootref= FirebaseDatabase.getInstance().getReference();
        rootref.child("Messages")
                .child(usermessagesList.get(position).getTo())
                .child(usermessagesList.get(position).getFrom())
                .child(usermessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void>   task)
            {
                if(task.isSuccessful()){
                    rootref.child("Messages")
                            .child(usermessagesList.get(position).getFrom())
                            .child(usermessagesList.get(position).getTo())
                            .child(usermessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();


                }
            }
        });

    }



}
