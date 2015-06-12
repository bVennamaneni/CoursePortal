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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String PREF_FILE_NAME = "Current_User", PWD_FILE_NAME = "Password_lock";
    private TextView postView;
    private String u_Name, u_Role, u_Id, id, u_Email;
    private ArrayList<Course> courseDetailList;
    private ArrayList<Notification> notificationList;
    private ArrayList<String> enrolledList, courseList;
    private ArrayMap<String, String> enrollmentType;
    private ListView listView;
    private Button showMore;
    private Context context;
    private Menu optionsMenu;
    private LinearLayout professor_layout;
    private Boolean postLimit = false, isReloading = false;
    private AlertDialog user_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        Parse.initialize(this, "7mRqTRbmcuuZAwEr15TgFI8dimWrIQ9b6nHox6Wn", "bVNaJZ71Hjc3eHs0rWW7NloKKFGnKoBY9qYKSKel");
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));

        if (getIntent().getBooleanExtra("Exit me", false)) {
            finish();
            return;
        }
        SharedPreferences user = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);

        int check = user.getInt("Check", 0);
        if (check == 0) {
            Intent login_intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login_intent);
            finish();
        }
        if (!getIntent().getBooleanExtra("login", false)) {
            SharedPreferences securityCode = getSharedPreferences(PWD_FILE_NAME, MODE_PRIVATE);
            String passCode = securityCode.getString("PassCode", "");
            if (!passCode.equals("")) {
                Intent pwd_intent = new Intent(MainActivity.this, PasswordActivity.class);
                pwd_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(pwd_intent);
                finish();
            }
        }

        u_Name = user.getString("UserName", "");
        u_Id = user.getString("UserId", "");
        u_Role = user.getString("UserRole", "");
        u_Email = user.getString("UserEmail", "");
        postView = (TextView) findViewById(R.id.c_announcement);
        listView = (ListView) findViewById(R.id.course_list);
        id = user.getString("UserId", "none");
        courseDetailList = new ArrayList<>();
        enrolledList = new ArrayList<>();
        notificationList = new ArrayList<>();
        professor_layout = (LinearLayout) findViewById(R.id.c_professorLayout);


        if (isNetworkConnected()) {
            getCoursesEnrolled();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String[] line = listView.getItemAtPosition(i).toString().split(",");
                    String[] selected = line[0].split("=");
                    for (Course c : courseDetailList) {
                        if (c.course_no.equals(selected[1])) {
                            Intent intent = new Intent(MainActivity.this, CourseActivity.class);
                            intent.putExtra("CourseID", c.course_id);
                            intent.putExtra("CourseNo", c.course_no);
                            intent.putExtra("CourseTitle", c.course_title);
                            intent.putExtra("CourseEnrollType", enrollmentType.get(c.course_id));
                            intent.putExtra("CourseInstructor", c.course_instructor);
                            intent.putExtra("CourseGrading", c.course_grading.toString());
                            startActivity(intent);
                        }
                    }
                }
            });

            showMore = (Button) findViewById(R.id.c_load);
            showMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postLimit = false;
                    loadMorePosts();
                    showMore.setVisibility(View.GONE);
                }
            });
            if (u_Role.equals("professor")) {

                Button addPost = (Button) findViewById(R.id.c_addPost);
                addPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openPostActivity();
                    }
                });

                Button addGrade = (Button) findViewById(R.id.c_addGrade);
                addGrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGradeActivity();

                    }
                });

            }
        }
    }

    private void getCoursesEnrolled() {
        enrollmentType = new ArrayMap<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Enrollment");
        query.whereEqualTo("RedId", id);
        query.selectKeys(Arrays.asList("CourseId", "Type"));
        query.orderByAscending("CourseId");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    if (objList.size() != 0) {
                        for (int i = 0; i < objList.size(); i++) {
                            ParseObject obj = objList.get(i);
                            enrolledList.add(obj.getString("CourseId"));
                            enrollmentType.put(obj.getString("CourseId"), obj.getString("Type"));
                        }
                        if (u_Role.equals("professor"))
                            professor_layout.setVisibility(View.VISIBLE);
                        getCourseDetails();
                    } else {
                        professor_layout.setVisibility(View.GONE);
                        TextView textView = (TextView) findViewById(R.id.m_notEnrolled);
                        textView.setText(getString(R.string.not_enrolled));
                        textView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void getCourseDetails() {
        String[] courses = new String[enrolledList.size()];
        courses = enrolledList.toArray(courses);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Course");
        query.selectKeys(Arrays.asList("c_id", "c_no", "c_title", "instructor", "grading"));
        query.include("instructor");
        query.whereContainedIn("c_id", Arrays.asList(courses));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objList.size(); i++) {
                        ParseObject obj = objList.get(i);
                        Course c = new Course();
                        c.course_id = obj.getString("c_id");
                        c.course_no = obj.getString("c_no");
                        c.course_title = obj.getString("c_title");
                        c.course_instructor = obj.getParseObject("instructor").getString("firstName") +
                                " " + obj.getParseObject("instructor").getString("lastName");
                        c.course_grading = obj.getJSONObject("grading");
                        courseDetailList.add(c);
                        if (i == objList.size() - 1) {
                            displayCourses();
                            displayRecentPosts();
                        }
                    }
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });

    }

    private void displayCourses() {
        ArrayList<HashMap<String, String>> list = new ArrayList();
        HashMap<String, String> item;
        courseList = new ArrayList();
        for (Course c : courseDetailList) {
            item = new HashMap();
            item.put("line1", c.course_no);
            item.put("line2", c.course_title);
            list.add(item);
            courseList.add(c.course_id + ":" + c.course_title);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, list,
                android.R.layout.two_line_list_item,
                new String[]{"line1", "line2"},
                new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(simpleAdapter);
        listView.setDividerHeight(1);
        setListViewHeight(listView);
        setMenuOption();
    }

    private void displayRecentPosts() {
        String[] courses = new String[enrolledList.size()];
        courses = enrolledList.toArray(courses);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Notification");
        query.whereContainedIn("c_id", Arrays.asList(courses));
        query.whereEqualTo("userGroup", "All");
        query.orderByDescending("createdAt");
        query.setLimit(5);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objList.size(); i++) {
                        ParseObject obj = objList.get(i);
                        Notification note = new Notification();
                        note.n_courseId = obj.getString("c_id");
                        note.n_content = obj.getString("content");
                        note.n_subject = obj.getString("subject");
                        note.n_createdAt = obj.getCreatedAt().toString();
                        notificationList.add(note);
                    }
                    if (objList.size() > 2) {
                        showMore.setVisibility(View.VISIBLE);
                    }
                    postLimit = true;
                    loadMorePosts();
                } else {
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void loadMorePosts() {
        int postCount = 0;
        postView.setText("");
        String mainTitle = "<b>Category: All</b>";
        postView.append(Html.fromHtml(mainTitle));
        postView.append("\n\n");
        for (Notification n : notificationList) {
            String title = "<b>Posted on:</b>";
            String s_title = "<b>Posted to:</b>";
            String subject = "<b>" + n.n_subject + "</b>";
            postView.append(Html.fromHtml(subject));
            postView.append("\n");
            for (Course c : courseDetailList) {
                if (c.course_id.equals(n.n_courseId)) {
                    postView.append(Html.fromHtml(s_title));
                    postView.append("\t" + c.course_no + "\n");
                    postView.append(Html.fromHtml(title));
                    postView.append("\t" + n.n_createdAt + "\n");
                }
            }
            postView.append(n.n_content);
            if (postLimit && postCount == 1)
                break;
            else if (postCount != notificationList.size() - 1) {
                postView.append("\n\n");
            }
            postCount++;
        }
        isReloading = false;
    }

    class Course {
        String course_id, course_no, course_title, course_instructor;
        JSONObject course_grading;
    }

    class Notification {
        String n_subject, n_content, n_courseId, n_createdAt;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.userAccount:
                displayUser();
                return true;
            case R.id.writePost:
                openPostActivity();
                return true;
            case R.id.action_settings:
                Intent s_intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(s_intent);
                return true;
            case R.id.action_refresh:
                reloadData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setMenuOption() {
        if (u_Role.equals("professor")) {
            MenuItem item = optionsMenu.findItem(R.id.writePost);
            item.setVisible(true);
        }
    }

    private void openPostActivity() {
        Intent intent = new Intent(MainActivity.this, PostActivity.class);
        if (isNetworkConnected()) {
            intent.putStringArrayListExtra("CourseList", courseList);
        }
        startActivity(intent);
    }

    private void openGradeActivity() {
        Intent gradeIntent = new Intent(MainActivity.this, GradeActivity.class);
        gradeIntent.putStringArrayListExtra("CourseList", courseList);
        startActivity(gradeIntent);
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

    private void reloadData() {
        if (!isReloading) {
            courseDetailList = new ArrayList<>();
            enrolledList = new ArrayList<>();
            notificationList = new ArrayList<>();
            getCoursesEnrolled();
            isReloading = true;
        }
    }

    private void displayUser() {
        final TextView display = new TextView(context);
        String userData = "<b>Red Id: </b>" + u_Id + "<br/>" + "<b>Category: </b>" + u_Role + "<br/><b>Email: </b>" + u_Email;
        display.setTextAppearance(this, R.style.Base_TextAppearance_AppCompat_Medium);
        display.setTextColor(Color.BLACK);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (10 * scale + 0.5f);
        display.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
        display.setText(Html.fromHtml(userData));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(u_Name)
                .setView(display)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                });
        user_dialog = builder.create();
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


}
