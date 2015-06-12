package com.bhavanavennamaneni.courseportal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class PostActivity extends AppCompatActivity {

    private ArrayList<String> selectedCourse, enrolledIdList, emailList;
    private Spinner courseSpinner;
    private ArrayMap<String, String> courseMap;
    private EditText postSubject, postContent;
    private RadioGroup e_Type;
    private Boolean isPost;
    private int postSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        courseSpinner = (Spinner) findViewById(R.id.courseList);
        postSubject = (EditText) findViewById(R.id.post_subject);
        postContent = (EditText) findViewById(R.id.post_content);
        e_Type = (RadioGroup) findViewById(R.id.radioType);
        setDefaultEnrollmentType();

        postSubject.setHorizontallyScrolling(false);
        getSupportActionBar().setTitle("Compose Post");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));

        Button clear = (Button) findViewById(R.id.post_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseSpinner.setSelection(0);
                postSubject.setText("");
                postContent.setText("");
                setDefaultEnrollmentType();
            }
        });

        if (isNetworkConnected()) {
            loadCourseSpinner();
            Button post_button = (Button) findViewById(R.id.post_a_button);
            post_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isPost = true;
                    validatePost();
                }
            });
            Button email_button = (Button) findViewById(R.id.post_e_button);
            email_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isPost = false;
                    validatePost();
                }
            });
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }

    private void setDefaultEnrollmentType() {
        RadioButton radioButton = (RadioButton) findViewById(R.id.radio_all);
        radioButton.setChecked(true);
    }

    private void loadCourseSpinner() {
        courseMap = new ArrayMap<>();
        ArrayList<String> list;
        ArrayList<String> courseArrayList = new ArrayList<>();
        courseArrayList.add("All");
        list = getIntent().getStringArrayListExtra("CourseList");
        for (int i = 0; i < list.size(); i++) {
            String[] temp = list.get(i).split(":");
            courseMap.put(temp[1], temp[0]);
            courseArrayList.add(temp[1]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, courseArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter);
    }

    private void validatePost() {
        selectedCourse = new ArrayList<>();
        postSuccess = 0;

        String selected = courseSpinner.getSelectedItem().toString();
        if (selected.equals("All")) {
            String[] cId = courseMap.values().toArray(new String[courseMap.size() - 1]);
            Collections.addAll(selectedCourse, cId);
        } else {
            String c_Id = courseMap.get(selected);
            selectedCourse.add(c_Id);
        }

        int checked = e_Type.getCheckedRadioButtonId();
        RadioButton enrollType = (RadioButton) findViewById(checked);
        String selectedType = enrollType.getText().toString();
        String subject = postSubject.getText().toString();
        String content = postContent.getText().toString();
        if (!TextUtils.isEmpty(subject)) {
            if (!TextUtils.isEmpty(content)) {
                if (isPost) {
                    for (int j = 0; j < selectedCourse.size(); j++) {
                        sendPost(subject, content, selectedCourse.get(j), selectedType);
                    }
                } else {
                    getEnrolledList(selectedType);
                }
            } else {
                postContent.setError("Content field is Empty");
                postContent.requestFocus();
            }
        } else {
            postSubject.setError("Subject field is Empty");
            postSubject.requestFocus();
        }
    }

    private void sendPost(String subject, String content, String courseId, String type) {
        ParseObject post = new ParseObject("Notification");
        post.put("subject", subject);
        post.put("content", content);
        post.put("c_id", courseId);
        post.put("userGroup", type);
        post.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    postSuccess = postSuccess + 1;
                    onPostFinished();
                } else {
                    Toast.makeText(getBaseContext(), "Unsuccessful, Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onPostFinished() {
        if (postSuccess == selectedCourse.size()) {
            postSubject.setText("");
            postContent.setText("");
            courseSpinner.setSelection(0);
            setDefaultEnrollmentType();
            Toast.makeText(getBaseContext(), "Announcement Posted Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void getEnrolledList(String type) {
        enrolledIdList = new ArrayList<>();
        String[] courses = new String[selectedCourse.size()];
        courses = selectedCourse.toArray(courses);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Enrollment");
        query.whereContainedIn("CourseId", Arrays.asList(courses));
        if (!type.equals("All")) {
            query.whereEqualTo("Type", type);
        } else {
            query.whereNotEqualTo("Type", "Instructor");
        }
        query.selectKeys(Arrays.asList("CourseId", "RedId", "Type"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objList.size(); i++) {
                        ParseObject obj = objList.get(i);
                        if (enrolledIdList.size() != 0) {
                            for (int j = 0; j < enrolledIdList.size(); j++) {
                                String e_id = enrolledIdList.get(j);
                                if (!e_id.equals(obj.getString("RedId"))) {
                                    enrolledIdList.add(obj.getString("RedId"));
                                } else {
                                    break;
                                }
                            }
                        } else {
                            enrolledIdList.add(obj.getString("RedId"));
                        }
                    }
                    getEmailAddress();
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void getEmailAddress() {
        emailList = new ArrayList<>();
        String[] id = new String[enrolledIdList.size()];
        id = enrolledIdList.toArray(id);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserData");
        query.whereContainedIn("redId", Arrays.asList(id));
        query.selectKeys(Arrays.asList("redId", "emailId"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objList.size(); i++) {
                        ParseObject obj = objList.get(i);
                        emailList.add(obj.getString("emailId"));
                    }
                    sendEmail();
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void sendEmail() {
        String[] emailId = new String[emailList.size()];
        emailId = emailList.toArray(emailId);
        String subject = postSubject.getText().toString();
        String content = postContent.getText().toString();

        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("application/octet-stream");
        email.putExtra(Intent.EXTRA_BCC, emailId);
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, content);
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose Email Account:"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        backToMainActivity();
        return super.onOptionsItemSelected(item);
    }

    private void backToMainActivity() {
        Intent back = new Intent(PostActivity.this, MainActivity.class);
        back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        back.putExtra("login", true);
        startActivity(back);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToMainActivity();
    }
}
