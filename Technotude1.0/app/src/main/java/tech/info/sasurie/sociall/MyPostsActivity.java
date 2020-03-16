package tech.info.sasurie.sociall;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private RecyclerView MyPostList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference PostsRef,LikesRef;
    Boolean LikeChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar=findViewById(R.id.my_posts_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MyPostList=findViewById(R.id.myPost_list);
        MyPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        MyPostList.setLayoutManager(linearLayoutManager);

        DisplayMyPosts();

    }

    private void DisplayMyPosts()
    {
        Query myPost=PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId+ "\uf8ff");

        FirebaseRecyclerAdapter<Posts, MyPostViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, MyPostViewHolder>
                (
                        Posts.class,
                        R.layout.all_posts_layout,
                        MyPostViewHolder.class,
                        myPost
                )
        {
            @Override
            protected void populateViewHolder(MyPostViewHolder viewHolder, Posts model, int position)
            {

                final String PostKey=getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(), model.getPostimage());
                viewHolder.setLikeButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MyPostsActivity.this,ClickPostActivity.class).putExtra("PostKey",PostKey));
                    }
                });

                viewHolder.CommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(MyPostsActivity.this,CommentsActivity.class).putExtra("PostKey",PostKey));

                    }
                });


                viewHolder.LikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        LikeChecker=true;

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (LikeChecker.equals(true))
                                {

                                    if (dataSnapshot.child(PostKey).hasChild(currentUserId))
                                    {

                                        LikesRef.child(PostKey).child(currentUserId).removeValue();
                                        LikeChecker=false;


                                    }
                                    else
                                    {

                                        LikesRef.child(PostKey).child(currentUserId).setValue(true);
                                        LikeChecker=false;



                                    }

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });



            }
        };
        MyPostList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class MyPostViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        ImageView LikeButton,CommentButton;
        TextView LikesNo;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public MyPostViewHolder(View itemView)
        {
            super(itemView);
            mView=itemView;

            LikeButton=(ImageView) mView.findViewById(R.id.like_button);
            CommentButton= (ImageView) mView.findViewById(R.id.comment_button);
            LikesNo=(TextView) mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();

        }


        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.click_post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }

        public void setLikeButtonStatus(final String PostKey)
        {


            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {

                        countLikes=(int)  dataSnapshot.child(PostKey).getChildrenCount();

                        LikeButton.setImageResource(R.drawable.afterlike);

                        LikesNo.setText(Integer.toString(countLikes)+(" "));

                    }else
                    {

                        countLikes=(int)  dataSnapshot.child(PostKey).getChildrenCount();

                        LikeButton.setImageResource(R.drawable.beforelike);

                        LikesNo.setText(Integer.toString(countLikes)+(" "));


                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }
}
