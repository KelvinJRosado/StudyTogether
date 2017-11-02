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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ViewClassmatesActivity extends AppCompatActivity {

    Context thisContext = this;
    Button btBack;
    TextView tvDisplay;

    int studentPk = LoginActivity.userPK;
    String token = LoginActivity.token;

    ArrayList<Integer> courses = new ArrayList<>();
    ArrayList<StudyGroup> courseDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_classmates);
        setTitle("Your Classmates");
        initGui();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPreferences.getString("token", token);
        studentPk = sharedPreferences.getInt("userPk", studentPk);
    }

    public void onBackPressed() {
        btBack.performClick();
    }

    void initGui() {
        btBack = (Button) findViewById(R.id.btClassmatesReturn);
        tvDisplay = (TextView) findViewById(R.id.tvClassmatesView);
        tvDisplay.setMovementMethod(new ScrollingMovementMethod());

        final Scroller scroller = new Scroller(thisContext);
        tvDisplay.setScroller(scroller);
        tvDisplay.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gesture = new GestureDetector(thisContext,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float veloX, float veloY) {
                            scroller.fling(0, tvDisplay.getScrollY(), 0, (int) -veloY, 0, 0, 0,
                                    (tvDisplay.getLineCount() * tvDisplay.getLineHeight()));
                            return super.onFling(e1, e2, veloX, veloY);
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gesture.onTouchEvent(event);
                return false;
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, ContentsActivity.class);
                startActivity(mainIntent);
            }
        });

        tvDisplay.setText("Fetching data. Please wait");
        tvDisplay.setTextColor(Color.BLUE);
        new getSchedulePks().execute();
    }

    private class getSchedulePks extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;
        String jsonString;

        @Override
        protected String doInBackground(String... params) {

            try {
                String myUrl = "https://vtebscefip.localtunnel.me/schedule/?student="
                        + studentPk;

                URL url = new URL(myUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "JWT " + token);

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    reader.close();

                } finally {
                    urlConnection.disconnect();
                }

                jsonString = stringBuilder.toString();

                return jsonString;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(thisContext, "An error occurred. Please try again later",
                        Toast.LENGTH_SHORT).show();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONArray data = new JSONArray(jsonString);

                for (int i = 0; i < data.length(); i++) {
                    JSONObject object = data.getJSONObject(i);
                    int coursePk = object.getInt("courses");
                    courses.add(coursePk);
                }

                new getCourseInfo().execute();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(thisContext, "An error occurred. Please try again later",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class getCourseInfo extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            try {
                for (int i = 0; i < courses.size(); i++) {
                    int pk = courses.get(i);

                    String myUrl = "https://vtebscefip.localtunnel.me/courses/"
                            + pk;

                    URL url = new URL(myUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Authorization", "JWT " + token);

                    try {
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

                    String json = stringBuilder.toString();
                    JSONObject obj = new JSONObject(json);

                    String ss = obj.get("prefix") + " " + obj.get("number")
                            + ": " + obj.get("title") + "\n";

                    //Bad naming; Used to represent Course, not study group
                    StudyGroup aCourse = new StudyGroup(0);
                    aCourse.addCourse(ss);
                    aCourse.coursePk = pk;

                    courseDetails.add(aCourse);

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            new getClassmatePks().execute();

        }
    }

    private class getClassmatePks extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < courseDetails.size(); i++) {
                StudyGroup myClass = courseDetails.get(i);
                int pk = myClass.coursePk;
                stringBuilder = new StringBuilder();

                try {
                    String myUrl = "https://vtebscefip.localtunnel.me/schedule/?courses=" + pk;

                    URL url = new URL(myUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Authorization", "JWT " + token);

                    try {

                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        reader.close();

                    } finally {
                        urlConnection.disconnect();
                    }

                    String jsonString = stringBuilder.toString();
                    JSONArray array = new JSONArray(jsonString);

                    for (int k = 0; k < array.length(); k++) {
                        JSONObject object = array.getJSONObject(k);
                        int memberPk = object.getInt("student");

                        if (memberPk == studentPk) {
                            continue;
                        }

                        StudyGroupMember classmate = new StudyGroupMember(memberPk);
                        myClass.addMember(classmate);
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new getClassmateDetails().execute();
        }
    }

    private class getClassmateDetails extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < courseDetails.size(); i++) {
                StudyGroup myCourse = courseDetails.get(i);

                for (int k = 0; k < myCourse.groupMembers.size(); k++) {
                    StudyGroupMember classmate = myCourse.groupMembers.get(k);
                    int memberPk = classmate.getPk();
                    stringBuilder = new StringBuilder();

                    try {
                        String myUrl = "https://vtebscefip.localtunnel.me/users/" + memberPk;
                        URL url = new URL(myUrl);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setRequestProperty("Authorization", "JWT " + token);

                        try {
                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line).append("\n");
                            }
                            reader.close();
                        } finally {
                            urlConnection.disconnect();
                        }

                        String jsonString = stringBuilder.toString();
                        JSONObject studentJson = new JSONObject(jsonString);

                        String firstName = studentJson.getString("first_name");
                        String lastName = studentJson.getString("last_name");
                        String name = firstName + " " + lastName;

                        classmate.addName(name);


                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            StringBuilder display = new StringBuilder();
            for (int i = 0; i < courseDetails.size(); i++) {
                StudyGroup aCourse = courseDetails.get(i);
                display.append(aCourse.displayForClass());
            }

            tvDisplay.setText(display.toString());
            tvDisplay.setTextColor(Color.BLACK);

        }
    }

}
