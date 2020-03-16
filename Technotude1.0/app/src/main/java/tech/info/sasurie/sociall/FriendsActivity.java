package tech.info.sasurie.sociall;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{

    private RecyclerView FriendsList;
    private DatabaseReference FriendsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;
    private Toolbar mToolbar;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        MobileAds.initialize(this, MainActivity.APP_ID);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAuth=FirebaseAuth.getInstance();
        online_user_id=mAuth.getCurrentUser().getUid();

        FriendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        FriendsList=findViewById(R.id.friends_list);

        mToolbar=findViewById(R.id.friends_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        FriendsList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();

    }

    public void updateUserStatus(String state)
    {
        String saveCurrentDate,saveCurrentTime;

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        Map currentUserStatus=new HashMap();
        currentUserStatus.put("time",saveCurrentTime);
        currentUserStatus.put("date",saveCurrentDate);
        currentUserStatus.put("type",state);

        UsersRef.child(online_user_id).child("userState").updateChildren(currentUserStatus);

    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (
                        Friends.class,
                        R.layout.users_layout,
                        FriendsViewHolder.class,
                        FriendsRef

                )
        {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position)
            {

                viewHolder.setDate(model.getDate());

                final String usersId=getRef(position).getKey();
                UsersRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            final String userName=dataSnapshot.child("fullname").getValue().toString();
                            String profileImage=dataSnapshot.child("profileimage").getValue().toString();

                            final String type;

                            if (dataSnapshot.hasChild("userState"))
                            {
                                type=dataSnapshot.child("userState").child("type").getValue().toString();

                                if (type.equals("online"))
                                {
                                    viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }

                            }


                            viewHolder.setFullname(userName);
                            viewHolder.setProfileimage(getApplicationContext(),profileImage);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[]=new CharSequence[]
                                            {
                                                    userName+"'s Profile",
                                                    "Send Message"
                                            };
                                    AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which==0)
                                            {
                                                startActivity(new Intent(FriendsActivity.this,PersonProfileActivity.class).putExtra("visit_user_id",usersId));
                                            }
                                            if (which==1)
                                            {
                                                startActivity(new Intent(FriendsActivity.this,ChatActivity.class).putExtra("visit_user_id",usersId).putExtra("userName",userName));
                                            }
                                        }
                                    });
                                    builder.show();
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

        };
        updateUserStatus("online");
        FriendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageView onlineStatusView;

        public FriendsViewHolder(View itemView)
        {
            super(itemView);

            mView=itemView;

            onlineStatusView=mView.findViewById(R.id.all_users_online_icon);

        }
        public void setFullname(String fullname){

            TextView userNameView=mView.findViewById(R.id.all_users_profile_full_name);
            userNameView.setText(fullname);

        }
        public void setDate(String date) {

            TextView userStatus=mView.findViewById(R.id.all_users_status);
            userStatus.setText("Friends Since: "+date);

        }
        public void setProfileimage(final Context ctx, final String profileimage) {
            final CircleImageView userThumbImage=mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(userThumbImage);



        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserStatus("online");

    }
}
