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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;


public class RegisterActivity extends AppCompatActivity {

    private EditText r_id, r_password, r_cPassword;
    private TextView errorTextView;
    private String role;
    private String Id, Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle(getString(R.string.signUp));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        r_id = (EditText) findViewById(R.id.register_redId);
        r_password = (EditText) findViewById(R.id.register_password);
        r_cPassword = (EditText) findViewById(R.id.register_confirm);
        errorTextView = (TextView) findViewById(R.id.r_error_textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button clear = (Button) findViewById(R.id.register_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFields();
            }
        });
        Button create = (Button) findViewById(R.id.register);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorTextView.setText("");
                if (isNetworkConnected()) {
                    validateData();
                } else {
                    errorTextView.setText(getString(R.string.network_error));
                }
            }
        });
    }

    private void clearFields() {
        r_id.setText("");
        r_password.setText("");
        r_cPassword.setText("");
        errorTextView.setText("");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }

    private void validateData() {
        String id = r_id.getText().toString();
        String password = r_password.getText().toString();
        String cPassword = r_cPassword.getText().toString();

        if (!TextUtils.isEmpty(id)) {
            if (!TextUtils.isEmpty(password) && isPasswordValid(password)) {
                if (!TextUtils.isEmpty(cPassword)) {
                    if (password.equals(cPassword)) {
                        checkIfExistingUser(id, password);
                    } else {
                        r_cPassword.setError(getString(R.string.error_password_match));
                        r_cPassword.requestFocus();
                    }
                } else {
                    r_cPassword.setError(getString(R.string.error_field_required));
                    r_cPassword.requestFocus();
                }
            } else {
                if (TextUtils.isEmpty(password))
                    r_password.setError(getString(R.string.error_field_required));
                else
                    r_password.setError(getString(R.string.error_password_length));
                r_password.requestFocus();
            }
        } else {
            r_id.setError(getString(R.string.error_field_required));
            r_id.requestFocus();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void checkIfExistingUser(String id, String password) {

        Id = id;
        Password = password;
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Users");
        query1.whereEqualTo("username", Id);
        query1.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    if (objList.size() != 0) {
                        errorTextView.setText(getString(R.string.error_existing_user));
                        Toast.makeText(getBaseContext(), "Existing User", Toast.LENGTH_SHORT).show();
                    } else {
                        checkIfValidUser(Id, Password);
                    }
                } else {
                    errorTextView.setText(getString(R.string.server_error));
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void checkIfValidUser(String Id, String Password) {
        final String id, password;
        id = Id;
        password = Password;
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("UserData");
        query1.whereEqualTo("redId", Id);
        query1.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objList, ParseException e) {
                if (e == null) {
                    if (objList.size() != 0) {
                        for (int i = 0; i < objList.size(); i++) {
                            ParseObject obj = objList.get(i);
                            String objectId = obj.getObjectId();
                            role = obj.getString("role");
                            addUserAccount(id, password, role, objectId);
                        }
                    } else {
                        errorTextView.setText(getString(R.string.error_invalid_user));
                        Toast.makeText(getBaseContext(), "Invalid User", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    errorTextView.setText(getString(R.string.server_error));
                    Log.d("ERROR", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void addUserAccount(String Id, String Password, String Role, String ObjectId) {
        ParseObject user = new ParseObject("Users");
        user.put("username", Id);
        user.put("password", Password);
        user.put("role", Role);
        user.put("userData", ParseObject.createWithoutData("UserData", ObjectId));
        user.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    clearFields();
                    Toast.makeText(getBaseContext(), "Registered Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    errorTextView.setText("Registration Failed,\n Please Try again");
                    Toast.makeText(getBaseContext(), "Registration Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
