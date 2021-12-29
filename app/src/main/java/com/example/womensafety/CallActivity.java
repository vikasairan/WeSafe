package com.example.womensafety;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

public class CallActivity extends AppCompatActivity {
    ImageView accept;
    RelativeLayout attend;
    boolean attended = false;
    LinearLayout background;
    ImageView callerImg;
    RelativeLayout calling;
    Boolean check = Boolean.FALSE;
    Context context;
    ImageView imageView;
    TextView inCall;
    TextView incomming;
    boolean locked;
    AudioManager mAudioManager;
    String mPath;
    Thread main;
    int min = 0;
    MediaPlayer mp;
    TextView nameText;
    int originalVolume;
    TextView phoneText;
    Ringtone r;
    ImageView reject;
    int sec = 0;
    SharedPreferences sharedPref;
    Thread t;
    InterstitialAd mInterstitialAd;
    AdRequest adRequestint;

    @TargetApi(16)
    protected void onCreate(Bundle savedInstanceState) {
        locked = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
        context = this;
        Window window = getWindow();
        window.addFlags(AccessibilityEventCompat.TYPE_WINDOWS_CHANGED);
        window.addFlags(FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call2);
        context = this;
        calling = (RelativeLayout) findViewById(R.id.calling);
        attend = (RelativeLayout) findViewById(R.id.attend);
        incomming = (TextView) findViewById(R.id.timer);
        inCall = (TextView) findViewById(R.id.text_in_call);
        imageView = (ImageView) findViewById(R.id.speaker);
        sharedPref = getSharedPreferences("file", 0);
        nameText = (TextView) findViewById(R.id.caller_name);
        phoneText = (TextView) findViewById(R.id.caller_number);
        accept = (ImageView) findViewById(R.id.gif_answer);
        reject = (ImageView) findViewById(R.id.gif_reject);

        ImageView endCall = (ImageView) findViewById(R.id.end_call);
        main = Thread.currentThread();
        main.setName("Main Thread");
        setCaller();
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answer();
            }
        });
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decline();
            }
        });
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CallActivity.this, MainActivity.class));
                finish();
            }
        });

    }

    void ring() {
        try {
                r = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(1));
                r.play();
                return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void answer() {
        if (sharedPref.getString("ring", "").equals("")) {
            r.stop();
        } else {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setStreamVolume(3, originalVolume, 0);
            if (mp != null) {
                mp.stop();
                mp.release();
            }
        }
        calling.setVisibility(View.GONE);
        attend.setVisibility(View.VISIBLE);
        inCall.setVisibility(View.VISIBLE);
        incomming.setVisibility(View.VISIBLE);
        audioPlayer(sharedPref.getString("audio", ""));
        attended = true;
        t = new Thread() {
            public void run() {
                try {
                    Log.v("NewMyThread", t.getName());
                    while (true) {
                        Thread.sleep(1000);
                        if (main.isAlive()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    incomming.setText(updateTime());
                                }
                            });
                        } else {
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();
    }

    private String updateTime() {
        sec++;
        String time = "";
        if (sec < 60) {
            if (min < 10) {
                time = time + "0" + min;
            } else {
                time = time + min;
            }
            time = time + ":";
            if (sec < 10) {
                return time + "0" + sec;
            }
            return time + sec;
        }
        min++;
        sec = 0;
        if (min < 10) {
            time = time + "0" + min;
        } else {
            time = time + min;
        }
        time = time + ":";
        if (sec < 10) {
            return time + "0" + sec;
        }
        return time + sec;
    }

    public void decline() {
        if (sharedPref.getString("ring", "").equals("") && r != null) {
            r.stop();
        }
        startActivity(new Intent(CallActivity.this, MainActivity.class));
        finish();
    }

    public void audioPlayer(String path) {
        mPath = path;
        mp = new MediaPlayer();
        if(mp!=null){
            try {
                mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                originalVolume = mAudioManager.getStreamVolume(3);
                mAudioManager.setStreamVolume(3, mAudioManager.getStreamMaxVolume(3), 0);
                mp.setAudioStreamType(0);
                mp.setDataSource(path);
                mp.prepare();
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(16)
    void setCaller() {
        String name = sharedPref.getString("name", "");
        String phone = sharedPref.getString("number", "");
        nameText.setText(name);
        phoneText.setText(phone);
    }

    protected void onStop() {
        super.onStop();
        String path = sharedPref.getString("ring", "");
        if (r != null && path.equals("")) {
            r.stop();
        }
        if (mp != null) {
            try {
                if (mp.isPlaying()) {
                    mp.stop();
                    mp.release();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(3, originalVolume, 0);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onStart() {
        super.onStart();
        ring();
    }
}
