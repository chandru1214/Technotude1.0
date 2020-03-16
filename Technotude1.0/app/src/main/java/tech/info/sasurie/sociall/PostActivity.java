package tech.info.sasurie.sociall;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private Bitmap Photo;

    private ImageView SelectPostImage;
    private ImageView UpdatePostButton;
    private EditText PostDescription;

    private static final int Gallery_Pick = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    private Uri ImageUri;
    private String Description;

    private StorageReference PostsImagesRefrence;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    private long countPosts=0;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        MobileAds.initialize(this, MainActivity.APP_ID);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostsImagesRefrence = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        SelectPostImage = (ImageView) findViewById(R.id.select_post_image);
        UpdatePostButton = (ImageView) findViewById(R.id.update_post_button);
        PostDescription =(EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);


        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //OpenGallery();

                CharSequence options[]=new CharSequence[]
                        {
                                "Camera",
                                "Gallery"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("Select Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0)
                        {
                            OpenCamera();

                        }
                        if (which==1)
                        {
                            OpenGallery();

                        }
                    }
                });
                builder.show();

            }
        });


        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
            }
        });
    }

    private void OpenCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try
        {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST);
            }
        }
        catch (Exception e)
        {
            Log.d("Ragu", "OpenCamera: Exception"+e);
        }
    }


    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString();

        if(ImageUri == null && Photo == null)
        {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please enter description.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait");
            loadingBar.show();
            loadingBar.setCancelable(false);

            try
            {
                StoringImageToFirebaseStorage();
            }
            catch (Exception e)
            {
                System.out.print(e);
            }
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


        if (ImageUri != null && Photo == null)
        {
            StorageReference filePath = PostsImagesRefrence.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                {
                    if(task.isSuccessful())
                    {
                        downloadUrl = task.getResult().getDownloadUrl().toString();
                        try
                        {
                            SavingPostInformationToDatabase();
                        }
                        catch (Exception e)
                        {
                            System.out.print(e);
                        }

                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        else if (Photo != null && ImageUri == null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            byte[] b = stream.toByteArray();
            StorageReference filePath = PostsImagesRefrence.child("Post Images").child(postRandomName + ".jpg");

            filePath.putBytes(b).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        downloadUrl=task.getResult().getDownloadUrl().toString();
                        try
                        {
                            SavingPostInformationToDatabase();
                        }
                        catch (Exception e)
                        {
                            System.out.print(e);
                        }
                    }
                    else
                    {
                        Toast.makeText(PostActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }




    private void SavingPostInformationToDatabase()
    {

        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    countPosts=dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts=0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String userFullName=dataSnapshot.child("fullname").getValue().toString();
                    String userprofileImage = dataSnapshot.child("profileimage").getValue().toString();


                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage",userprofileImage);
                    postsMap.put("fullname", userFullName);
                    postsMap.put("counter",countPosts);

                    String ref=current_user_id+postRandomName;
                    String output=EncodeString(ref);


                   try
                   {
                       PostsRef.child(output).updateChildren(postsMap)
                               .addOnCompleteListener(new OnCompleteListener() {
                                   @Override
                                   public void onComplete(@NonNull Task task)
                                   {
                                       if(task.isSuccessful())
                                       {
                                           SendUserToMainActivity();
                                           loadingBar.dismiss();
                                           finish();
                                       }
                                       else
                                       {
                                           Toast.makeText(PostActivity.this, "Error Occured while updating your post."+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                           loadingBar.dismiss();
                                       }
                                   }
                               });
                   }
                   catch (Exception e)
                   {
                       System.out.print(e);
                       loadingBar.dismiss();
                       Toast.makeText(PostActivity.this, "Exception Try Again"+e, Toast.LENGTH_SHORT).show();
                   }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    public static String EncodeString(String string)
    {
        return string.replace(".", ",");
    }

    public static String DecodeString(String string)
    {
        return string.replace(",", ".");
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
            SelectPostImage.setImageURI(ImageUri);
        }
        else if (requestCode==CAMERA_PIC_REQUEST && resultCode==RESULT_OK && data!=null)
        {
            Photo = (Bitmap)data.getExtras().get("data");
            SelectPostImage.setImageBitmap(Photo);

            UploadCameraPost();

        }
    }

    private void UploadCameraPost()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] b = stream.toByteArray();
    }


   /* @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
       try
       {
           int id = item.getItemId();

           if(id == android.R.id.home)
           {
               SendUserToMainActivity();
           }

       }
       catch (Exception e)
       {
           System.out.print(e);
       }

        return super.onOptionsItemSelected(item);
    }
*/


    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}
