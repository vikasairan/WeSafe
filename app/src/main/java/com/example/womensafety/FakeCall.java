package com.example.womensafety;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FakeCall extends AppCompatActivity {
    EditText nameEditText;
    EditText phoneEditText;
    EditText timeEditText;
    SharedPreferences sharedPref;
    protected void onResume() {
        super.onResume();
        setCaller();
    }

    void setCaller() {
        String name = sharedPref.getString("name", "");
        String phone = sharedPref.getString("number", "");
        String time = sharedPref.getString("time", "");
        nameEditText.setText(name);
        phoneEditText.setText(phone);
        timeEditText.setText(time);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fakecall);
        nameEditText = (EditText) findViewById(R.id.caller_name);
        phoneEditText = (EditText) findViewById(R.id.caller_number);
        timeEditText = (EditText) findViewById(R.id.time);
        sharedPref = getSharedPreferences("file", 0);
        Button schedule=findViewById(R.id.schedule);
        schedule.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            int time=0;
                                            if(timeEditText.getText().length()>0) {
                                                time = Integer.parseInt(timeEditText.getText().toString());
                                            }
                                            AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                            PendingIntent alarmIntent = PendingIntent.getBroadcast(FakeCall.this, 0, new Intent(FakeCall.this, AlarmReceiver.class), 0);
                                            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (long) (time * 1000), alarmIntent);
                                            Toast.makeText(FakeCall.this, "Call timer Set "+time+" sec", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    });
        nameEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!sharedPref.getString("name", "").equals(nameEditText.getText().toString())) {
                    saveName(nameEditText.getText().toString());
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        phoneEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!sharedPref.getString("number", "").equals(phoneEditText.getText().toString())) {
                    savePhone(phoneEditText.getText().toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        timeEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!sharedPref.getString("number", "").equals(timeEditText.getText().toString())) {
                    savetime(timeEditText.getText().toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        setCaller();
    }

    private void saveName(String name) {
        Editor editor = sharedPref.edit();
        editor.putString("name", name);
        editor.apply();
    }

    private void savePhone(String phone) {
        Editor editor = sharedPref.edit();
        editor.putString("number", phone);
        editor.apply();
    }
    private void savetime(String time) {
        Editor editor = sharedPref.edit();
        editor.putString("time", time);
        editor.apply();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                String name = data.getStringExtra("name");
                String number = data.getStringExtra("number");
                String time = data.getStringExtra("number");
                saveName(name);
                savePhone(number);
                savetime(time);
            }
        }
    }
}