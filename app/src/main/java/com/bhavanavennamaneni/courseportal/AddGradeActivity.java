package com.bhavanavennamaneni.courseportal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class AddGradeActivity extends AppCompatActivity {

    private ArrayList<String> enrolledList;
    private List<EditText> editTextList;
    private List<TextView> textViewList;
    private JSONObject courseGrading;
    private String gradeItem, courseId;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grade);
        enrolledList = new ArrayList<>();
        Intent intent = getIntent();
        courseId = intent.getStringExtra("CourseId");
        gradeItem = intent.getStringExtra("GradeItem");
        String title = intent.getStringExtra("CourseTitle");
        String totalPoints = intent.getStringExtra("TotalPossible");
        try {
            courseGrading = new JSONObject(intent.getStringExtra("CourseGrading"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        statusView = (TextView) findViewById(R.id.addGradeStatus);
        TextView courseTitle = (TextView) findViewById(R.id.addGradeTitle1);
        TextView totalPointsTitle = (TextView) findViewById(R.id.addGradeTitle2);
        courseTitle.setText(title);
        totalPointsTitle.setText("Total Possible Points: " + totalPoints);
        getSupportActionBar().setTitle(gradeItem);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));
        if (isNetworkConnected())
            getEnrolled(courseId);
        else
            statusView.setText(getString(R.string.network_error));

        Button done = (Button) findViewById(R.id.addGradeDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getEnrolled(String courseId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Enrollment");
        query.whereEqualTo("CourseId", courseId);
        query.orderByDescending("RedId");
        query.whereNotEqualTo("Type", "Instructor");
        query.selectKeys(Arrays.asList("RedId"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    if (objList.size() != 0) {
                        for (int i = 0; i < objList.size(); i++) {
                            ParseObject obj = objList.get(i);
                            enrolledList.add(obj.getString("RedId"));
                        }
                        createInputFields();
                    } else
                        statusView.setText("No Students Enrolled");
                } else {
                    statusView.setText(getString(R.string.network_error));
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void createInputFields() {
        TableLayout gradeTableLayout = (TableLayout) findViewById(R.id.addGradeLayout);
        EditText editText;
        TextView textView;
        Button save;
        editTextList = new ArrayList<>();
        textViewList = new ArrayList<>();
        for (int i = 0; i < enrolledList.size(); i++) {
            TableRow tableRow = new TableRow(AddGradeActivity.this);
            editText = (EditText) getLayoutInflater().inflate(R.layout.custom_edit_text, null);
            textView = (TextView) getLayoutInflater().inflate(R.layout.custom_text_view, null);
            save = new Button(AddGradeActivity.this);
            editTextList.add(editText);
            textViewList.add(textView);
            editText.setId(i + 1);
            textView.setId(i + 1);
            save.setId(i + 1);
            editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            textView.setText(enrolledList.get(i));
            save.setText(getString(R.string.title_save_button));
            tableRow.addView(textView);
            tableRow.addView(editText);
            tableRow.addView(save);
            gradeTableLayout.addView(tableRow);
            View line = new View(this);
            line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 5));
            line.setBackgroundColor(Color.parseColor("#C9C9C9"));
            gradeTableLayout.addView(line);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        int buttonId = view.getId();
                        for (int i = 0; i < editTextList.size(); i++) {
                            EditText input = editTextList.get(i);
                            TextView idView = textViewList.get(i);
                            if (buttonId == input.getId()) {
                                String id = idView.getText().toString();
                                String points = input.getText().toString();
                                if (!points.equals("")) {
                                    input.setText("");
                                    saveGrade(id, points);
                                } else {
                                    input.requestFocus();
                                    statusView.setText("Enter Points");
                                }
                            }
                        }
                    } else
                        statusView.setText(getString(R.string.network_error));
                }
            });
        }

        Button clear = (Button) findViewById(R.id.addGradeClear);
        clear.setVisibility(View.VISIBLE);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < editTextList.size(); i++) {
                    EditText editText = editTextList.get(i);
                    editText.setText("");
                    statusView.setText("");
                }
            }
        });
    }

    private void saveGrade(final String redId, final String points) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Grade");
        query.whereEqualTo("courseId", courseId);
        query.whereEqualTo("redId", redId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    if (objList.size() != 0) {
                        for (int i = 0; i < objList.size(); i++) {
                            ParseObject obj = objList.get(i);
                            JSONObject object = obj.getJSONObject("points");
                            Iterator<String> iterator = object.keys();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                if (key.equals(gradeItem)) {
                                    try {
                                        object.put(key, points);
                                    } catch (JSONException exception) {
                                    }
                                }
                            }
                            obj.put("points", object);
                            obj.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null)
                                        statusView.setText(redId + ": " + points + " Saved");
                                    else
                                        statusView.setText(redId + ": " + points + " \n Save failed, Try again");
                                }
                            });
                        }
                    } else {
                        addNewRow(redId, points);
                    }
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void addNewRow(final String redId, final String points) {
        Iterator<String> iterator = courseGrading.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.equals(gradeItem)) {
                try {
                    courseGrading.put(key, points);
                } catch (JSONException exception) {
                }
            } else {
                try {
                    courseGrading.put(key, "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ParseObject grade = new ParseObject("Grade");
        grade.put("courseId", courseId);
        grade.put("redId", redId);
        grade.put("points", courseGrading);
        grade.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    statusView.setText(redId + ": " + points + " Saved");
                } else {
                    statusView.setText(redId + ": " + points + " \n Save failed, Try again");
                }
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
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
