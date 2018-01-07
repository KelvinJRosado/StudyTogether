package com.example.kelvin.testcapstone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.ArrayList;

public class ViewGroupsActivity extends AppCompatActivity {

    Button btReturn;
    TextView tvGroups;
    Context thisContext = this;
    String token = LoginActivity.token;
    int studentPk = LoginActivity.userPK;

    //ArrayList of Study Groups
    ArrayList<StudyGroup> myGroups;

    //Layout
    LinearLayout groupLayout;

    @Override
    public void onBackPressed() {
        btReturn.performClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_groups);
        setTitle("Your Study Groups");
        initGui();

        tvGroups.setText("Fetching groups. Please wait");
        tvGroups.setTextColor(Color.BLUE);

        new getGroupPKs().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPreferences.getString("token", token);
        studentPk = sharedPreferences.getInt("userPk", studentPk);
    }

    void initGui() {
        btReturn = (Button) findViewById(R.id.btGroupsReturn);
        tvGroups = (TextView) findViewById(R.id.tvGroupsView);
        //tvGroups.setMovementMethod(new ScrollingMovementMethod());
        groupLayout = (LinearLayout) findViewById(R.id.LayoutGroupsList);
        myGroups = new ArrayList<>();

        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, ContentsActivity.class);
                startActivity(mainIntent);
            }
        });

        final Scroller scroller = new Scroller(thisContext);
        tvGroups.setScroller(scroller);
        tvGroups.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gesture = new GestureDetector(thisContext,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float veloX, float veloY) {
                            scroller.fling(0, tvGroups.getScrollY(), 0, (int) -veloY, 0, 0, 0,
                                    (tvGroups.getLineCount() * tvGroups.getLineHeight()));
                            return super.onFling(e1, e2, veloX, veloY);
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gesture.onTouchEvent(event);
                return false;
            }
        });
    }

    //Get pk of every group user is part of
    private class getGroupPKs extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;
        String jsonString;

        @Override
        protected String doInBackground(String... params) {

            try {
                String myUrl = "http://52.2.157.47:8000/groupmembership/"
                        + "?student=" + studentPk;

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
                JSONArray groupsJson = new JSONArray(jsonString);

                //Get list of all groups user is part of
                for (int i = 0; i < groupsJson.length(); i++) {
                    JSONObject memberPair = groupsJson.getJSONObject(i);
                    int groupPk = memberPair.getInt("group");
                    StudyGroup group = new StudyGroup(groupPk);
                    myGroups.add(group);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            new getGroupCoursePk().execute();

        }
    }

    private class getGroupCoursePk extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            //Go one by one through ArrayList and add course pk to StudyGroup objects
            for (int i = 0; i < myGroups.size(); i++) {
                StudyGroup group = myGroups.get(i);
                int pk = group.getPk();
                stringBuilder = new StringBuilder();

                try {
                    String myUrl = "http://52.2.157.47:8000/groups/?id=" + pk;
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

                    JSONArray jsonArray = new JSONArray(jsonString);

                    JSONObject info = jsonArray.getJSONObject(0);

                    group.coursePk = info.getInt("course");

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new getCourseInfoDetail().execute();
        }
    }

    private class getCourseInfoDetail extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            try {
                //Iterate through Student's groups
                for (int i = 0; i < myGroups.size(); i++) {
                    StudyGroup group = myGroups.get(i);
                    int pk = group.coursePk;

                    String myUrl = "http://52.2.157.47:8000/courses/"
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

                    String jsonString = stringBuilder.toString();

                    JSONObject course = new JSONObject(jsonString);

                    String ss = course.get("prefix") + " " + course.get("number")
                            + ": " + course.get("title");

                    group.addCourse(ss);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new getGroupMemberPks().execute();
        }
    }

    private class getGroupMemberPks extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < myGroups.size(); i++) {
                StudyGroup group = myGroups.get(i);
                int groupPk = group.getPk();
                stringBuilder = new StringBuilder();

                try {
                    String myUrl = "http://52.2.157.47:8000/groupmembership/?group=" + groupPk;
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
                        JSONObject student = array.getJSONObject(k);
                        int memberPk = student.getInt("student");

                        if (memberPk == studentPk) {
                            continue;
                        }

                        StudyGroupMember member = new StudyGroupMember(memberPk);
                        group.addMember(member);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new getGroupMemberDetails().execute();
        }
    }

    private class getGroupMemberDetails extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            //Iterate through groups and through members in those groups
            for (int i = 0; i < myGroups.size(); i++) {
                StudyGroup group = myGroups.get(i);

                for (int k = 0; k < group.groupMembers.size(); k++) {
                    StudyGroupMember member = group.groupMembers.get(k);
                    int memberPk = member.getPk();
                    stringBuilder = new StringBuilder();

                    try {
                        String myUrl = "http://52.2.157.47:8000/users/" + memberPk;
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

                        String email = studentJson.getString("email");
                        String firstName = studentJson.getString("first_name");
                        String lastName = studentJson.getString("last_name");
                        String name = firstName + " " + lastName;

                        member.addEmail(email);
                        member.addName(name);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            for (int i = 0; i < myGroups.size(); i++) {
                TextView display = new TextView(thisContext);
                StudyGroup group = myGroups.get(i);
                display.setText(group.toString());
                display.setTextColor(Color.BLACK);
                groupLayout.addView(display);
            }

            tvGroups.setText("Showing Groups:");
            tvGroups.setTextColor(Color.BLACK);
        }
    }

}
