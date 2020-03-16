package tech.info.sasurie.sociall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class SettingsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;

    private EditText userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB,userMobile;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfImage;
    private ImageView ChooseImage;

    private DatabaseReference SettingsuserRef;

    private FirebaseAuth mAuth;

    private String currentUserId;
    final static int Gallery_Pick = 1;

    public ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;

    private SpotsDialog spotsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();

        currentUserId=mAuth.getCurrentUser().getUid();

        SettingsuserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");



        mToolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName=findViewById(R.id.settings_username);
        userProfName=findViewById(R.id.settings_profile_fullname);
        userGender=findViewById(R.id.settings_gender);
        userCountry=findViewById(R.id.settings_country);
        userDOB=findViewById(R.id.settings_dob);
        userStatus=findViewById(R.id.settings_status);
        userRelation=findViewById(R.id.settings_relationship_status);
        userMobile=findViewById(R.id.settings_mobile);
        ChooseImage=findViewById(R.id.choose_image);

        UpdateAccountSettingsButton=findViewById(R.id.update_account_setting_button);
        userProfImage=findViewById(R.id.settings_profile_image);
        loadingBar=new ProgressDialog(this);

        spotsDialog=new SpotsDialog(this,"Updating Information");



        SettingsuserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

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

                    Picasso.with(SettingsActivity.this).load(mProfileImage).placeholder(R.drawable.profile).into(userProfImage);


                    userName.setText(mProfileUsername);
                    userProfName.setText(mProfileFullname);
                    userGender.setText(mProfileGender);
                    userCountry.setText(mProfileCountry);
                    userDOB.setText(mProfileDob);
                    userStatus.setText(mProfileStatus);
                    userRelation.setText(mProfileRelation);
                    userMobile.setText(mProfileMobile);



                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);

            }
        });


        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidateAccountInfo();

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image Updated", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            SettingsuserRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {

                                                userProfImage.setImageURI(Uri.parse(downloadUrl));
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo()
    {

        String username=userName.getText().toString();
        String userprofname=userProfName.getText().toString();
        String usergender=userGender.getText().toString();
        String userstatus=userStatus.getText().toString();
        String usercountry=userCountry.getText().toString();
        String userdob=userDOB.getText().toString();
        String userrelation=userRelation.getText().toString();
        String usermobile=userMobile.getText().toString();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please enter Username", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(userprofname))
        {
            Toast.makeText(this, "Please enter Userfullname", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(usergender))
        {
            Toast.makeText(this, "Please enter gender", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(userstatus))
        {
            Toast.makeText(this, "Please enter status", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(usercountry))
        {
            Toast.makeText(this, "Please enter country", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(userdob))
        {
            Toast.makeText(this, "Please enter dob", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userrelation))
        {
            Toast.makeText(this, "Please enter Relation", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(usermobile) && usermobile.length()!=10)
        {
            Toast.makeText(this, "Please give an valid mobile number", Toast.LENGTH_SHORT).show();
        }
        else
        {
            UpdateAccountInfo(username,userprofname,usergender,userstatus,usercountry,userdob,userrelation,usermobile);


        }




    }

    private void UpdateAccountInfo(String username, String userprofname, String usergender, String userstatus, String usercountry, String userdob, String userrelation, String usermobile)
    {

        /*loadingBar.setTitle("Please Wait");
        loadingBar.setMessage("Updating Profile Details");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);*/

        spotsDialog.show();

        HashMap userMap=new HashMap();
        userMap.put("username", username);
        userMap.put("fullname", userprofname);
        userMap.put("country", usercountry);
        userMap.put("status", userstatus);
        userMap.put("gender", usergender);
        userMap.put("dob", userdob);
        userMap.put("relationshipstatus", userrelation);
        userMap.put("mobile",usermobile);

        SettingsuserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful())
                {
                    SendUserToMainActivity();
                    spotsDialog.dismiss();
                    //Toast.makeText(SettingsActivity.this, "Account Detailed Updated Successfully", Toast.LENGTH_SHORT).show();
                    //loadingBar.dismiss();


                }else
                {
                    Toast.makeText(SettingsActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    spotsDialog.dismiss();
                    //loadingBar.dismiss();

                }

            }
        });


    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
