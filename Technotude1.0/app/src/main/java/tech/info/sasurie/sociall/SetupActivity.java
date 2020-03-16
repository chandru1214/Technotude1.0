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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class SetupActivity extends AppCompatActivity
{

    private CircleImageView UserProfileImage;
    private EditText UserName, UserFullName, UserCountry,UserMobile;
    private Button ButtonSave;
    private ImageView ChooseImage;
    private final static int Gallery_Pick = 1;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;
    private String CurrentUserId;
    private FirebaseAuth mAuth;
    private Uri ImageUri,ResultUri;
    private String CurrentUserName,CurrentUserFullName,CurrentUserCountry,CurrentUserMobile;
    private SpotsDialog spotsDialog;

    private String downloadUrl="https://firebasestorage.googleapis.com/v0/b/myapp-4eadd.appspot.com/o/chatterplace.png?alt=media&token=e51fa887-bfc6-48ff-87c6-e2c61976534e";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();

        UserProfileImage = findViewById(R.id.userImage);
        UserFullName = findViewById(R.id.user_full_name);
        UserName = findViewById(R.id.user_name);
        UserCountry = findViewById(R.id.user_country);
        UserMobile=findViewById(R.id.user_mobile);
        ChooseImage=findViewById(R.id.choose_image);
        ButtonSave=findViewById(R.id.buttonSave);
        loadingBar = new ProgressDialog(this);

        spotsDialog=new SpotsDialog(this,"Saving Information");

        CurrentUserId = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        ButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUserFullName=UserFullName.getText().toString();
                CurrentUserName=UserName.getText().toString();
                CurrentUserCountry=UserCountry.getText().toString();
                CurrentUserMobile=UserMobile.getText().toString();

                if (TextUtils.isEmpty(CurrentUserFullName))
                {
                    Toast.makeText(SetupActivity.this, "Enter Your Name!!!", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(CurrentUserName))
                {
                    Toast.makeText(SetupActivity.this, "Enter Your UserName!!!", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(CurrentUserCountry))
                {
                    Toast.makeText(SetupActivity.this, "Enter Your Country!!!", Toast.LENGTH_SHORT).show();
                }
                else if (CurrentUserMobile.length() !=10 || TextUtils.isEmpty(CurrentUserMobile))
                {
                    Toast.makeText(SetupActivity.this, "Please Given Valid Mobile Number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SavingUserInformation();
                }
            }
        });



    }

    private void SavingUserInformation()
    {
        /*loadingBar.setTitle("Saving Information");
        loadingBar.setMessage("Please wait");
        loadingBar.show();
        loadingBar.setCancelable(false);*/

        spotsDialog.show();

        StorageReference filePath = UserProfileImageRef.child(CurrentUserId + ".jpg");

        if (ResultUri !=null)
        {
            filePath.putFile(ResultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful())
                    {

                        downloadUrl = task.getResult().getDownloadUrl().toString();
                        SavingInfoUser();

                    }
                }
            });

        }
        else
        {
            SavingInfoUser();
        }
    }

    private void SavingInfoUser()
    {
        HashMap userMap = new HashMap();
        userMap.put("username", CurrentUserName);
        userMap.put("fullname", CurrentUserFullName);
        userMap.put("country", CurrentUserCountry);
        userMap.put("profileimage",downloadUrl);
        userMap.put("status", "Hey there, i am using Sociall , developed by RBK.");
        userMap.put("gender", "none");
        userMap.put("dob", "none");
        userMap.put("relationshipstatus", "none");
        userMap.put("mobile",CurrentUserMobile);
        UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    spotsDialog.dismiss();
                    //loadingBar.dismiss();
                }
                else
                {
                    String message =  task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                    spotsDialog.dismiss();
                    //loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null)
        {
            ImageUri = data.getData();

            CropImage.activity(ImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {

                ResultUri = result.getUri();
                UserProfileImage.setImageURI(ResultUri);
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
