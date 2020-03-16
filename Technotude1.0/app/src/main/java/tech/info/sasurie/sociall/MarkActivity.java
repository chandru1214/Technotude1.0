package tech.info.sasurie.sociall;

import android.app.ActionBar;
import android.nfc.Tag;
import android.util.Log;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestHandler;
import com.squareup.picasso.RequestHandler;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.lang.String;

//import static aptitude.sasurie.leisurebreaker.LoginActivity.MyPREFERENCES;
//import static tech.info.sasurie.sociall.Les_LoginActivity.MyPREFERENCES;

public class MarkActivity extends AppCompatActivity {
    String testid,regno,mark;
    private TextView textView,textView9;
    SharedPreferences sharedpreferences;
    private AdView mAdView;
    private DatabaseReference Markdata,UserData,mDb;
    Users users;
private  FirebaseUser userKey,user1;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mDatabase;
   private String  senderUserId,fullname,profileimage,status,receiverUserId;
private int marks;
    private static final String TAG = "MarkActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);
        Intent intent = getIntent();
        MobileAds.initialize(this,
                "ca-app-pub-2052757055681240~3637998856");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        firebaseAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance();
            mDb = mDatabase.getReference();
            user1 = firebaseAuth.getCurrentUser();
            String userKey = user1.getUid();

        mDb.child("Users").child(userKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    fullname = dataSnapshot.child("fullname").getValue(String.class);
                    profileimage = dataSnapshot.child("profileimage").getValue(String.class);
                    Log.d(TAG, "Name: " + fullname);
                    Log.d(TAG, "Name: " + profileimage);
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        /*sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if(sharedpreferences.contains(Config.KEY_REGNO) && sharedpreferences.contains(Config.KEY_PASSWORD)){

        }else {
            if(sharedpreferences.contains(Config.KEY_REGNO) && sharedpreferences.contains(Config.KEY_PASSWORD)){

                Intent loginIntent = new Intent(Les_MarkActivity.this,Les_LoginActivity.class);
                startActivity(loginIntent);

                finish();   //finish current activity
            }

        }*/
users=new Users(fullname,status,profileimage,marks);
 Markdata= FirebaseDatabase.getInstance().getReference().child("Mark Details");
        UserData=FirebaseDatabase.getInstance().getReference().child("Users");
        testid = intent.getStringExtra(Config.TEST_ID);
        // regno=intent.getStringExtra(Config.KEY_REGNO);
        mark=intent.getStringExtra(Config.TEST_MARK);

        textView=findViewById(R.id.textmark);
        textView.setText(mark);
        textView9=findViewById(R.id.textView9);
        registerMark();



        textView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


               users.setFullname(fullname);
               users.setProfileimage(profileimage);
                users.setMark(Integer.parseInt(mark));
              Markdata.push().setValue(users);

Toast.makeText(MarkActivity.this,fullname,Toast.LENGTH_LONG).show();

                Intent i= new Intent(MarkActivity.this,TestViewActivity.class);




                startActivity(i);
                finish();
            }
        });


    }
    private void registerMark() {
        class AddUser extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MarkActivity.this,"Adding...","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(MarkActivity.this,s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String,String> params = new HashMap<>();
                // params.put(Config.KEY_REGNO,regno);
                //params.put(Config.KEY_TESTID,testid);
                params.put(Config.TEST_MARK,mark);
                //params.put(Config.KEY_EMP_PHNO,phno);
                //   RequestHandler rh = new RequestHandler();
                //  String res = rh.sendPostRequest(Config.URL_LOGINS, params);
                String res=" ";
                return res;
            }
        }

        AddUser ae = new AddUser();
        ae.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
