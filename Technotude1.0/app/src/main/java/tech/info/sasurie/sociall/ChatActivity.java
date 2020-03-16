package tech.info.sasurie.sociall;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

    private Toolbar ChatToolBar;
    private ImageView SendMessageButton,SendImageMessageButton;
    private EditText UserMessage;
    private RecyclerView MessageList;
    private String ReceiverId,ReceiverName,SenderId;
    private FirebaseAuth mAuth;

    private TextView ReceiverFullName,UserlastSeen;
    private CircleImageView ReceiverImage;

    private DatabaseReference RootRef,UsersRef;
    private static final int Gallery_Pick=1;
    private StorageReference MessageImageStorageRef;
    private ProgressDialog progressDialog;

    private final List<Messages> messageList=new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        SenderId=mAuth.getCurrentUser().getUid();
        progressDialog=new ProgressDialog(this);


        ReceiverId=getIntent().getExtras().get("visit_user_id").toString();
        ReceiverName=getIntent().getExtras().get("userName").toString();

        RootRef= FirebaseDatabase.getInstance().getReference();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        MessageImageStorageRef= FirebaseStorage.getInstance().getReference().child("Messages_Pictures");

        ChatToolBar=findViewById(R.id.chat_app_bar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);


        UserMessage=findViewById(R.id.input_message);
        SendMessageButton=findViewById(R.id.send_message_button);
        SendImageMessageButton=findViewById(R.id.send_image_button);

        ReceiverFullName=findViewById(R.id.custom_profile_name);
        ReceiverImage=findViewById(R.id.custom_profile_image);
        UserlastSeen=findViewById(R.id.custom_user_last_seen);

        messageAdapter=new MessageAdapter(messageList);

        MessageList=findViewById(R.id.messages_list);
        linearLayoutManager=new LinearLayoutManager(this);
        MessageList.setHasFixedSize(true);
        MessageList.setLayoutManager(linearLayoutManager);
        MessageList.setAdapter(messageAdapter);

        DisplayMessage();


        ReceiverFullName.setText(ReceiverName);

        RootRef.child("Users").child(ReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    final String profileImage=dataSnapshot.child("profileimage").getValue().toString();
                    final String type=dataSnapshot.child("userState").child("type").getValue().toString();
                    final String lastdate=dataSnapshot.child("userState").child("date").getValue().toString();
                    final String lasttime=dataSnapshot.child("userState").child("time").getValue().toString();

                    if (type.equals("online"))
                    {
                        UserlastSeen.setText("online");
                    }
                    else
                    {
                        UserlastSeen.setText("last seen : "+lastdate+"  "+lasttime);
                    }

                    Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(ReceiverImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageFriends();
            }
        });

        SendImageMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });

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

        UsersRef.child(SenderId).child("userState").updateChildren(currentUserStatus);

    }

    private void DisplayMessage()
    {
        RootRef.child("Messages").child(SenderId).child(ReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages=dataSnapshot.getValue(Messages.class);
                messageList.add(messages);

                messageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessageFriends()
    {
        updateUserStatus("online");

        String messageText=UserMessage.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show();
        }
        else
        {

            StoreUserMessage(messageText);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data!=null) {


            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
            final String saveCurrentTime = currentTime.format(calFordTime.getTime());


            progressDialog.setTitle("Sending Image");
            progressDialog.setMessage("Please Wait");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Uri imageUri = data.getData();


            final String message_sender_ref = "Messages/" + SenderId + "/" + ReceiverId;

            final String message_receiver_ref = "Messages/" + ReceiverId + "/" + SenderId;


            DatabaseReference user_message_key = RootRef.child("Messages").child(SenderId).child(ReceiverId).push();

            final String message_push_id = user_message_key.getKey();

            StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");
            if (imageUri != null) {
                filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();



                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", downloadUri);
                            messageTextBody.put("seen", false);
                            messageTextBody.put("type", "image");
                            messageTextBody.put("date", saveCurrentDate);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("from", SenderId);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);


                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Sent success", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                            Toast.makeText(ChatActivity.this, "Picture Sent Successfully", Toast.LENGTH_SHORT).show();

                            progressDialog.dismiss();

                        } else {

                            Toast.makeText(ChatActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            progressDialog.dismiss();

                        }


                    }
                });

            }
        }
    }

    private void StoreUserMessage(String messageText)
    {

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        final String saveCurrentTime = currentTime.format(calFordTime.getTime());

        String message_sender_ref="Messages/"+ SenderId +"/" +ReceiverId;

        String message_receiver_ref="Messages/"+ ReceiverId +"/" +SenderId;

        DatabaseReference user_message_key=RootRef.child("Messages").child(SenderId).child(ReceiverId).push();

        String message_push_id=user_message_key.getKey();

        Map messageTextBody=new HashMap();

        messageTextBody.put("message",messageText);
        messageTextBody.put("seen",false);
        messageTextBody.put("type","text");
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("time",saveCurrentTime);
        messageTextBody.put("from", SenderId);

        Map messageBodyDetails=new HashMap();
        messageBodyDetails.put(message_sender_ref +"/"+ message_push_id,messageTextBody);
        messageBodyDetails.put(message_receiver_ref +"/"+ message_push_id,messageTextBody);

        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(ChatActivity.this, "Sucess", Toast.LENGTH_SHORT).show();
                    UserMessage.setText("");
                }
                else
                {
                    Toast.makeText(ChatActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserStatus("online");

    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        updateUserStatus("online");
    }


}

