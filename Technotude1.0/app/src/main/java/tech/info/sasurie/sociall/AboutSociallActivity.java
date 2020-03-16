package tech.info.sasurie.sociall;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AboutSociallActivity extends AppCompatActivity
{

    private ImageView SociallView, SitView;
    private TextView AppNameView, UpdateView, VersionView;
    private Button UpdateButton;
    private FirebaseAuth mAuth;
    private DatabaseReference AdminRef;
    private static final int Gallery_Pick=1;
    private static final int Gallery_Pick1=2;

    private String CurrentUserId;
    private Uri ImageUri, ImageUri1;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, downloadUrl1;
    private StorageReference PostsImagesRefrence;
    private FirebaseUser Currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_sociall);

        mAuth=FirebaseAuth.getInstance();

        PostsImagesRefrence = FirebaseStorage.getInstance().getReference();

        AdminRef= FirebaseDatabase.getInstance().getReference().child("AdminAppDetails");

        CurrentUserId=mAuth.getCurrentUser().getUid();

        Currentuser=mAuth.getCurrentUser();

        SociallView=findViewById(R.id.imageViewAppIcon);
        SitView=findViewById(R.id.imageViewPower);

        AppNameView=findViewById(R.id.textViewAppName);
        UpdateView=findViewById(R.id.textViewUpdate);
        VersionView=findViewById(R.id.textViewAppVersion);

        UpdateButton=findViewById(R.id.buttonUpdate);


        UpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UpdateView.getText().toString().equals("Updates are not Available!!!"))
                {
                    Toast.makeText(AboutSociallActivity.this, "Updates are not Available!!!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(AboutSociallActivity.this, "Please Download the New Updated App", Toast.LENGTH_SHORT).show();
                }
            }
        });



        if (Currentuser != null)
        {
            AdminRef.child("kUomz6kgXzSLJmxSzMF3V5gjgPM2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        String appname=dataSnapshot.child("appname").getValue().toString();
                        String appimage=dataSnapshot.child("appimage").getValue().toString();
                        String appversion=dataSnapshot.child("appversion").getValue().toString();
                        String powerimage=dataSnapshot.child("powerimage").getValue().toString();
                        String update=dataSnapshot.child("update").getValue().toString();

                        AppNameView.setText(appname);
                        UpdateView.setText(update);
                        VersionView.setText(appversion);

                        Picasso.with(AboutSociallActivity.this).load(appimage).placeholder(R.drawable.sociallicon).into(SociallView);
                        Picasso.with(AboutSociallActivity.this).load(powerimage).placeholder(R.drawable.sit).into(SitView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }

}
