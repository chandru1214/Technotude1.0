package tech.info.sasurie.sociall;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Linus on 17/09/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabase;


    public MessageAdapter(List<Messages> userMessagesList) {

        this.userMessagesList = userMessagesList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_user, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(v);


    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {

        String message_sender_id = mAuth.getCurrentUser().getUid();


        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();

        String fromMessageType = messages.getType();

        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        UsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

             if (dataSnapshot.exists()) {
                 String userName = dataSnapshot.child("fullname").getValue().toString();

                 String userImage = dataSnapshot.child("profileimage").getValue().toString();

                 Picasso.with(holder.userProfileImage.getContext()).load(userImage).placeholder(R.drawable.profile).into(holder.userProfileImage);
             }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text"))
        {

            holder.RecieverMessageText.setVisibility(View.INVISIBLE);
            holder.userProfileImage.setVisibility(View.INVISIBLE);

            if (fromUserId.equals(message_sender_id))
            {

                //holder.messagePicture.setVisibility(View.INVISIBLE);

                holder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_layout);

                holder.SenderMessageText.setTextColor(Color.WHITE);

                holder.SenderMessageText.setGravity(Gravity.LEFT);

                holder.SenderMessageText.setText(messages.getMessage());


            }
            else
            {

                holder.SenderMessageText.setVisibility(View.INVISIBLE);
                holder.RecieverMessageText.setVisibility(View.VISIBLE);
                holder.userProfileImage.setVisibility(View.VISIBLE);
                holder.RecieverMessageText.setBackgroundResource(R.drawable.reciever_message_layout);

                holder.RecieverMessageText.setTextColor(Color.WHITE);

                holder.RecieverMessageText.setGravity(Gravity.LEFT);

                holder.RecieverMessageText.setText(messages.getMessage());


            }


            //holder.messageText.setText(messages.getMessage());


        }
        else
        {

           if (fromUserId.equals(message_sender_id))
           {
               holder.SenderMessageText.setVisibility(View.GONE);
               holder.SenderMessageText.setPadding(0, 0, 0, 0);
               holder.RecieverMessageText.setVisibility(View.GONE);
               holder.userProfileImage.setVisibility(View.INVISIBLE);

               //holder.messagePicture.setForegroundGravity(22);
               holder.messagePicture.setVisibility(View.VISIBLE);


               Picasso.with(holder.userProfileImage.getContext()).load(messages.getMessage()).placeholder(R.drawable.profile).into(holder.messagePicture);
           }
           else
           {

               holder.SenderMessageText.setVisibility(View.GONE);
               holder.SenderMessageText.setPadding(0, 0, 0, 0);
               holder.RecieverMessageText.setVisibility(View.GONE);
               holder.userProfileImage.setVisibility(View.VISIBLE);

               //holder.messagePicture.setForegroundGravity(22);
               holder.messagePicture.setVisibility(View.VISIBLE);


               Picasso.with(holder.userProfileImage.getContext()).load(messages.getMessage()).placeholder(R.drawable.profile).into(holder.messagePicture);

           }


        }


    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView SenderMessageText,RecieverMessageText;
        public CircleImageView userProfileImage;
        public ImageView messagePicture;

        public MessageViewHolder(View view)
        {

            super(view);

            SenderMessageText = view.findViewById(R.id.sender_message_text);
            RecieverMessageText= view.findViewById(R.id.reciever_message_text);
            messagePicture = view.findViewById(R.id.message_image_view);
            userProfileImage = view.findViewById(R.id.messages_profile_images);
        }

    }

}