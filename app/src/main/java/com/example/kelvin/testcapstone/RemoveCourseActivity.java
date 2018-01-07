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

public class RemoveCourseActivity extends AppCompatActivity {

    EditText editPrefix, editNumber;
    Button btSubmit, btReturn;
    Context thisContext = this;

    String coursePrefix = "";
    String courseNumber = "";

    String token = LoginActivity.token;
    int studentPk = LoginActivity.userPK;
    int coursePk = 0;//Set later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_course);
        setTitle("Remove Course from profile");
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
        btReturn.performClick();
    }

    void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void initGui() {
        editNumber = (EditText) findViewById(R.id.editTextRemoveCourseNumber);
        editPrefix = (EditText) findViewById(R.id.editTextRemoveCoursePrefix);
        btReturn = (Button) findViewById(R.id.btRemoveCourseReturn);
        btSubmit = (Button) findViewById(R.id.btRemoveCourseSubmit);

        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNumber.setText("");
                editPrefix.setText("");

                Intent myIntent = new Intent(thisContext, ContentsActivity.class);
                startActivity(myIntent);
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                courseNumber = editNumber.getText().toString();
                coursePrefix = editPrefix.getText().toString();

                hideKeyboard();

                if (false) {
                    Toast.makeText(thisContext, "Invalid characters. Try again", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (coursePrefix.isEmpty() || courseNumber.isEmpty()) {
                    Toast.makeText(thisContext, "Course Prefix and Number are required. Error",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                new GetCourseID().execute();
            }
        });
    }

    private class DeleteFromSchedule extends AsyncTask<String, Void, String> {

        int status = 0;

        @Override
        protected String doInBackground(String... params) {
            //Authenticate
            try {
                //Connect to url
                URL url = new URL("http://52.2.157.47:8000/removecourse/?format=json");

                //Store arguments
                Map<String, Object> mapParams = new LinkedHashMap<>();
                mapParams.put("student", studentPk);
                mapParams.put("courses", coursePk);

                //Make StringBuilder object of DELETE data
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : mapParams.entrySet()) {
                    if (postData.length() != 0)
                        postData.append('&');//Separate args with & char
                    //Append username and password and encode
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }

                //Get postData as bytes
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                //Open connection
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();

                //Write username and password and get token
                connect.setRequestMethod("DELETE");
                connect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connect.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                connect.setRequestProperty("Authorization", "JWT " + token);
                connect.setDoOutput(true);
                connect.getOutputStream().write(postDataBytes);
                status = connect.getResponseCode();
                Reader input = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));

                //Get token as String
                StringBuilder sb = new StringBuilder();
                for (int c; (c = input.read()) >= 0; )
                    sb.append((char) c);

                String ss = sb.toString();
                System.out.print(ss);

                connect.disconnect();

            } catch (IOException e) {
                //Error
                e.printStackTrace();
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (status == 404) {
                Toast.makeText(thisContext, "Error. Course not in your profile", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            //Success removing
            if (status == 204) {
                String message = "Successfully removed " + coursePrefix.toUpperCase() +
                        " " + courseNumber + " from your profile";
                Toast.makeText(thisContext, message, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class GetCourseID extends AsyncTask<String, Void, String> {

        int response = 0;
        StringBuilder stringBuilder;

        @Override
        protected String doInBackground(String... params) {

            String myUrl = "http://52.2.157.47:8000/courses/?format=json&prefix=" + coursePrefix
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

                coursePk = courseJSON.getInt("course_id");

                //Use course id parsed here to remove from schedule
                new DeleteFromSchedule().execute();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
