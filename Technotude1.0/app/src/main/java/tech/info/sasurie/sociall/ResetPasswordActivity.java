package tech.info.sasurie.sociall;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity
{

    private Toolbar toolbar;

    private FirebaseAuth mAuth;

    private EditText ResetEmail;
    private Button ResetPasswordButton;

    private String UserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth=FirebaseAuth.getInstance();

        toolbar=(Toolbar) findViewById(R.id.forgot_password_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        ResetEmail=(EditText) findViewById(R.id.reset_password_EMAIL);
        ResetPasswordButton=(Button) findViewById(R.id.reset_password_email_button);


        ResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserEmail=ResetEmail.getText().toString();

                if (TextUtils.isEmpty(UserEmail))
                {
                    Toast.makeText(ResetPasswordActivity.this, "Enter Your Email", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(UserEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                Toast.makeText(ResetPasswordActivity.this, "Go Check Your Mail", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(ResetPasswordActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });




    }
}
