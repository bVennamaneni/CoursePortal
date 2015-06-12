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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {

    private static final String PREF_FILE_NAME = "Current_User", PWD_FILE_NAME = "Password_lock";
    private AlertDialog password_dialog;
    private Switch lockSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle(getString(R.string.title_activity_settings));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5CB8E6")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lockSwitch = (Switch) findViewById(R.id.security_switch);
        setLockSwitch();

        lockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    promptPasswordSetup();
                } else {
                    getSharedPreferences(PWD_FILE_NAME, 0).edit().clear().commit();
                }
            }
        });
        TextView remove = (TextView) findViewById(R.id.remove_textView);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAccount();
            }
        });
    }

    private void promptPasswordSetup() {
        View view = getLayoutInflater().inflate(R.layout.dialog_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_dialog))
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePassword();
                    }
                });
        password_dialog = builder.create();
        password_dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        password_dialog.setCanceledOnTouchOutside(false);
        password_dialog.show();
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
                }
            }
        });

        password_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setLockSwitch();
            }
        });

        password_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setLockSwitch();
            }
        });
    }

    private void setLockSwitch() {
        SharedPreferences securityCode = getSharedPreferences(PWD_FILE_NAME, MODE_PRIVATE);
        String passCode = securityCode.getString("PassCode", "");
        if (!passCode.equals("")) {
            lockSwitch.setChecked(true);
        } else {
            lockSwitch.setChecked(false);
        }
    }

    private void savePassword() {
        EditText editText = (EditText) password_dialog.findViewById(R.id.dialog_password);
        String code = editText.getText().toString();
        SharedPreferences.Editor editor = getSharedPreferences(PWD_FILE_NAME, MODE_PRIVATE).edit();
        editor.putString("PassCode", code);
        editor.commit();
        password_dialog.dismiss();
    }

    private void removeAccount() {
        getSharedPreferences(PREF_FILE_NAME, 0).edit().clear().commit();
        getSharedPreferences(PWD_FILE_NAME, 0).edit().clear().commit();
        backToMainActivity();
    }

    private void backToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("login", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToMainActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        backToMainActivity();
        return super.onOptionsItemSelected(item);

    }
}
