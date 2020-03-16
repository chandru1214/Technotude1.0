package tech.info.sasurie.sociall;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity
{

    private TextView userName,userProfName,userMobile,userStatus;
    private ImageView userProfImage;
    private Button SendReqButton,DeclineReqButton;
    private FirebaseAuth mAuth;
    private DatabaseReference profileUserRef,UsersRef,FriendRequestRef,FriendsRef;
    private String senderUserId,receiverUserId,CURRENT_STATE;
    private String saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);


        mAuth=FirebaseAuth.getInstance();


        senderUserId=mAuth.getCurrentUser().getUid();

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();

        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef=FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");

        userName=findViewById(R.id.person_profile_username);
        userProfName=findViewById(R.id.person_profile_full_name);
        userMobile=findViewById(R.id.person_profile_mobile);
        userStatus=findViewById(R.id.person_profile_status);


        userProfImage=findViewById(R.id.person_profile_pic);

        SendReqButton=findViewById(R.id.person_send_friend_request_btn);
        DeclineReqButton=findViewById(R.id.person_decline_friend_request_btn);

        DeclineReqButton.setVisibility(View.INVISIBLE);
        DeclineReqButton.setEnabled(false);

        CURRENT_STATE="not_friends";

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.exists())
                {

                    String mProfileImage=dataSnapshot.child("profileimage").getValue().toString();
                    String mProfileUsername=dataSnapshot.child("username").getValue().toString();
                    String mProfileFullname=dataSnapshot.child("fullname").getValue().toString();
                    String mProfileGender=dataSnapshot.child("gender").getValue().toString();
                    String mProfileStatus=dataSnapshot.child("status").getValue().toString();
                    String mProfileDob=dataSnapshot.child("dob").getValue().toString();
                    String mProfileCountry=dataSnapshot.child("country").getValue().toString();
                    String mProfileRelation=dataSnapshot.child("relationshipstatus").getValue().toString();
                    String mProfileMobile=dataSnapshot.child("mobile").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(mProfileImage).placeholder(R.drawable.profile).into(userProfImage);


                    userName.setText("@"+mProfileUsername);
                    userProfName.setText(mProfileFullname);
                    userStatus.setText(mProfileStatus);
                    userMobile.setText(mProfileMobile);


                    MaintainButton();



                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (!senderUserId.equals(receiverUserId))
        {
            SendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendReqButton.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends"))
                    {
                        UnFriendPerson();
                    }

                }
            });
        }
        else
        {
            DeclineReqButton.setVisibility(View.INVISIBLE);
            SendReqButton.setVisibility(View.INVISIBLE);
        }

    }

    private void UnFriendPerson()
    {
        FriendsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    FriendsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                SendReqButton.setEnabled(true);
                                CURRENT_STATE="not_friends";
                                SendReqButton.setText("Send Friend Request");
                                DeclineReqButton.setEnabled(false);
                                DeclineReqButton.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptFriendRequest()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                FriendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            FriendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        SendReqButton.setEnabled(true);
                                                        CURRENT_STATE="friends";
                                                        SendReqButton.setText("UnFriend");
                                                        DeclineReqButton.setEnabled(false);
                                                        DeclineReqButton.setVisibility(View.INVISIBLE);


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

    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    FriendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                SendReqButton.setEnabled(true);
                                CURRENT_STATE="not_friends";
                                SendReqButton.setText("Send Friend Request");
                                DeclineReqButton.setEnabled(false);
                                DeclineReqButton.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void MaintainButton()
    {
        FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(receiverUserId))
                {
                    String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent"))
                    {
                        CURRENT_STATE="request_sent";
                        SendReqButton.setText("Cancel Friend Request");
                        SendReqButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        DeclineReqButton.setEnabled(false);
                        DeclineReqButton.setVisibility(View.INVISIBLE);
                    }
                    else if (request_type.equals("received"))
                    {
                        CURRENT_STATE="request_received";
                        SendReqButton.setText("Accept Friend Request");
                        DeclineReqButton.setEnabled(true);
                        DeclineReqButton.setVisibility(View.VISIBLE);

                        DeclineReqButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }

                }
                else
                {
                    FriendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(receiverUserId))
                            {
                                CURRENT_STATE="friends";
                                SendReqButton.setText("UnFriend");

                                DeclineReqButton.setVisibility(View.INVISIBLE);
                                DeclineReqButton.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void SendFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    FriendRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                SendReqButton.setEnabled(true);
                                CURRENT_STATE="request_sent";
                                SendReqButton.setText("Cancel Friend Request");
                                SendReqButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                DeclineReqButton.setEnabled(false);
                                DeclineReqButton.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }
}
