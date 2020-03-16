package tech.info.sasurie.sociall;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity
{

    private int progressStatus = 0;


    private Handler handler = new Handler();

    private TextView NameView;
    private String Name= "Technotude";
    private int i=0;
    private ImageView SociallView, SitView;
    private ProgressBar LodingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        NameView=findViewById(R.id.textViewName);
        SociallView=findViewById(R.id.imageViewSociall);
        SitView=findViewById(R.id.imageViewSit);

        FirebaseMessaging.getInstance().subscribeToTopic("aptitude");




        if(!isConnected(SplashActivity.this))
        {
            buildDialog(SplashActivity.this).show();
        }
        else
        {

            Thread splash=new Thread()
            {
                @Override
                public void run()
                {


                    while (progressStatus<=100)
                    {
                        progressStatus=progressStatus+1;

                        try
                        {
                            sleep(50);



                        }
                        catch (Exception e)
                        {

                        }


                        handler.post(new Runnable() {
                            @Override
                            public void run() {



                                if ( progressStatus>10 &&progressStatus<=30)
                                {
                                    SociallView.setVisibility(View.VISIBLE);
                                }
                                else if (progressStatus>31 && progressStatus<60)
                                {
                                    //SociallView.setVisibility(View.INVISIBLE);
                                    if (i<Name.length())
                                    {
                                        NameView.setText(NameView.getText()+""+Name.charAt(i));
                                        i++;
                                    }

                                }
                                else if (progressStatus>61 && progressStatus<90)
                                {


                                    //SociallView.setVisibility(View.VISIBLE);
                                }
                                else if (progressStatus>91)
                                {
                                    NameView.setText(null);
                                    SociallView.setVisibility(View.INVISIBLE);

                                    SitView.setVisibility(View.VISIBLE);
                                }
                                // progressBar.setProgress(progressStatus);

                            }
                        });

                    }


                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finish();



                }

            };
            splash.start();
        }



    }

    public boolean isConnected(Context context)
    {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting())
        {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;

        }
        else
        {
            return false;
        }

        return false;
    }

    public AlertDialog.Builder buildDialog(Context c)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        return builder;
    }


}
