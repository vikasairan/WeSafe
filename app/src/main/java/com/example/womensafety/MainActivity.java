package com.example.womensafety;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.womensafety.models.Contact;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Intent mServiceIntent;
    private BackgroundService mSensorService;
    Context ctx;
    public Context getCtx() {
        return ctx;
    }

    private final String TAG = this.getClass().getSimpleName();
    private static final double NEARBY_DISTANCE = 2; // Kilometres
    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;
    final double[] latitude = new double[1];
    final double[] longitude = new double[1];

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        final SQLiteDatabase mydatabase = openOrCreateDatabase("DATABASE", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS contact(name VARCHAR2,phone VARCHAR2 PRIMARY KEY);");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch tracking = findViewById(R.id.enabletracking);
        ImageView safe_places = findViewById(R.id.safe_places);
        ImageView panic = findViewById(R.id.panic);
        ImageView camera = findViewById(R.id.camera);
        ImageView contact = findViewById(R.id.contact);
        ImageView fakecall = findViewById(R.id.fakecall);
        ImageView tracklocation = findViewById(R.id.tracklocation);
        Button logout = findViewById(R.id.logout);
        final TextView tv1 = findViewById(R.id.tv1);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            DatabaseReference contactRef = database.getReference("Contact");
            contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String name = dataSnapshot.child(auth.getCurrentUser().getUid()).child("name").getValue().toString();
                    tv1.setText("Welcome, " + name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        final SharedPreferences sharedPreferences = getSharedPreferences("isChecked", 0);
        boolean value = false;
        value = sharedPreferences.getBoolean("isChecked", value); // retrieve the value of your key
        tracking.setChecked(value);

        safe_places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imap = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(imap);
            }
        });

        fakecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imap = new Intent(MainActivity.this, FakeCall.class);
                startActivity(imap);
            }
        });


        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            ctx = this;
            mSensorService = new BackgroundService();
            mServiceIntent = new Intent(getCtx(), mSensorService.getClass());
            if (!isMyServiceRunning(mSensorService.getClass())) {
                startService(mServiceIntent);
            }

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    latitude[0] = location.getLatitude();
                    longitude[0] = location.getLongitude();

