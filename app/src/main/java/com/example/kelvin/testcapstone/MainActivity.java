package com.example.kelvin.testcapstone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*

Course Search tool

 */

public class MainActivity extends AppCompatActivity {

    TextView tvCourse, tvStatus;
    String jsonData;
    Button btSearch, btBack;
    EditText editPrefix, editNumber;
    Context thisContext = this;

    String argPrefix, argNumber;
    String token;//Token for authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btSearch = (Button) findViewById(R.id.btSearch);
        btBack = (Button) findViewById(R.id.btBack);
        editPrefix = (EditText) findViewById(R.id.editPrefix);
        editNumber = (EditText) findViewById(R.id.editNumber);
        token = LoginActivity.token;

        tvCourse = (TextView) findViewById(R.id.tvCourse);
        tvCourse.setMovementMethod(new ScrollingMovementMethod());
        final Scroller scroller = new Scroller(thisContext);
        tvCourse.setScroller(scroller);
        tvCourse.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gesture = new GestureDetector(thisContext,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float veloX, float veloY) {
                            scroller.fling(0, tvCourse.getScrollY(), 0, (int) -veloY, 0, 0, 0,
                                    (tvCourse.getLineCount() * tvCourse.getLineHeight()));
                            return super.onFling(e1, e2, veloX, veloY);
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gesture.onTouchEvent(event);
                return false;
            }
        });

        startGui();

    }

    void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPreferences.getString("token", token);
    }

    void startGui() {
        //Set up editPrefix
        editPrefix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPrefix.getText().clear();
            }
        });

        editNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNumber.getText().clear();
            }
        });

        //Set up Search button
        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                argPrefix = editPrefix.getText().toString();
                argNumber = editNumber.getText().toString();
                hideKeyboard();

                if (!Utility.preventAttack(argPrefix) || !Utility.preventAttack(argNumber)) {
                    Toast.makeText(thisContext, "Invalid characters. Try again", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (argPrefix.isEmpty() && argNumber.isEmpty()) {
                    tvStatus.setText("Searching for all Courses. Please wait");
                    tvStatus.setTextColor(Color.BLUE);
                } else if (!argPrefix.isEmpty() && !argNumber.isEmpty()) {
                    tvStatus.setText("Searching for " + argPrefix + " " + argNumber + ". Please wait");
                    tvStatus.setTextColor(Color.BLUE);
                } else if (!argPrefix.isEmpty() && argNumber.isEmpty()) {
                    tvStatus.setText("Searching for all " + argPrefix.toUpperCase() +
                            " courses. Please wait");
                    tvStatus.setTextColor(Color.BLUE);
                } else if (argPrefix.isEmpty() && !argNumber.isEmpty()) {
                    tvStatus.setText("Searching for all " + argNumber + " courses. Please wait");
                    tvStatus.setTextColor(Color.BLUE);
                }

                connect();
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, ContentsActivity.class);
                startActivity(mainIntent);
            }
        });

        setTitle("Course Search");
    }

    @Override
    public void onBackPressed() {
        btBack.performClick();
    }

    public void connect() {
        new RetrieveInfoTask().execute();
    }

    public void parse() {
        try {
            String result = "";

            //Get JSON array of all courses with given
            JSONArray allCourses = new JSONArray(jsonData);

            for (int i = 0; i < allCourses.length(); i++) {
                //Get course info, except sections
                JSONObject course = allCourses.getJSONObject(i);
                String prefix = course.getString("prefix");
                String number = course.getString("number");
                String title = course.getString("title");
                int hours = course.getInt("credit_hours");
                Course courseFinal = new Course(prefix, number, title, hours);

                //Get section info
                JSONArray sections = course.getJSONArray("sections");
                for (int j = 0; j < sections.length(); j++) {
                    JSONObject section = sections.getJSONObject(j);
                    int crn = section.getInt("crn");
                    String sect = section.getString("section");
                    String days = section.getString("days");
                    String time = section.getString("time");
                    int seats = section.getInt("seats");
                    String teacher = section.getString("teacher");
                    String building = section.getString("building");
                    String room = section.getString("room");

                    Section sectionFinal = new Section(crn, sect, days, time, seats,
                            teacher, building, room);
                    courseFinal.addSection(sectionFinal);
                }

                result += courseFinal.toString();

            }

            tvCourse.setText(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class RetrieveInfoTask extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;
        int response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String myUrl;

                //Search by Course Number
                if (argPrefix.isEmpty() && !argNumber.isEmpty()) {
                    myUrl = "https://vtebscefip.localtunnel.me/courses/?format=json&"
                            + "number=" + argNumber;
                }
                //Search by Course Prefix
                else if (argNumber.isEmpty() && !argPrefix.isEmpty()) {
                    myUrl = "https://vtebscefip.localtunnel.me/courses/?format=json&prefix=" + argPrefix;
                }
                //Search by Course Prefix and Number
                else if (!argNumber.isEmpty() && !argPrefix.isEmpty()) {
                    myUrl = "https://vtebscefip.localtunnel.me/courses/?format=json&prefix=" + argPrefix
                            + "&number=" + argNumber;
                } else {
                    myUrl = "https://vtebscefip.localtunnel.me/courses/?format=json";
                }

                URL url = new URL(myUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "JWT " + token);

                try {
                    response = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    reader.close();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }

                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            jsonData = stringBuilder.toString();
            parse();

            if (tvCourse.getText().toString().isEmpty()) {
                tvStatus.setText("Finished searching. No results found");
                tvStatus.setTextColor(Color.RED);
            } else {
                tvStatus.setText("Finished searching. Result(s) shown");
                tvStatus.setTextColor(Color.GREEN);
            }

        }
    }

}