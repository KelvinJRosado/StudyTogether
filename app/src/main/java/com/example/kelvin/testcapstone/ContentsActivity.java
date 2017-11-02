package com.example.kelvin.testcapstone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class ContentsActivity extends AppCompatActivity {

    Button btCourseSearch, btLogout, btAddCourse, btViewCourses, btRemoveCourse;
    Button btViewGroups, btViewClassmates;
    Context thisContext;
    String token = LoginActivity.token;
    int userPk = LoginActivity.userPK;
    //String username = LoginActivity.username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);
        setTitle("Welcome");
        thisContext = this;
        hideKeyboard();
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPreferences.getString("token", token);
        userPk = sharedPreferences.getInt("userPk", userPk);
    }

    void initGui() {
        btCourseSearch = (Button) findViewById(R.id.btCourseSearch);
        btAddCourse = (Button) findViewById(R.id.btAddCourse);
        btViewCourses = (Button) findViewById(R.id.btViewCourses);
        btRemoveCourse = (Button) findViewById(R.id.btContentsRemoveCourse);
        btLogout = (Button) findViewById(R.id.btLogout);
        btViewGroups = (Button) findViewById(R.id.btContentsViewGroups);
        btViewClassmates = (Button) findViewById(R.id.btContentsClassmates);

        btCourseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, LoginActivity.class);
                startActivity(mainIntent);
            }
        });

        btAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, AddCourseActivity.class);
                startActivity(mainIntent);
            }
        });

        btViewCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(thisContext, ViewCoursesActivity.class);
                startActivity(mainIntent);
            }
        });

        btRemoveCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(thisContext, RemoveCourseActivity.class);
                startActivity(myIntent);
            }
        });

        btViewGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(thisContext, ViewGroupsActivity.class);
                startActivity(myIntent);
            }
        });

        btViewClassmates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(thisContext, ViewClassmatesActivity.class);
                startActivity(myIntent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        btLogout.performClick();
    }

}
