package tech.info.sasurie.sociall;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

//import static aptitude.sasurie.leisurebreaker.LoginActivity.MyPREFERENCES;
//import static tech.info.sasurie.sociall.Les_LoginActivity.MyPREFERENCES;

public class TestActivity extends AppCompatActivity {
    JSONObject jsonObject = null;
    String[] answer, answerdin;
    JSONArray result;
    String testid, answe;
    int var = 0;
    int value = 0;
    int mark;
    RelativeLayout relativeLayout;
    Paint paint;
    View view;
    Path path2;
    Bitmap bitmap;
    Canvas canvas;
    Menu menu;
    private TextView question, questno, textView4;
    private RadioGroup radioGroup;
    private Button button, button2;
    private String regno;
    private RadioButton choice1, choice2, choice3, choice4, answerd;
    TextView mTextField,duration;
    int durations=0;
    SharedPreferences sharedpreferences;
    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
      /*  sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if(sharedpreferences.contains(Config.KEY_REGNO) && sharedpreferences.contains(Config.KEY_PASSWORD)){
        }else {
            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            if(sharedpreferences.contains(Config.KEY_REGNO) && sharedpreferences.contains(Config.KEY_PASSWORD)){
                Intent loginIntent = new Intent(Les_TestActivity.this,Les_LoginActivity.class);
                startActivity(loginIntent);
                finish();   //finish current activity
            }
        }*/
        Intent intent = getIntent();
        mTextField =findViewById(R.id.remain_time);
        duration=findViewById(R.id.duration);


        testid = intent.getStringExtra(Config.TEST_ID);
      //  regno = intent.getStringExtra(Config.KEY_REGNO);
        button2 = findViewById(R.id.button13);


        questno = findViewById(R.id.question_no);
        question = findViewById(R.id.question_text);
        radioGroup = findViewById(R.id.radioquestion);
        button = findViewById(R.id.next_btn);
        choice1 = findViewById(R.id.choice1);
        choice2 = findViewById(R.id.choice2);
        choice3 = findViewById(R.id.choice3);
        choice4 = findViewById(R.id.choice4);


        relativeLayout = (RelativeLayout) findViewById(R.id.relativelayout1);


