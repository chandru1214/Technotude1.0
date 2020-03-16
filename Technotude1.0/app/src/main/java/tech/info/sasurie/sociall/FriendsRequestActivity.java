package tech.info.sasurie.sociall;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FriendsRequestActivity extends AppCompatActivity
{
    private RecyclerView Request_list;
    private DatabaseReference FriendsRequest;
    private FirebaseAuth mAuth;

    private String online_user_id;

    private DatabaseReference UsersDatabase;

    private DatabaseReference FriendsDatabaseRef;
    private DatabaseReference FriendsRequestDatabaseRef;

    private Toolbar mToolbar;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_request);


        mAuth=FirebaseAuth.getInstance();
        MobileAds.initialize(this, MainActivity.APP_ID);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        online_user_id=mAuth.getCurrentUser().getUid();

        FriendsRequest= FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(online_user_id);
        UsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");

        FriendsDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRequestDatabaseRef=FirebaseDatabase.getInstance().getReference().child("FriendRequests");


        Request_list=findViewById(R.id.request_list);

        mToolbar=findViewById(R.id.request_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friend Request / You Given");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Request_list.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        Request_list.setLayoutManager(linearLayoutManager);


    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests,RequestViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(

                Requests.class,
                R.layout.friend_request_layout,
                RequestViewHolder.class,
                FriendsRequest


        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position) {

                final String list_user_id=getRef(position).getKey();

                DatabaseReference get_type_ref=getRef(position).child("request_type").getRef();

                get_type_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        if (dataSnapshot.exists()){

                            String request_type=dataSnapshot.getValue().toString();

                            if (request_type.equals("received"))
                            {


                                UsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {


                                       if (dataSnapshot.exists())
                                       {
                                           final String userName=dataSnapshot.child("fullname").getValue().toString();
                                           final String userImage=dataSnapshot.child("profileimage").getValue().toString();

                                           final String userStatus=dataSnapshot.child("status").getValue().toString();

                                           viewHolder.setUserName(userName);

                                           viewHolder.setProfileimage(userImage,getApplicationContext());
                                           viewHolder.setUserStatus(userStatus);

                                           viewHolder.userNameDisplay.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {

                                                   Intent profileIntent=new Intent(FriendsRequestActivity.this,PersonProfileActivity.class);
                                                   profileIntent.putExtra("visit_user_id",list_user_id);
                                                   startActivity(profileIntent);

                                               }
                                           });


                                           viewHolder.AcceptButton.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {

                                                   Calendar calFordDate = Calendar.getInstance();
                                                   SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                   final String saveCurrentDate = currentDate.format(calFordDate.getTime());
                                                   FriendsDatabaseRef.child(online_user_id).child(list_user_id).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                       @Override
                                                       public void onSuccess(Void aVoid) {


                                                           FriendsDatabaseRef.child(list_user_id).child(online_user_id).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                               @Override
                                                               public void onSuccess(Void aVoid) {
                                                                   FriendsRequestDatabaseRef.child(online_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {

                                                                           if (task.isSuccessful()){


                                                                               FriendsRequestDatabaseRef.child(list_user_id).child(online_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                   @Override
                                                                                   public void onComplete(@NonNull Task<Void> task) {

                                                                                       if (task.isSuccessful()){


                                                                                           Toast.makeText(FriendsRequestActivity.this, "Friend Request Accepted ", Toast.LENGTH_SHORT).show();


                                                                                       }


                                                                                   }
                                                                               });




                                                                           }

                                                                       }
                                                                   });


                                                               }
                                                           });

                                                       }
                                                   });
                                               }
                                           });

                                           viewHolder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   FriendsRequestDatabaseRef.child(online_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {

                                                           if (task.isSuccessful()){


                                                               FriendsRequestDatabaseRef.child(list_user_id).child(online_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<Void> task) {

                                                                       if (task.isSuccessful()){


                                                                           Toast.makeText(FriendsRequestActivity.this, "Friend Request Rejected", Toast.LENGTH_SHORT).show();

                                                                       }


                                                                   }
                                                               });




                                                           }

                                                       }
                                                   });

                                               }
                                           });


                                       }



                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });




                            }
                            else if (request_type.equals("sent"))
                            {


                                UsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        viewHolder.AcceptButton.setText("Cancel Friend Request");
                                        viewHolder.AcceptButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                                        viewHolder.CancelButton.setVisibility(View.GONE);

                                        final String userName=dataSnapshot.child("fullname").getValue().toString();
                                        final String userImage=dataSnapshot.child("profileimage").getValue().toString();

                                        final String userStatus=dataSnapshot.child("status").getValue().toString();

                                        viewHolder.setUserName(userName);

                                        viewHolder.setProfileimage(userImage,getApplicationContext());
                                        viewHolder.setUserStatus(userStatus);


                                        viewHolder.userNameDisplay.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                Intent profileIntent=new Intent(FriendsRequestActivity.this,PersonProfileActivity.class);
                                                profileIntent.putExtra("visit_user_id",list_user_id);
                                                startActivity(profileIntent);

                                            }
                                        });


                                        viewHolder.AcceptButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                FriendsRequestDatabaseRef.child(online_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){


                                                            FriendsRequestDatabaseRef.child(list_user_id).child(online_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){


                                                                        Toast.makeText(FriendsRequestActivity.this, "Friend Request Cancelled", Toast.LENGTH_SHORT).show();

                                                                    }


                                                                }
                                                            });




                                                        }

                                                    }
                                                });

                                            }
                                        });





                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });




                            }


                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });





            }
        };

        Request_list.setAdapter(firebaseRecyclerAdapter);

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {

        View view;
        TextView userNameDisplay;

        Button AcceptButton,CancelButton;


        public RequestViewHolder(View itemView)
        {

            super(itemView);

            view=itemView;

            AcceptButton=(Button) view.findViewById(R.id.request_accept_btn);
            CancelButton=(Button) view.findViewById(R.id.request_decline_btn);
            userNameDisplay=view.findViewById(R.id.request_profile_name);

        }

        public void setUserName(String userName)
        {

            userNameDisplay.setText(userName);

        }



        public void setUserStatus(String userStatus)
        {

            TextView status=view.findViewById(R.id.request_profile_status);
            status.setText(userStatus);


        }

        public void setProfileimage(final String thumbImage, final Context ctx)
        {
            final ImageView userThumbImage=view.findViewById(R.id.request_profile_image);
            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.profile).into(userThumbImage);

        }
    }
}
