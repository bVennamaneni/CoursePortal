package com.bhavanavennamaneni.courseportal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


public class PasswordActivity extends AppCompatActivity {

    private static final String PWD_FILE_NAME = "Password_lock";
    private AlertDialog password_dialog;
    private Boolean isValid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = new View(this);
        view.setBackgroundColor(Color.BLACK);
        setContentView(view);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));
        promptPassword();
    }


    private void promptPassword() {
        View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_p_dialog))
                .setView(view)
                .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validatePassword();

                    }
                });
        password_dialog = builder.create();
        password_dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        password_dialog.show();
        password_dialog.setCanceledOnTouchOutside(false);
        password_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


        EditText p_editText = (EditText) password_dialog.findViewById(R.id.dialog_password);
        p_editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    password_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else
                    password_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        p_editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    validatePassword();
                    return true;
                }
                return false;
            }
        });

        password_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (isValid) {
                    finish();
                } else {
                    password_dialog.show();
                }
                isValid = true;
            }
        });

        password_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (isValid) {
                    finish();
                } else {
                    password_dialog.show();
                }
                isValid = true;
            }
        });
    }

    private void validatePassword() {
        EditText editText = (EditText) password_dialog.findViewById(R.id.dialog_password);
        String code = editText.getText().toString();
        SharedPreferences securityCode = getSharedPreferences(PWD_FILE_NAME, MODE_PRIVATE);
        String passCode = securityCode.getString("PassCode", "");
        if (passCode.equals(code)) {
            isValid = true;
            Intent back = new Intent(PasswordActivity.this, MainActivity.class);
            back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            back.putExtra("login", true);
            startActivity(back);
            finish();
        } else {
            isValid = false;
            editText.setText("");
        }
    }


}
