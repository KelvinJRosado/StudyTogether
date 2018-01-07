package com.example.kelvin.testcapstone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

public class LoginActivity extends AppCompatActivity {

    static String username;
    static String token = "";
    static int userPK;
    static String userFirstName = "";
    //GUI elements
    EditText editUser, editPass;
    Button btLogin, btRegister;
    Context thisContext;
    TextView tvError;
    //Vars for Remember me option
    CheckBox cbRemember;
    private SharedPreferences loginPrefs;
    private SharedPreferences.Editor loginPrefsEditor;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        thisContext = this;
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

    public void initGui() {
        //Initialize GUI elements
        editUser = (EditText) findViewById(R.id.editUsername);
        editPass = (EditText) findViewById(R.id.editPassword);
        editPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        btLogin = (Button) findViewById(R.id.btLogin);
        btRegister = (Button) findViewById(R.id.btRegister);
        tvError = (TextView) findViewById(R.id.tvError);
        cbRemember = (CheckBox) findViewById(R.id.cbRememberMe);
        setTitle("Login");

        //Remember me setup
        loginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        boolean saveLogin = loginPrefs.getBoolean("saveLogin", false);
        if (saveLogin) {
            editUser.setText(loginPrefs.getString("username", ""));
            editPass.setText(loginPrefs.getString("password", ""));
            cbRemember.setChecked(true);
        }

        btLogin.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                username = editUser.getText().toString();
                password = editPass.getText().toString();
                hideKeyboard();

                //Make sure Credentials are provided
                if (username.isEmpty() || password.isEmpty()) {
                    tvError.setText("Credentials not provided. Error");
                    tvError.setTextColor(Color.RED);
                    return;
                }
                //Show message showing credentials are being checked
                else {
                    tvError.setText("Verifying credentials. Please wait");
                    tvError.setTextColor(Color.BLUE);
                }

                login();

            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, RegisterActivity.class);
                startActivity(mainIntent);
            }
        });

    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void login() {
        new GetTokenTask().execute();
    }

    @Override
    public void onBackPressed() {
    }

    private class GetTokenTask extends AsyncTask<String, Void, String> {

        boolean errorIO = false, errorJSON = false;
        boolean invalidLogin = false;

        @Override
        protected String doInBackground(String... params) {
            //Authenticate
            try {
                //Connect to url
                URL url = new URL("http://52.2.157.47:8000/api-token-auth/");

                //Store arguments
                Map<String, Object> mapParams = new LinkedHashMap<>();
                mapParams.put("username", username);
                mapParams.put("password", password);

                //Make StringBuilder object of POST data
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
                connect.setRequestMethod("POST");
                connect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connect.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                connect.setDoOutput(true);
                connect.getOutputStream().write(postDataBytes);

                if (connect.getResponseCode() == 400) {
                    invalidLogin = true;
                    return null;
                }

                Reader input = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));

                //Get token as String
                StringBuilder sb = new StringBuilder();
                for (int c; (c = input.read()) >= 0; )
                    sb.append((char) c);
                token = sb.toString();

                JSONObject jwtToken = new JSONObject(token);
                token = jwtToken.getString("token");

                //Put token in SharedPreferences
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("token", token);
                editor.commit();

                connect.disconnect();

            } catch (JSONException e) {
                e.printStackTrace();
                errorJSON = true;
                errorIO = false;
                return null;
            } catch (IOException e) {
                //Invalid credentials
                e.printStackTrace();
                errorIO = true;
                errorJSON = false;
                return null;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

            if (!isConnectedToInternet()) {
                tvError.setText("Device not connected to internet. Try again later");
                tvError.setTextColor(Color.RED);
                this.cancel(true);
            } else {
                super.onPreExecute();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (invalidLogin) {
                tvError.setText("Invalid credentials. Try again");
                tvError.setTextColor(Color.RED);
                return;
            }

            if (errorJSON || errorIO) {
                tvError.setText("Unable to access server. Try again later");
                tvError.setTextColor(Color.RED);
                return;
            }

            //Save credentials if desired
            if (cbRemember.isChecked()) {
                loginPrefsEditor = loginPrefs.edit();
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", username);
                loginPrefsEditor.putString("password", password);
                loginPrefsEditor.apply();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
            }

            //Get user pk and store for future use
            new GetPKTask().execute();

        }
    }

    private class GetPKTask extends AsyncTask<String, Void, String> {

        StringBuilder stringBuilder;
        int response;

        JSONObject pkJson;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String myUrl = "http://52.2.157.47:8000/users/" + username + "/";

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

            String accountInfo = stringBuilder.toString();

            try {
                pkJson = new JSONObject(accountInfo);
                userPK = pkJson.getInt("id");
                userFirstName = pkJson.getString("first_name");

                //Put pk in SharedPreferences
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("userPk", userPK);
                editor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Now authenticated, go to main activity
            Intent mainIntent = new Intent(thisContext, ContentsActivity.class);
            startActivity(mainIntent);

        }
    }
}
