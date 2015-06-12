package com.bhavanavennamaneni.courseportal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class LoginActivity extends AppCompatActivity {

    private EditText mId, mPassword;
    private TextView errorText;
    private View mProgressView;
    private View mLoginFormView;
    private static final String PREF_FILE_NAME = "Current_User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));
        mId = (EditText) findViewById(R.id.login_Id);
        mPassword = (EditText) findViewById(R.id.login_password);
        errorText = (TextView) findViewById(R.id.error_textView);

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                errorText.setVisibility(View.INVISIBLE);
                errorText.setText("");
                attemptLogin();
            }
        });

        Button signUp = (Button) findViewById(R.id.login_signUp);
        signUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    errorText.setVisibility(View.INVISIBLE);
                    errorText.setText("");
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (isNetworkConnected()) {
            mId.setError(null);
            mPassword.setError(null);
            String id = mId.getText().toString();
            String password = mPassword.getText().toString();
            if (!TextUtils.isEmpty(id)) {
                if (!TextUtils.isEmpty(password)) {
                    showProgress(true);
                    UserLoginTask(id, password);
                } else {
                    mPassword.setError(getString(R.string.error_field_required));
                    mPassword.requestFocus();
                }
            } else {
                mId.setError(getString(R.string.error_field_required));
                mId.requestFocus();
            }
        } else {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText(getString(R.string.network_error));
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }

    /* Shows the progress UI and hides the login form.*/
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void UserLoginTask(String Id, String Password) {
        final String id;
        final String password;
        id = Id;
        password = Password;
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Users");
        query1.whereEqualTo("username", id);
        query1.include("userData");
        query1.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    if (objList.size() != 0) {
                        for (int i = 0; i < objList.size(); i++) {
                            ParseObject obj = objList.get(i);
                            String pwd = obj.getString("password");
                            if (password.equals(pwd)) {
                                String name = obj.getParseObject("userData").getString("firstName") + " " +
                                        obj.getParseObject("userData").getString("lastName");
                                String role = obj.getParseObject("userData").getString("role");
                                String email = obj.getParseObject("userData").getString("emailId");
                                loginSuccess(id, name, role, email);
                            } else {
                                handleLoginError();
                            }
                        }
                    } else
                        handleLoginError();
                } else {
                    showProgress(false);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText(getString(R.string.network_error));
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void handleLoginError() {
        showProgress(false);
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(getString(R.string.invalid_credentials));
        mId.requestFocus();
    }

    private void loginSuccess(String userId, String userName, String userRole, String userEmail) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE).edit();
        editor.putInt("Check", 1);
        editor.putString("UserName", userName);
        editor.putString("UserId", userId);
        editor.putString("UserRole", userRole);
        editor.putString("UserEmail", userEmail);
        editor.commit();
        showProgress(false);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}



