package com.bhavanavennamaneni.courseportal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
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


public class CourseActivity extends AppCompatActivity {

    private static final String PREF_FILE_NAME = "Current_User";
    private TextView postView, aboutView, gradeView, postTitle, aboutTitle, enrolledTitle, gradeTitle;
    private LinearLayout postLayout, aboutLayout, enrolledLayout, gradeLayout;
    private TableLayout enrolledTLayout, gradeTLayout;
    private ImageButton postDown, aboutDown, enrolledDown, gradeDown;
    private ListView enrolledListView;
    private ArrayMap<String, String> enrolledMap, keyValueMap;
    private int x, y, z, w;
    private String u_Role, enrollType, courseId, gradeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        postView = (TextView) findViewById(R.id.c_postView);
        aboutView = (TextView) findViewById(R.id.c_aboutView);
        postTitle = (TextView) findViewById(R.id.c_postTitle);
        aboutTitle = (TextView) findViewById(R.id.c_aboutTitle);
        postLayout = (LinearLayout) findViewById(R.id.c_postLayout);
        aboutLayout = (LinearLayout) findViewById(R.id.c_aboutLayout);
        enrolledTitle = (TextView) findViewById(R.id.c_enrolledTitle);
        enrolledLayout = (LinearLayout) findViewById(R.id.c_enrolledLayout);
        enrolledListView = (ListView) findViewById(R.id.c_enrolledList);
        enrolledTLayout = (TableLayout) findViewById(R.id.c_enrolledTLayout);
        gradeTitle = (TextView) findViewById(R.id.c_gradeTitle);
        gradeLayout = (LinearLayout) findViewById(R.id.c_gradeLayout);
        gradeTLayout = (TableLayout) findViewById(R.id.c_gradeTLayout);
        gradeView = (TextView) findViewById(R.id.c_gradeView);

        Intent intent = getIntent();
        courseId = intent.getStringExtra("CourseID");
        String courseNo = intent.getStringExtra("CourseNo");
        String courseTitle = intent.getStringExtra("CourseTitle");
        enrollType = intent.getStringExtra("CourseEnrollType");
        String courseInstructor = intent.getStringExtra("CourseInstructor");
        keyValueMap = new ArrayMap<>();
        String grading = "<b>Grade Criterion:</b><br/>";
        try {
            JSONObject courseGrading = new JSONObject(intent.getStringExtra("CourseGrading"));
            Iterator<String> iterator = courseGrading.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                try {
                    String value = courseGrading.get(key).toString();
                    grading += key + ": " + value + "<br/>";
                    keyValueMap.put(key, value);
                } catch (JSONException e) {
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setTitle(courseNo);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));

