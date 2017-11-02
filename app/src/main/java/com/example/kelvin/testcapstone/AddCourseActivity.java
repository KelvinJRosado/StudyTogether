package com.example.kelvin.testcapstone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddCourseActivity extends AppCompatActivity {

    final String DUPLICATE_COURSE_ADDED = "{\"non_field_errors\":[\"The fields student, " +
            "courses must make a unique set.\"]}";
    Context thisContext = this;
    //    String username = LoginActivity.username;
    String token = LoginActivity.token;
    int userPK = LoginActivity.userPK;
    int course = 0;
    String coursePrefix = "";
    String courseNumber = "";
    EditText editPrefix, editNumber;
    Button btSubmit, btReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        setTitle("Add course to profile");
        initGUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPreferences.getString("token", token);
        userPK = sharedPreferences.getInt("userPk", userPK);
    }

    void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onBackPressed() {
        btReturn.performClick();
    }

    void initGUI() {
        btSubmit = (Button) findViewById(R.id.btAddCourseSubmit);
        btReturn = (Button) findViewById(R.id.btReturnAddCourse);
        editPrefix = (EditText) findViewById(R.id.addCoursePrefixEdit);
        editNumber = (EditText) findViewById(R.id.addCourseNumberEdit);

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coursePrefix = editPrefix.getText().toString();
                courseNumber = editNumber.getText().toString();

                hideKeyboard();

                if (!Utility.preventAttack(coursePrefix) || !Utility.preventAttack(courseNumber)) {
                    Toast.makeText(thisContext, "Invalid characters. Try again", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (courseNumber.isEmpty() || coursePrefix.isEmpty()) {
                    Toast.makeText(thisContext, "Course number and prefix are required", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                //If number and prefix provided
                new GetCourseID().execute();
            }
        });

        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, ContentsActivity.class);
                startActivity(mainIntent);
            }
        });

    }

    private class GetCourseID extends AsyncTask<String, Void, String> {

        int response = 0;
        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            String myUrl = "https://vtebscefip.localtunnel.me/courses/?format=json&prefix=" + coursePrefix
                    + "&number=" + courseNumber;

            try {
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
                    return null;
                } finally {
                    urlConnection.disconnect();
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Get course id

            if (stringBuilder.toString().equals("[]\n")) {
                Toast.makeText(thisContext, "Course Not found. Error", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONArray courseArray = new JSONArray(stringBuilder.toString());
                JSONObject courseJSON = courseArray.getJSONObject(0);

                course = courseJSON.getInt("course_id");

                System.out.print(course);

                //Use course id parsed here to add to schedule
                new AddCourse().execute();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class AddCourse extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected String doInBackground(String... params) {

            try {
                String myUrl = "https://vtebscefip.localtunnel.me/addcourse/";
                URL url = new URL(myUrl);

                Map<String, Object> mapParams = new LinkedHashMap<>();
                mapParams.put("student", userPK);
                mapParams.put("courses", course);

                //Make StringBuilder object of POST data
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : mapParams.entrySet()) {
                    if (postData.length() != 0)
                        postData.append('&');//Seperate args with & char
                    //Append username, password, and email and encode
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }

                //Get postData as bytes
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                //Open connection
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();

                //Write username and password and get token
                connect.setRequestMethod("POST");
                connect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connect.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                connect.setRequestProperty("Authorization", "JWT " + token);
                connect.setDoOutput(true);
                connect.getOutputStream().write(postDataBytes);

                Reader input = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));

                //Get response as String
                StringBuilder sb = new StringBuilder();
                for (int c; (c = input.read()) >= 0; )
                    sb.append((char) c);

                response = sb.toString();

                connect.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (response.contains(DUPLICATE_COURSE_ADDED)) {
                Toast.makeText(thisContext, "Course already exists on your schedule",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(thisContext, "Course successfully added", Toast.LENGTH_LONG).show();

        }
    }
}