//                Toast.makeText(MainActivity.this,latitude[0]+","+longitude[0],Toast.LENGTH_SHORT).show();

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
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 0, 1, listener);
        }
        panic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Cursor resultSet = mydatabase.rawQuery("Select * from contact", null);

                while (resultSet.moveToNext()) {
                    String name = resultSet.getString(0);
                    String phone = resultSet.getString(1);
                    Contact helperContact = new Contact();
                    helperContact.setName(name);
                    helperContact.setPhone(phone);
                    sendMySMS(helperContact,latitude[0], longitude[0]);
                }
                resultSet.close();
                contact_nearby(latitude[0], longitude[0]);
                Toast.makeText(getApplicationContext(), "Message Delivered!", Toast.LENGTH_SHORT).show();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File dir = Environment.getExternalStorageDirectory();
                File folder = new File(dir.getPath(), "Women Safety");
                if (!folder.exists()) {
                    File Directory = new File(folder.getPath());
                    Directory.mkdirs();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
                String currentTimeStamp = dateFormat.format(new Date());
                File output = new File(folder, "img" + currentTimeStamp + ".jpeg");
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
                startActivityForResult(i, 1);
                return;
            }
        });


        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, contact.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           mSensorService = new BackgroundService();
                                           mServiceIntent = new Intent(getCtx(), mSensorService.getClass());
                                           if (isMyServiceRunning(mSensorService.getClass())) {
                                               stopService(mServiceIntent);
                                           }
                                           FirebaseAuth auth = FirebaseAuth.getInstance();
                                           auth.signOut();
                                           Intent s = new Intent(MainActivity.this, MainLoginActivity.class);
                                           startActivity(s);
                                           finish();
                                       }
                                   });
        tracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton panicView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferences.edit().putBoolean("isChecked", true).apply();
                    Cursor cursor = mydatabase.rawQuery("Select * from contact", null);
                    while (cursor.moveToNext()) {
                        final String phoneNumber = cursor.getString(1);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference dbRef = database.getReference().child("Contact");
                        dbRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                    String phone = dsp.child("phone").getValue().toString();
                                    if (phone.equals(phoneNumber) || phone.equals("+91" + phoneNumber) || ("+91" + phone).equals(phoneNumber)) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference("Emergency");
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        myRef.child(auth.getCurrentUser().getUid()).child(dsp.getKey()).setValue("Yes");
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
                else
                {
                    sharedPreferences.edit().putBoolean("isChecked", false).apply();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference dbRef = database.getReference().child("Emergency");
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    dbRef.child(auth.getCurrentUser().getUid()).setValue(null);
                }
            }
        });


        tracklocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itrack = new Intent(MainActivity.this, TrackActivity.class);
                startActivity(itrack);
            }
        });



    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }


    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }



    private void contact_nearby(final double latitude_user, final double longitude_user)
    {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final DatabaseReference myRef = database.getReference().child("Contact");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String phoneNumber=dataSnapshot.child(auth.getCurrentUser().getUid()).child("phone").getValue().toString();
                final DatabaseReference dbRef = database.getReference().child("User");

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                         @Override
                                                         public void onDataChange(DataSnapshot dataSnapshot) {
                                                             for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                                                 String latitude = dsp.child("latitude").getValue().toString();
                                                                 String longitude = dsp.child("longitude").getValue().toString();
                                                                 String phone = dsp.getKey();
                                                                 String name = dsp.child("name").getValue().toString();
                                                                 double range = distance(latitude_user, longitude_user, Double.parseDouble(latitude), Double.parseDouble(longitude), "K");
                                                                 final SQLiteDatabase mydatabase = openOrCreateDatabase("DATABASE", MODE_PRIVATE, null);
                                                                 mydatabase.execSQL("CREATE TABLE IF NOT EXISTS contact(name VARCHAR2,phone VARCHAR2 PRIMARY KEY);");
                                                                 Cursor resultSet = mydatabase.rawQuery("Select * from contact", null);
                                                                  if (range <= NEARBY_DISTANCE&&!phone.equals(phoneNumber)) {
                                                                      int flag = 0;
                                                                      while (resultSet.moveToNext()) {
                                                                          String number = resultSet.getString(1);
                                                                          Toast.makeText(MainActivity.this,number+" "+phone,Toast.LENGTH_SHORT).show();
                                                                          if (number.equals(phone)) {
                                                                              flag = 1;
                                                                              break;
                                                                          }
                                                                      }
                                                                      resultSet.close();
                                                                      if (flag == 0) {
                                                                          Contact helperContact = new Contact();
                                                                          helperContact.setName(name);
                                                                          helperContact.setPhone(phone);
                                                                          sendMySMS(helperContact, latitude_user, longitude_user);
                                                                      }
                                                                  }
                                                             }
                                                         }

                                                         @Override
                                                         public void onCancelled(DatabaseError databaseError) {
                                                             return;
                                                         }
                                                     }
                );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                return;
            }
        });
    }

    public static String[] getPermissions(Context context)
            throws PackageManager.NameNotFoundException {
        PackageInfo info = context.getPackageManager().getPackageInfo(
                context.getPackageName(), PackageManager.GET_PERMISSIONS);

        return info.requestedPermissions;
    }

    public void sendMySMS(Contact helpingUser,double latitude,double longitude) {
        String phone = helpingUser.getPhone();
        String helperName = helpingUser.getName();
        String message= String.format(getString(R.string.emergency_contact_distress_message_text), helperName,String.valueOf(latitude),String.valueOf(longitude),String.valueOf(latitude),String.valueOf(longitude));
        SmsManager sms = SmsManager.getDefault();
//        List<String> messages = sms.divideMessage(message);
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
        sms.sendTextMessage(phone, null, message, sentIntent, deliveredIntent);

    }

    public void onResume() {
        super.onResume();
        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Unknown Error";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Sent Successfully !!";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Generic Failure Error";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : No Service Available";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Radio is off";
                        break;
                    default:
                        break;
                }
            }
        };
        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Message Not Delivered";
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Delivered Successfully";
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
            }
        };
        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }


    public void onPause() {
        super.onPause();
        unregisterReceiver(sentStatusReceiver);
        unregisterReceiver(deliveredStatusReceiver);
    }
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit == "K") {
                dist = dist * 1.609344;
            } else if (unit == "N") {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }


}

