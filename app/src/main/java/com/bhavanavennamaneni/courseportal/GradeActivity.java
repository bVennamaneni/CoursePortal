package com.bhavanavennamaneni.courseportal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class GradeActivity extends AppCompatActivity {

    private String selectedCourseId, selectedTitle;
    private LinearLayout linearLayout;
    private ArrayMap<String, JSONObject> gradingMap;
    private ArrayMap<String, String> courseMap;
    private ArrayList<String> courseList;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);
        getSupportActionBar().setTitle(getString(R.string.title_activity_grade));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));
        courseMap = new ArrayMap<>();
        courseList = getIntent().getStringArrayListExtra("CourseList");
        radioGroup = (RadioGroup) findViewById(R.id.gradeRadioGroup);
        linearLayout = (LinearLayout) findViewById(R.id.gradeItemButtons);
        gradingMap = new ArrayMap<>();
        loadCourseRadioGroup();
    }

    private void loadCourseRadioGroup() {
        RadioButton radioButton;
        for (int i = 0; i < courseList.size(); i++) {
            radioButton = new RadioButton(this);
            String[] temp = courseList.get(i).split(":");
            courseMap.put(temp[1], temp[0]);
            radioButton.setText(temp[1]);
            radioButton.setTextAppearance(this, android.R.style.TextAppearance_Medium);
            radioGroup.addView(radioButton);
        }
        if (isNetworkConnected())
            getGradingObjects();
        else
            Toast.makeText(GradeActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    private void getGradingObjects() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Course");
        query.whereContainedIn("c_id", courseMap.values());
        query.selectKeys(Arrays.asList("grading", "c_id"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objList.size(); i++) {
                        ParseObject obj = objList.get(i);
                        JSONObject jsonObject = obj.getJSONObject("grading");
                        gradingMap.put(obj.getString("c_id"), jsonObject);
                    }
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                RadioButton selected = (RadioButton) findViewById(checked);
                selectedTitle = selected.getText().toString();
                selectedCourseId = courseMap.get(selectedTitle);
                loadGradeItems(selectedCourseId);
            }
        });
    }

    private void loadGradeItems(String courseId) {
        if (isNetworkConnected()) {
            linearLayout.removeAllViewsInLayout();
            List<String> keys = new ArrayList<>();
            final ArrayMap<String, String> keyValueMap = new ArrayMap();
            JSONObject gradeObject = gradingMap.get(courseId);

            Iterator<String> iterator = gradeObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                try {
                    String value = gradeObject.get(key).toString();
                    keys.add(key);
                    keyValueMap.put(key, value);
                } catch (JSONException e) {
                }
            }
            Collections.sort(keys);

            Button button;
            for (int i = 0; i < keys.size(); i++) {
                button = new Button(GradeActivity.this);
                button.setText(keys.get(i));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                button.setLayoutParams(params);
                linearLayout.addView(button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button button = (Button) view;
                        String key = button.getText().toString();
                        String value = keyValueMap.get(key);
                        onGradeItemClick(key, value);
                    }
                });
            }
        } else
            Toast.makeText(GradeActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
    }

    private void onGradeItemClick(String gradeItem, String totalPossible) {
        Intent intent = new Intent(GradeActivity.this, AddGradeActivity.class);
        intent.putExtra("GradeItem", gradeItem);
        intent.putExtra("TotalPossible", totalPossible);
        intent.putExtra("CourseId", selectedCourseId);
        intent.putExtra("CourseGrading", gradingMap.get(selectedCourseId).toString());
        intent.putExtra("CourseList", courseList);
        intent.putExtra("CourseTitle", selectedTitle);
        startActivity(intent);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