        SharedPreferences user = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        u_Role = user.getString("UserRole", "");
        if (isNetworkConnected()) {
            if (u_Role.equals("professor")) {
                enrolledMap = new ArrayMap<>();
                getEnrolled(courseId);
            } else {
                enrolledTLayout.setVisibility(View.GONE);
                enrolledLayout.setVisibility(View.GONE);
            }
            if (u_Role.equals("student")) {
                String redId = user.getString("UserId", "");
                getGrade(redId);
            } else {
                gradeTLayout.setVisibility(View.GONE);
                gradeLayout.setVisibility(View.GONE);
            }
            getPosts(courseId);
        } else {
            Toast.makeText(CourseActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
        aboutView.append(courseNo + "\n");
        aboutView.append(courseTitle + "\n");
        String instructor = "<b>Instructor: </b>" + courseInstructor;
        aboutView.append(Html.fromHtml(instructor) + "\n");
        aboutView.append(Html.fromHtml(grading));
    }

    private void getPosts(String courseId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Notification");
        query.whereEqualTo("c_id", courseId);
        if (!enrollType.equals("Instructor")) {
            query.whereContainedIn("userGroup", Arrays.asList("All", enrollType));
        }
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    if (objList.size() != 0) {
                        for (int i = 0; i < objList.size(); i++) {
                            ParseObject obj = objList.get(i);
                            String title = "<b>Posted on:</b>";
                            String subject = "<b>" + obj.getString("subject") + "</b>";
                            String title1 = "<b>Posted to:</b>";
                            postView.append(Html.fromHtml(subject));
                            postView.append("\n");
                            postView.append(Html.fromHtml(title));
                            postView.append("\t" + obj.getCreatedAt().toString() + "\n");
                            postView.append(Html.fromHtml(title1));
                            postView.append("\t" + obj.getString("userGroup") + "\n");
                            postView.append(obj.getString("content"));
                            if (i != objList.size() - 1) {
                                postView.append("\n\n");
                            }
                        }
                    } else {
                        postView.setText(getString(R.string.title_no_posts));
                    }
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void getEnrolled(String courseId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Enrollment");
        query.whereEqualTo("CourseId", courseId);
        query.orderByDescending("RedId");
        query.whereNotEqualTo("Type", "Instructor");
        query.selectKeys(Arrays.asList("RedId", "Type"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objList.size(); i++) {
                        ParseObject obj = objList.get(i);
                        enrolledMap.put(obj.getString("RedId"), obj.getString("Type"));
                    }
                    loadEnrolledListView();
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void loadEnrolledListView() {
        if (!enrolledMap.isEmpty()) {
            String[] id = new String[enrolledMap.size()];
            id = enrolledMap.keySet().toArray(id);
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.list_item, id);
            enrolledListView.setAdapter(arrayAdapter);
            enrolledListView.setDividerHeight(1);
            setListViewHeight(enrolledListView);
            enrolledListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String selected = enrolledListView.getItemAtPosition(i).toString();
                    if (isNetworkConnected()) {
                        getGrade(selected);
                        getStudentData(selected);
                    } else
                        Toast.makeText(CourseActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String[] id = new String[]{getString(R.string.title_no_students)};
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.list_item, id);
            enrolledListView.setAdapter(arrayAdapter);
        }
    }

    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        float scale = getResources().getDisplayMetrics().density;
        int dpInPixels = (int) (5 * scale + 0.5f);
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight() + dpInPixels;
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void getGrade(String RedId) {
        JSONObject studentGrade;
        gradeString = "";
        List<String> list = new ArrayList<>();
        ParseQuery getGrade = new ParseQuery("Grade");
        getGrade.whereEqualTo("courseId", courseId);
        getGrade.whereEqualTo("redId", RedId);
        getGrade.selectKeys(Arrays.asList("points"));
        try {
            ParseObject grade = getGrade.getFirst();
            studentGrade = grade.getJSONObject("points");
            /* Storing the Grading JSONObject keys, to sort before displaying */
            Iterator<String> iterator = studentGrade.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                list.add(key);
            }
            Collections.sort(list);
            for (int i = 0; i < list.size(); i++) {
                String key = list.get(i);
                String total = keyValueMap.get(key);
                try {
                    String value = studentGrade.get(key).toString();
                    gradeString += key + ": " + value + "/" + total + "<br/>";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (u_Role.equals("student")) {
                gradeView.setText(Html.fromHtml(gradeString));
            }
        } catch (ParseException e) {
            gradeView.setText(getString(R.string.title_no_grades));
            e.printStackTrace();
        }
    }

    private void getStudentData(String RedId) {
        ParseQuery getStudent = new ParseQuery("UserData");
        getStudent.whereEqualTo("redId", RedId);
        try {
            ParseObject s_data = getStudent.getFirst();
            String name = s_data.getString("firstName") + " " + s_data.getString("lastName");
            String title = enrolledMap.get(RedId);
            String email = s_data.getString("emailId");
            displayStudent(RedId, name, title, email, gradeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void displayStudent(String id, String name, String title, String email, String grade) {
        final TextView display = new TextView(this);
        ScrollView scrollView = new ScrollView(this);

        String userData = "<b>Name: </b>" + name + "<br/><br/>" + "<b>Enrollment: </b>" + title;
        userData += "<br/><br/><b>Email Id: </b>" + email + "<br/><br/><b>Grade:</b><br/>" + grade;
        display.setTextAppearance(this, R.style.Base_TextAppearance_AppCompat_Medium);
        display.setTextColor(Color.BLACK);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (10 * scale + 0.5f);
        display.setPadding(2 * dpAsPixels, dpAsPixels, 2 * dpAsPixels, dpAsPixels);
        display.setText(Html.fromHtml(userData));
        display.append("\n");
        scrollView.addView(display);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(id)
                .setView(scrollView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                });
        AlertDialog user_dialog = builder.create();
        user_dialog.show();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        user_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        x = 0;
        y = 1;

        postLayout.setVisibility(View.GONE);
        aboutLayout.setVisibility(View.VISIBLE);

        postDown = (ImageButton) findViewById(R.id.c_downPost);
        aboutDown = (ImageButton) findViewById(R.id.c_downAbout);
        aboutDown.setImageResource(R.drawable.right);

        postDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postOnClick();
            }
        });
        aboutDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutOnClick();
            }
        });
        postTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postOnClick();
            }
        });
        aboutTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutOnClick();
            }
        });

        if (u_Role.equals("professor")) {
            z = 0;
            enrolledLayout.setVisibility(View.GONE);
            enrolledDown = (ImageButton) findViewById(R.id.c_downEnrolled);
            enrolledDown.setImageResource(R.drawable.down);
            enrolledDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enrolledOnClick();
                }
            });
            enrolledTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enrolledOnClick();
                }
            });
        }

        if (u_Role.equals("student")) {
            w = 0;
            gradeLayout.setVisibility(View.GONE);
            gradeDown = (ImageButton) findViewById(R.id.c_downGrade);
            gradeDown.setImageResource(R.drawable.down);
            gradeDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gradeOnClick();
                }
            });
            gradeTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gradeOnClick();
                }
            });
        }
    }

    private void postOnClick() {
        if (x == 0) {
            postDown.setImageResource(R.drawable.right);
            aboutDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.VISIBLE);
            aboutLayout.setVisibility(View.GONE);
            x = 1;
            y = 0;
            if (u_Role.equals("professor")) {
                z = 0;
                enrolledDown.setImageResource(R.drawable.down);
                enrolledLayout.setVisibility(View.GONE);
            } else if (u_Role.equals("student")) {
                w = 0;
                gradeDown.setImageResource(R.drawable.down);
                gradeLayout.setVisibility(View.GONE);
            }
        } else {
            postDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.GONE);
            x = 0;
            if (u_Role.equals("professor")) {
                enrolledLayout.setVisibility(View.GONE);
            } else if (u_Role.equals("student")) {
                gradeLayout.setVisibility(View.GONE);
            }
        }
    }

    private void aboutOnClick() {
        if (y == 0) {
            aboutDown.setImageResource(R.drawable.right);
            postDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.VISIBLE);
            y = 1;
            x = 0;
            if (u_Role.equals("professor")) {
                z = 0;
                enrolledDown.setImageResource(R.drawable.down);
                enrolledLayout.setVisibility(View.GONE);
            } else if (u_Role.equals("student")) {
                w = 0;
                gradeDown.setImageResource(R.drawable.down);
                gradeLayout.setVisibility(View.GONE);
            }
        } else {
            aboutDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.GONE);
            y = 0;
            if (u_Role.equals("professor")) {
                enrolledLayout.setVisibility(View.GONE);
            } else if (u_Role.equals("student")) {
                gradeLayout.setVisibility(View.GONE);
            }
        }
    }

    private void enrolledOnClick() {
        if (z == 0) {
            enrolledDown.setImageResource(R.drawable.right);
            aboutDown.setImageResource(R.drawable.down);
            postDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.GONE);
            enrolledLayout.setVisibility(View.VISIBLE);
            z = 1;
            x = 0;
            y = 0;
        } else {
            enrolledDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.GONE);
            enrolledLayout.setVisibility(View.GONE);
            z = 0;
        }
    }

    private void gradeOnClick() {
        if (w == 0) {
            gradeDown.setImageResource(R.drawable.right);
            aboutDown.setImageResource(R.drawable.down);
            postDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.GONE);
            gradeLayout.setVisibility(View.VISIBLE);
            w = 1;
            x = 0;
            y = 0;
        } else {
            gradeDown.setImageResource(R.drawable.down);
            postLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.GONE);
            gradeLayout.setVisibility(View.GONE);
            w = 0;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CourseActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("login", true);
        startActivity(intent);
        finish();
    }
}
