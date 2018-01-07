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
import android.widget.Button;
import android.widget.Scroller;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ViewCoursesActivity extends AppCompatActivity {

    Button btReturn;
    TextView tvInfo;
    StringBuilder courseList;
    ArrayList<Integer> coursePkList;
    String jsonData;
    ArrayList<String> courseJsonData;

    int studentPK = LoginActivity.userPK;
    String token = LoginActivity.token;
    Context thisContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_courses);
        setTitle("Your Courses");
        initGui();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPreferences.getString("token", token);
        studentPK = sharedPreferences.getInt("userPk", studentPK);
    }

    public void onBackPressed() {
        btReturn.performClick();
    }

    void initGui() {
        btReturn = (Button) findViewById(R.id.btViewCoursesReturn);
        tvInfo = (TextView) findViewById(R.id.tvViewCoursesInfo);
        tvInfo.setMovementMethod(new ScrollingMovementMethod());
        courseList = new StringBuilder();
        courseList.append("Your courses:\n");
        coursePkList = new ArrayList<>();
        courseJsonData = new ArrayList<>();

        final Scroller scroller = new Scroller(thisContext);
        tvInfo.setScroller(scroller);
        tvInfo.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gesture = new GestureDetector(thisContext,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float veloX, float veloY) {
                            scroller.fling(0, tvInfo.getScrollY(), 0, (int) -veloY, 0, 0, 0,
                                    (tvInfo.getLineCount() * tvInfo.getLineHeight()));
                            return super.onFling(e1, e2, veloX, veloY);
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gesture.onTouchEvent(event);
                return false;
            }
        });

        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, ContentsActivity.class);
                startActivity(mainIntent);
            }
        });

        tvInfo.setText("Retrieving schedule. Please wait");
        tvInfo.setTextColor(Color.BLUE);
        connectGetPk();
    }

    void connectGetPk() {
        new GetSchedulePK().execute();
    }

    void connectGetDetails() {
        new GetScheduleDetails().execute();
    }

    void parse() {
        try {

            //Get JSON array of all courses with given
            JSONArray allCourses = new JSONArray(jsonData);

            for (int i = 0; i < allCourses.length(); i++) {
                //Get course info, except sections
                JSONObject course = allCourses.getJSONObject(i);
                int pk = course.getInt("courses");

                coursePkList.add(pk);

            }

            //tvCourse.setText(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        connectGetDetails();
    }

    void secondParse() {

        for (String ss : courseJsonData) {
            try {
                JSONArray array = new JSONArray(ss);
                JSONObject json = array.getJSONObject(0);
                String prefix = json.getString("prefix");
                String number = json.getString("number");
                String title = json.getString("title");

                String course = prefix + " " + number + ": " + title;

                courseList.append(course).append("\n");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (courseList.toString().equals("Your courses:\n")) {
            tvInfo.setText("No courses on profile");
            tvInfo.setTextColor(Color.RED);
            return;
        }

        tvInfo.setTextColor(Color.BLACK);
        tvInfo.setText(courseList.toString());

    }

    private class GetSchedulePK extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;
        int response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String myUrl = "http://52.2.157.47:8000/schedule/?format=json&student="
                        + studentPK;

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

                jsonData = stringBuilder.toString();

                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            parse();
        }

    }

    private class GetScheduleDetails extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;
        int response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < coursePkList.size(); i++) {

                int currentPk = coursePkList.get(i);

                try {
                    String myUrl = "http://52.2.157.47:8000/courses/?format=json&course_id="
                            + currentPk;

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

                    courseJsonData.add(stringBuilder.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            secondParse();
        }

    }

}
