package tech.info.sasurie.sociall;
import android.content.Context;
import android.support.annotation.NonNull;
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
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity
{
    private ImageView PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;

    private String Post_Key , current_user_id;
    private DatabaseReference UsersRef , PostsRef;

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth=FirebaseAuth.getInstance();

        current_user_id=mAuth.getCurrentUser().getUid();

        Post_Key=getIntent().getExtras().get("PostKey").toString();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        CommentsList=findViewById(R.id.comments_list);

        mToolbar=findViewById(R.id.comments_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText=findViewById(R.id.comment_input);
        PostCommentButton=findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {

                            String userName=dataSnapshot.child("username").getValue().toString();
                            String userImage=dataSnapshot.child("profileimage").getValue().toString();

                            ValidateComment(userName,userImage);



                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(Comments.class, R.layout.all_comments_layout, CommentsViewHolder.class, PostsRef) {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position) {


                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());


            }
        };

        CommentsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(View itemView)
        {
            super(itemView);

            mView=itemView;
        }

        public void setUsername(String username)
        {

            TextView myUserName=mView.findViewById(R.id.comment_username);
            myUserName.setText("@"+username+"   ");

        }

        public void setComment(String comment)
        {

            TextView myComment=mView.findViewById(R.id.comment_text);
            myComment.setText(comment);


        }

        public void setDate(String date)
        {

            TextView myDate=mView.findViewById(R.id.comment_date);
            myDate.setText(" Date: "+date);


        }

        public void setTime(String time)
        {

            TextView myTime=mView.findViewById(R.id.comment_time);
            myTime.setText(" Time: "+time);

        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView userImage=mView.findViewById(R.id.comment_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(userImage);
        }

    }


    private void ValidateComment(String userName, String userImage)
    {

        String CommentText=CommentInputText.getText().toString();

        if (TextUtils.isEmpty(CommentText))
        {

            Toast.makeText(this, "Please write an comment", Toast.LENGTH_SHORT).show();

        }
        else
        {


            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
            final String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final String RandomKey= current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap=new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",CommentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);
            commentsMap.put("profileimage",userImage);

            PostsRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {

                        CommentInputText.setText("  ");
                        Toast.makeText(CommentsActivity.this, "Thanks For Your Feedback", Toast.LENGTH_SHORT).show();


                    }
                    else
                    {

                        Toast.makeText(CommentsActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            });


        }

    }
}
