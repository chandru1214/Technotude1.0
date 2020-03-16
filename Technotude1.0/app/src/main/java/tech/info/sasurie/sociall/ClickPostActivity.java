package tech.info.sasurie.sociall;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity
{

    private ImageView PostImage;
    private TextView PostDescription;
    private ImageView EditPostButton,DeletePostButton;
    private String PostKey,currentUserId,dataseUserId,description,image;
    private DatabaseReference ClickPostRef,UsersRef;

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth=FirebaseAuth.getInstance();

        currentUserId=mAuth.getCurrentUser().getUid();

        PostKey=getIntent().getExtras().get("PostKey").toString();

        ClickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        PostImage=findViewById(R.id.click_post_image);
        PostDescription=findViewById(R.id.click_post_description);
        EditPostButton=findViewById(R.id.edit_post_button);
        DeletePostButton=findViewById(R.id.delete_post_button);
        mToolbar=findViewById(R.id.click_app_bar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        EditPostButton.setVisibility(View.INVISIBLE);
        DeletePostButton.setVisibility(View.INVISIBLE);


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    description=dataSnapshot.child("description").getValue().toString();
                    image=dataSnapshot.child("postimage").getValue().toString();
                    dataseUserId=dataSnapshot.child("uid").getValue().toString();

                    PostDescription.setText(description);

                    Picasso.with(ClickPostActivity.this).load(image).into(PostImage);


                    UsersRef.child(dataseUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.exists())
                            {
                                final String name=dataSnapshot.child("fullname").getValue().toString();
                                getSupportActionBar().setTitle(name);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    if (currentUserId.equals(dataseUserId))
                    {

                        EditPostButton.setVisibility(View.VISIBLE);
                        DeletePostButton.setVisibility(View.VISIBLE);


                    }

                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            EditCurrentPost(description);

                        }
                    });

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeleteCurrentPost();

            }
        });




    }

    private void EditCurrentPost(String description)
    {

        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputField=new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ClickPostRef.child("description").setValue(inputField.getText().toString());
                //Toast.makeText(ClickPostActivity.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });

        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow();



    }

    private void DeleteCurrentPost()
    {

        ClickPostRef.removeValue();

        SendUserToMainActivity();
        //Toast.makeText(this, "Post Deleted", Toast.LENGTH_SHORT).show();


    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}