        getJSON();

    }


    private void getJSON() {

        class GetJSON extends AsyncTask<Void, Void, String> {

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(TestActivity.this, "Fetching Data", "Wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                try {
                    Log.e("json----",s);
                    jsonObject = new JSONObject(s);
                    if (s!=null) {
                        result = jsonObject.getJSONArray(Config.TAG_JSON_ARRAY);
                        durations=Integer.parseInt(jsonObject.getString(Config.TEST_DURATION));
                        int i = result.length();
                        setvalue(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startCounDown(durations);
                displayStudents(0);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String s = rh.sendGetRequestParam(Config.URL_GET_QUESTION, testid);
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();

    }

    private void startCounDown(final int durations) {
        countDownTimer= new CountDownTimer(durations*60000, 1000) {

            public void onTick(long millisUntilFinished) {
                duration.setText("Test Duration : "+secondsToString(durations*60));
                mTextField.setText(""+String.format(" %d : %d",
                        TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

            }

            public void onFinish() {
                submitClick(result.length());
            }
        }.start();
    }

    public void setvalue(final int i) {
        answerdin = new String[i];
        answer = new String[i];
        value = i - 1;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (var < value) {

                    int selectedId = radioGroup.getCheckedRadioButtonId();


                    if (selectedId == -1) {
                        Toast.makeText(TestActivity.this, "please select the answer", Toast.LENGTH_LONG).show();
                    } else {
                        answerd = findViewById(selectedId);
                        String answer = answerd.getText().toString().trim();
                        answerdin[var] = answer;
                        Log.e("selected",answerdin[var]+"|"+var);
                        radioGroup.clearCheck();
                        var = var + 1;
                        displayStudents(var);
                    }
                } else if (var == value) {
                    Toast.makeText(TestActivity.this, "You Completed the test Click Submit to Check your score", Toast.LENGTH_SHORT).show();
                    button.setText("Submit");
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    if (selectedId == -1) {
                        Toast.makeText(TestActivity.this, "please select the answer", Toast.LENGTH_LONG).show();
                    } else {
                        answerd = findViewById(selectedId);
                        answe = answerd.getText().toString().trim();
                        answerdin[var] = answe;
                        Log.e("selected",answerdin[var]+"|"+var);
                        var=var+1;
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                countDownTimer.cancel();
                                submitClick(i);
                            }
                        });
                    }
                }
            }
        });
    }

    public void submitClick(int i){

        if (var==0){
            mark=0;
        }else {
            for (int a=0;a<var;a++){
                Log.e("selected for ",answerdin.length+" | "+a);
                String ans = answer[a];
                String answ = answerdin[a];
                Log.e("selected in ",ans+" | "+answ +" | "+a);
                if (ans.equalsIgnoreCase(answ)) {
                    mark = mark + 1;
                } else {
                    mark = mark;
                }

            }
        }
        String s = Integer.toString(mark);
        Intent intent = new Intent(TestActivity.this,MarkActivity.class);
        intent.putExtra(Config.TEST_ID, testid);
        intent.putExtra(Config.TEST_MARK, s);
        intent.putExtra(Config.KEY_REGNO, regno);
        startActivity(intent);
        this.finish();
    }
    private void displayStudents(int j) {


        try {
            JSONObject jo = result.getJSONObject(j);

            int j1 = j + 1;
            String s1 = Integer.toString(j1);
            questno.setText(s1);
            String quest = jo.getString(Config.KEY_QUESTION);
            String cho1 = jo.getString(Config.KEY_CHOICE1);
            String cho2 = jo.getString(Config.KEY_CHOICE2);
            String cho3 = jo.getString(Config.KEY_CHOICE3);
            String cho4 = jo.getString(Config.KEY_CHOICE4);
            String answ = jo.getString(Config.KEY_ANSWER);

            question.setText(quest);
            choice1.setText(cho1);
            choice2.setText(cho2);
            choice3.setText(cho3);
            choice4.setText(cho4);
            answer[j] = answ;
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }


    private String secondsToString(int pTime) {
        final int min = pTime/60;
        final int sec = pTime-(min*60);

        final String strMin = placeZeroIfNeede(min);
        final String strSec = placeZeroIfNeede(sec);
        return String.format("%s : %s",strMin,strSec);
    }
    private String placeZeroIfNeede(int number) {
        return (number >=10)? Integer.toString(number):String.format("0%s",Integer.toString(number));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_favorite:
                //menu.findItem(R.id.action_favorite).setVisible(false);
                if (button2.getVisibility() == View.INVISIBLE) {

                    button2.setVisibility(View.VISIBLE);
                    relativeLayout.setVisibility(View.VISIBLE);
                    draw();
                } else {
                    button2.setVisibility(View.INVISIBLE);
                    relativeLayout.setVisibility(View.INVISIBLE);

                }

                return true;

            case R.id.action_settings:
                // location found
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void draw() {
        int wid, hei;
        Display dis1 = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        dis1.getSize(size);
        wid = size.x;
        hei = size.y;
        //int wid1= (int) (wid/2);
        int hei1 = (int) (hei / 2);
        view = new SketchSheetView(TestActivity.this);

        paint = new Paint();

        path2 = new Path();

        relativeLayout.addView(view, new LayoutParams(wid, hei1));

        paint.setDither(true);

        paint.setColor(Color.parseColor("#000000"));

        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeJoin(Paint.Join.ROUND);

        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setStrokeWidth(4);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                path2.reset();
                draw();

            }
        });


    }

    class SketchSheetView extends View {

        private ArrayList<DrawingClass> DrawingClassArrayList = new ArrayList<>();

        public SketchSheetView(Context context) {

            super(context);

            bitmap = Bitmap.createBitmap(820, 480, Bitmap.Config.ARGB_4444);

            canvas = new Canvas(bitmap);

            this.setBackgroundDrawable(getResources().getDrawable(R.drawable.box_text_test));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            DrawingClass pathWithPaint = new DrawingClass();

            canvas.drawPath(path2, paint);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                path2.moveTo(event.getX(), event.getY());

                path2.lineTo(event.getX(), event.getY());
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                path2.lineTo(event.getX(), event.getY());

                pathWithPaint.setPath(path2);

                pathWithPaint.setPaint(paint);

                DrawingClassArrayList.add(pathWithPaint);
            }

            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (DrawingClassArrayList.size() > 0) {

                canvas.drawPath(
                        DrawingClassArrayList.get(DrawingClassArrayList.size() - 1).getPath(),

                        DrawingClassArrayList.get(DrawingClassArrayList.size() - 1).getPaint());
            }
        }
    }

    public class DrawingClass {

        Path DrawingClassPath;
        Paint DrawingClassPaint;

        public Path getPath() {
            return DrawingClassPath;
        }

        public void setPath(Path path) {
            this.DrawingClassPath = path;
        }


        public Paint getPaint() {
            return DrawingClassPaint;
        }

        public void setPaint(Paint paint) {
            this.DrawingClassPaint = paint;
        }
    }



    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();

            //moveTaskToBack(false);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void exitByBackKey() {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("Do you want to quit the test")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        countDownTimer.cancel();
                        finish();
                        //close();


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .show();

    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }

}


