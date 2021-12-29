package com.example.womensafety;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BackgroundService extends Service {


    LocationManager m_locationManager;
    final double[] latitude = new double[1];
    final double[] longitude = new double[1];

    @Override
    public void onCreate() {

        this.m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            latitude[0] = location.getLatitude();
            longitude[0] = location.getLongitude();

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference("User");
            DatabaseReference contactRef = database.getReference("Contact");
            final double finalLatitude = latitude[0];
            final double finalLongitude = longitude[0];

            contactRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
                        String phone = dataSnapshot.child(auth.getCurrentUser().getUid()).child("phone").getValue().toString();
                        String name = dataSnapshot.child(auth.getCurrentUser().getUid()).child("name").getValue().toString();
                        DatabaseReference User = myRef.child(phone);
                        User.child("latitude").setValue(finalLatitude);
                        User.child("longitude").setValue(finalLongitude);
                        User.child("name").setValue(name);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            Intent broadcastIntent = new Intent(BackgroundService.this, broadcast.class);
            sendBroadcast(broadcastIntent);
        }
            return START_STICKY;
        }



        @Override
        public void onDestroy() {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            } else {
                super.onDestroy();
                Intent broadcastIntent = new Intent(BackgroundService.this, broadcast.class);
                sendBroadcast(broadcastIntent);
            }
        }
    }