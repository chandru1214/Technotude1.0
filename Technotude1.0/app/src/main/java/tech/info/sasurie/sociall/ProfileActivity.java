package tech.info.sasurie.sociall;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{

    private TextView userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB,userMobile;
    private ImageView userProfImage;
    private FirebaseAuth mAuth;
    private DatabaseReference profileUserRef,FriendsRef,PostsRef;
    private String currentUserId;
    private Button MyPosts,MyFriends;
    private int countFriends=0 , countPosts=0;
    private ImageView EditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();


        currentUserId=mAuth.getCurrentUser().getUid();

        profileUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");

        userProfName=findViewById(R.id.my_profile_full_name);
        userName=findViewById(R.id.my_profile_username);
        userStatus=findViewById(R.id.my_profile_status);

        userGender=findViewById(R.id.my_profile_gender);
        userCountry=findViewById(R.id.my_profile_country);
        userDOB=findViewById(R.id.my_profile_dob);

        userRelation=findViewById(R.id.my_profile_relation);
        userProfImage=findViewById(R.id.my_profile_pic);
        userMobile=findViewById(R.id.my_profile_mobile);
        EditProfile=findViewById(R.id.editProfile);
        MyPosts=findViewById(R.id.my_post_button);
        MyFriends=findViewById(R.id.my_friends_button);


        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,FriendsActivity.class));
            }
        });
        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,MyPostsActivity.class));
            }
        });





        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,SettingsActivity.class));
            }
        });

        try
        {

            profileUserRef.addValueEventListener(new ValueEventListener() {
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

                        Picasso.with(ProfileActivity.this).load(mProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                        userName.setText("@"+mProfileUsername);
                        userProfName.setText(mProfileFullname);
                        userStatus.setText(mProfileStatus);
                        userGender.setText(mProfileGender);
                        userCountry.setText(mProfileCountry);
                        userDOB.setText(mProfileDob);
                        userRelation.setText(mProfileRelation);
                        userMobile.setText(mProfileMobile);



                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        catch (Exception e)
        {
            System.out.print(e);
        }

       /*z FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    countFriends=(int) dataSnapshot.getChildrenCount();
                    MyFriends.setText(Integer.toString(countFriends)+" Friends ");
                }
                else
                {
                    MyFriends.setText("0 Friends");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId+ "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    countPosts=(int) dataSnapshot.getChildrenCount();
                    MyPosts.setText(Integer.toString(countPosts)+" Posts");
                }
                else
                {
                    MyPosts.setText("0 Posts");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


    }
}
