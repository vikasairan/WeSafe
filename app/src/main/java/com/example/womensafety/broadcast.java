package com.example.womensafety;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class broadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        context.startService(new Intent(context, BackgroundService.class));
    }
}