package com.example.kelvin.testcapstone;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    //Error messages
    final String DUPLICATE_USERNAME = "\"username\":[\"This field must be unique.\"]";
    final String SHORT_PASSWORD = "\"password\":[\"Ensure this field has at least 8 characters.\"]";
    final String INVALID_EMAIL = "\"email\":[\"Enter a valid email address.\"]";
    final String DUPLICATE_EMAIL = "{\"email\":[\"This field must be unique.\"]}";
    //GUI
    Button btSubmit, btReturn;
    EditText editUsername, editEmail, editPassword, editFirstName, editLastName;
    Context thisContext = this;
    SeekBar seekPrep, seekMotivation, seekContribution, seekDirection;
    TextView tvPrep, tvMotivation, tvContribution, tvDirection;
    //Server response
    JSONObject jsonResponse;
    //Register params
    String username, email, password, firstName, lastName;
    String response;
    int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Create Account");
        initGui();
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
        editEmail = (EditText) findViewById(R.id.editEmailRegister);
        editUsername = (EditText) findViewById(R.id.editUsernameRegister);
        editPassword = (EditText) findViewById(R.id.editPasswordRegister);
        editFirstName = (EditText) findViewById(R.id.editFirstNameRegister);
        editLastName = (EditText) findViewById(R.id.editLastNameRegister);
        btSubmit = (Button) findViewById(R.id.btRegisterSubmit);
        btReturn = (Button) findViewById(R.id.btRegisterReturn);
        seekContribution = (SeekBar) findViewById(R.id.editContributionRegister);
        seekDirection = (SeekBar) findViewById(R.id.editSelfDirectionRegister);
        seekMotivation = (SeekBar) findViewById(R.id.editMotivationRegister);
        seekPrep = (SeekBar) findViewById(R.id.editPrepRegister);
        tvContribution = (TextView) findViewById(R.id.tvContributionRegister);
        tvDirection = (TextView) findViewById(R.id.tvDirectionRegister);
        tvMotivation = (TextView) findViewById(R.id.tvMotivationRegister);
        tvPrep = (TextView) findViewById(R.id.tvPrepRegister);

        final String baseContribution = tvContribution.getText().toString();
        final String baseDirection = tvDirection.getText().toString();
        final String baseMotivation = tvMotivation.getText().toString();
        final String basePrep = tvPrep.getText().toString();

        editFirstName.requestFocus();

        //Clear everything on logout or on opening app
        editUsername.setText("");
        editEmail.setText("");
        editPassword.setText("");

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = editUsername.getText().toString();
                email = editEmail.getText().toString();
                password = editPassword.getText().toString();
                firstName = editFirstName.getText().toString();
                lastName = editLastName.getText().toString();

                //Prevent SQL injection
                if (Utility.validateUsername(username)) {
                    Toast.makeText(thisContext,
                            "Invalid characters in username. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Utility.validateEmail(email)) {
                    Toast.makeText(thisContext,
                            "Invalid characters in email. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Utility.preventAttack(password)) {
                    Toast.makeText(thisContext,
                            "Invalid characters in password. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Utility.preventAttack(firstName)) {
                    Toast.makeText(thisContext,
                            "Invalid characters in first name. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Utility.preventAttack(lastName)) {
                    Toast.makeText(thisContext,
                            "Invalid characters in last name. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Make sure email is GSU
                if (Utility.validateEmail(email)) {
                    Toast.makeText(thisContext,
                            "You must use a Georgia Southern email address. Try again",
                            Toast.LENGTH_SHORT).show();
                    return;
                }


                hideKeyboard();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() ||
                        firstName.isEmpty() || lastName.isEmpty()) {
                    Toast.makeText(thisContext, "All fields are required. Try again",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(thisContext, "Attempting to create account. Please wait",
                        Toast.LENGTH_SHORT).show();

                createUser();

            }
        });

        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, LoginActivity.class);
                startActivity(mainIntent);
            }
        });

        seekPrep.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPrep.setText(basePrep + ": " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideKeyboard();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekMotivation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMotivation.setText(baseMotivation + ": " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideKeyboard();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekDirection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDirection.setText(baseDirection + ": " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideKeyboard();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekContribution.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvContribution.setText(baseContribution + ": " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideKeyboard();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    void createUser() {
        new CreateUserTask().execute();
    }

    void addStats(){ new AddStatsTask().execute();}

    @Override
    public void onBackPressed() {
        btReturn.performClick();
    }

    private class CreateUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                String myUrl = "https://vtebscefip.localtunnel.me/register/";
                URL url = new URL(myUrl);

                Map<String, Object> mapParams = new LinkedHashMap<>();
                mapParams.put("username", username);
                mapParams.put("email", email);
                mapParams.put("password", password);
                mapParams.put("first_name", firstName);
                mapParams.put("last_name", lastName);

                //Make StringBuilder object of POST data
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : mapParams.entrySet()) {
                    if (postData.length() != 0)
                        postData.append('&');//Separate args with & char
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

            try {
                jsonResponse = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {studentId = jsonResponse.getInt("id");}
            catch (Exception e){e.printStackTrace();}
            response = jsonResponse.toString();

            //String Builder of errors, if any
            StringBuilder errorMessages = new StringBuilder();

            if (response.contains(DUPLICATE_USERNAME)) {
                errorMessages.append("Username already taken\n");
            }
            if (response.contains(SHORT_PASSWORD)) {
                errorMessages.append("Passwords must contain at least 8 characters\n");
            }
            if (response.contains(INVALID_EMAIL)) {
                errorMessages.append("Emails must be valid\n");
            }
            if (response.contains(DUPLICATE_EMAIL)) {
                errorMessages.append("Email already taken\n");
            }

            //There were errors
            if (!errorMessages.toString().isEmpty()) {
                String error = "Account creation failed with the following error(s):\n"
                        + errorMessages.toString();

                Toast.makeText(thisContext, error, Toast.LENGTH_LONG).show();
                return;
            }

            addStats();

        }
    }

    private class AddStatsTask extends AsyncTask<String, Void, String> {

        int prep = seekPrep.getProgress();
        int motivation = seekMotivation.getProgress();
        int direction = seekDirection.getProgress();
        int contribution = seekContribution.getProgress();
        int student = 0;

        @Override
        protected String doInBackground(String... params) {

            try {

                String myUrl = "https://vtebscefip.localtunnel.me/addstats/";
                URL url = new URL(myUrl);

                Map<String, Object> mapParams = new LinkedHashMap<>();
                mapParams.put("student", studentId);
                mapParams.put("prep", prep);
                mapParams.put("contribution", motivation);
                mapParams.put("direction", direction);
                mapParams.put("motivation", contribution);

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

            int x = 0;

            Toast.makeText(thisContext, "Account successfully created. Now please log in",
                    Toast.LENGTH_SHORT).show();

            Intent mainIntent = new Intent(thisContext, LoginActivity.class);
            startActivity(mainIntent);

        }
    }

}
