package tech.info.sasurie.sociall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import dmax.dialog.SpotsDialog;


public class LoginActivity extends AppCompatActivity
{

    private EditText UserEmail,UserPassword;
    private Button CreateButton,LoginButton,googleSignInButton,mobilesigninbutton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private boolean emailAddressChecker=false;
    private TextView ForgotPassword,CreateLink;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";
    private String Author="Dont Have";
    private SpotsDialog LogDialog,Credialog;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        UserEmail=findViewById(R.id.login_email);
        UserPassword=findViewById(R.id.login_password);
        CreateButton=findViewById(R.id.register_button);
        LoginButton=findViewById(R.id.login_button);
        ForgotPassword=findViewById(R.id.forgot_password_link);
        CreateLink=findViewById(R.id.create_link);
        googleSignInButton = (Button) findViewById(R.id.google_signin_button);
        mobilesigninbutton=findViewById(R.id.mobile_signin_button);

        loadingBar=new ProgressDialog(this);
        LogDialog = new SpotsDialog(this,"Please Wait");
        Credialog=new SpotsDialog(this,"Creating Account");


        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

        CreateLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               if (Author.equals("Dont Have"))
               {
                   LoginButton.setVisibility(View.INVISIBLE);

                   CreateButton.setVisibility(View.VISIBLE);

                   CreateLink.setText("Already Have an Account? Login");

                   Author="Account Have";

               }
               else if (Author.equals("Account Have"))
               {


                   LoginButton.setVisibility(View.VISIBLE);

                   CreateButton.setVisibility(View.INVISIBLE);

                   CreateLink.setText("Dont Have Account? Create New Account");

                   Author="Dont Have";

               }
            }
        });


        mobilesigninbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               Intent Registerintent=new Intent(LoginActivity.this,RegisterActivity.class);
               startActivity(Registerintent);
            }
        });




        CreateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email=UserEmail.getText().toString();
                String pass=UserPassword.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this, "Enter Your Email Here!!!", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(pass))
                {
                    Toast.makeText(LoginActivity.this, "Enter Your Password Here!!!", Toast.LENGTH_SHORT).show();
                }
                else if(pass.length()<6)
                {
                    Toast.makeText(LoginActivity.this, "Password shoud be atleast 6 characters", Toast.LENGTH_SHORT).show();
                }
                else
                {
                   /* loadingBar.setTitle("Creating Account");
                    loadingBar.setMessage("Please Wait");
                    loadingBar.show();*/
                    Credialog.show();
                    CreateNewAccount(email,pass);
                }

            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=UserEmail.getText().toString();
                String pass=UserPassword.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this, "Enter Your Email Here!!!", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(pass))
                {
                    Toast.makeText(LoginActivity.this, "Enter Your Password Here!!!", Toast.LENGTH_SHORT).show();
                }
                else if(pass.length()<6)
                {
                    Toast.makeText(LoginActivity.this, "Password shoud be atleast 6 characters", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    /*loadingBar.setTitle("Login");
                    loadingBar.setMessage("Please Wait");
                    loadingBar.show();*/
                    LogDialog.show();
                    login(email,pass);
                }
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Toast.makeText(LoginActivity.this, "Connection to Google Sign in failed...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                signIn();
            }
        });


    }

    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            loadingBar.setTitle("Google Sign In");
            loadingBar.setMessage("Please wait");
            loadingBar.setCancelable(true);
            loadingBar.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                //Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Can't get Auth result.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithCredential:success");
                            SendUserToMainActivity();
                            loadingBar.dismiss();
                        }
                        else
                        {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().toString();
                            SendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this, "Not Authenticated : " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void SendUserToLoginActivity()
    {
        startActivity(new Intent(LoginActivity.this,LoginActivity.class));
    }

    private void login(String email, String pass)
    {
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    VerifyEmailAddress();
//                    spotsDialog.dismiss();
                    //loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    LogDialog.dismiss();
                    //loadingBar.dismiss();
                }

            }
        });
    }

    private void VerifyEmailAddress()
    {
        FirebaseUser user=mAuth.getCurrentUser();
        emailAddressChecker=user.isEmailVerified();

        if (emailAddressChecker)
        {
            SendUserToMainActivity();
            LogDialog.dismiss();
        }
        else
        {
            SendUserToLoginActivity();
            LogDialog.dismiss();
            Toast.makeText(this, "Please Verify your Email", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
    }

    private void SendEmailVerification()
    {
        FirebaseUser user=mAuth.getCurrentUser();

        if (user!=null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        SendUserToLoginActivity();
                        Toast.makeText(LoginActivity.this, "Please Confirm Your Mail", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void CreateNewAccount(String email, String pass)
    {
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    SendEmailVerification();
                    Credialog.dismiss();
                    //loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Credialog.dismiss();
                    //loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(LoginActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if (currentUser !=null)
        {
            SendUserToMainActivity();
        }

    }
}
