package tech.info.sasurie.sociall;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private DatabaseReference UserData;
    private RecyclerView usersList;
    private ImageView SearchButton;
    private EditText SearchInputText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UserData= FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar=findViewById(R.id.find_friends_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SearchButton=findViewById(R.id.search_people_friends_button);
        SearchInputText=findViewById(R.id.search_box_input);
        usersList=findViewById(R.id.search_result_list);
        usersList.setHasFixedSize(true);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchUserName=SearchInputText.getText().toString();

                if (TextUtils.isEmpty(searchUserName))
                {

                    Toast.makeText(FindFriendsActivity.this, "Please Enter Your Friends Name", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    SearchForPeopleAndFriends(searchUserName);
                }


            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.all_users_display_layout,
                UsersViewHolder.class,
                UserData


        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setFullname(model.getFullname());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                final String visit_user_id=getRef(position).getKey();

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent=new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };
        usersList.setAdapter(firebaseRecyclerAdapter);


    }

    protected void SearchForPeopleAndFriends(String searchUserName)
    {

        Toast.makeText(this, "Searching ...", Toast.LENGTH_SHORT).show();

        Query searchPeople=UserData.orderByChild("fullname").startAt(searchUserName).endAt(searchUserName + "\uf8ff");


        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.all_users_display_layout,
                UsersViewHolder.class,
                searchPeople


        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setFullname(model.getFullname());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                final String visit_user_id=getRef(position).getKey();

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent=new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };
        usersList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {

        View view;


        public UsersViewHolder(View itemView)
        {
            super(itemView);
            view=itemView;
        }

        public void setFullname(String fullname){

            TextView userNameView=view.findViewById(R.id.all_users_profile_full_name);
            userNameView.setText(fullname);

        }
        public void setStatus(String status) {

            TextView userStatus=view.findViewById(R.id.all_users_status);
            userStatus.setText(status);

        }
        public void setProfileimage(final Context ctx, final String profileimage) {
            final CircleImageView userThumbImage=view.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(userThumbImage);


        }
    }
}
