package tech.info.sasurie.sociall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminSociallActivity extends AppCompatActivity
{

    private ImageView SociallView, SitView;
    private EditText AppNameView, UpdateView, VersionView;
    private Button UpdateButton;
    private FirebaseAuth mAuth;
    private DatabaseReference AdminRef;
    private static final int Gallery_Pick=1;
    private static final int Gallery_Pick1=2;

    private ProgressDialog loadingBar;

    private String CurrentUserId;
    private Uri ImageUri, ImageUri1;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, downloadUrl1;
    private StorageReference PostsImagesRefrence;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sociall);


        mAuth=FirebaseAuth.getInstance();

        loadingBar=new ProgressDialog(this);

        PostsImagesRefrence = FirebaseStorage.getInstance().getReference();

        AdminRef= FirebaseDatabase.getInstance().getReference().child("AdminAppDetails");

        CurrentUserId=mAuth.getCurrentUser().getUid();

        SociallView=findViewById(R.id.imageViewAppIcon);
        SitView=findViewById(R.id.imageViewPower);

        AppNameView=findViewById(R.id.textViewAppName);
        UpdateView=findViewById(R.id.textViewUpdate);
        VersionView=findViewById(R.id.textViewAppVersion);

        UpdateButton=findViewById(R.id.buttonUpdate);

        SociallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        SitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGalleryNew();
            }
        });

        UpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ValidateAppInfo();
            }
        });

    }


    private void ValidateAppInfo()
    {
        String appname=AppNameView.getText().toString();
        String appver=VersionView.getText().toString();
        String update=UpdateView.getText().toString();

        if(ImageUri == null && ImageUri1 == null)
        {
            Toast.makeText(this, "Please select image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(appname))
        {
            Toast.makeText(this, "Please enter app name.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(appver))
        {
            Toast.makeText(this, "Please enter version of app", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(update))
        {
            Toast.makeText(this, "Please enter the any updates", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait");
            loadingBar.show();
            loadingBar.setCancelable(false);

            StoringImageToFirebaseStorage();
        }
    }


    private void StoringImageToFirebaseStorage()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        if (ImageUri != null && ImageUri1 != null)
        {
            final StorageReference filePath = PostsImagesRefrence.child("App Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
            final StorageReference filePath1 = PostsImagesRefrence.child("App Images").child(ImageUri1.getLastPathSegment() + postRandomName + ".jpg");

            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                {
                    if(task.isSuccessful())
                    {
                        downloadUrl = task.getResult().getDownloadUrl().toString();
                        filePath1.putFile(ImageUri1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    downloadUrl1 = task.getResult().getDownloadUrl().toString();
                                    SavingPostInformationToDatabase();
                                }
                                else
                                {
                                    Toast.makeText(AdminSociallActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(AdminSociallActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


    private void SavingPostInformationToDatabase()
    {

        HashMap postsMap = new HashMap();
        postsMap.put("uid", CurrentUserId);
        postsMap.put("date", saveCurrentDate);
        postsMap.put("time", saveCurrentTime);
        postsMap.put("appname", AppNameView.getText().toString());
        postsMap.put("appimage", downloadUrl);
        postsMap.put("powerimage", downloadUrl1);
        postsMap.put("appversion", VersionView.getText().toString());
        postsMap.put("update",UpdateView.getText().toString());

        AdminRef.child(CurrentUserId).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {
                    loadingBar.dismiss();
                    startActivity(new Intent(AdminSociallActivity.this,MainActivity.class));
                }
                else
                {
                    loadingBar.dismiss();
                    Toast.makeText(AdminSociallActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });





    }

    private void OpenGalleryNew()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick1);
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SociallView.setImageURI(ImageUri);
        }
        else if (requestCode==Gallery_Pick1 && resultCode==RESULT_OK && data!=null)
        {
            ImageUri1=data.getData();
            SitView.setImageURI(ImageUri1);
        }
    }
}
