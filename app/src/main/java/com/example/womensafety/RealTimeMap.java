package com.example.womensafety;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RealTimeMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;//final double coordinate[]=new double[2];
    private final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final TextView lat=findViewById(R.id.lat);
        final TextView lng=findViewById(R.id.lng);

        Intent i=getIntent();
        Bundle extras = getIntent().getExtras();
        final String phone =extras.getString("phone");
//        Toast.makeText(getApplicationContext(), phone, Toast.LENGTH_SHORT).show();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mydb = database.getReference().child("User");
        mydb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String latitute=dataSnapshot.child(phone).child("latitude").getValue().toString();
                String longitude=dataSnapshot.child(phone).child("longitude").getValue().toString();
                lat.setText(latitute);
                lng.setText(longitude);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle extras = getIntent().getExtras();
        final String name =extras.getString("name");
        handler.postDelayed(new Runnable() {
                @Override
                public void run() {
        TextView lat=findViewById(R.id.lat);
        TextView lng=findViewById(R.id.lng);
        LatLng latlng = new LatLng( Double.parseDouble(lat.getText().toString()),Double.parseDouble(lng.getText().toString()));
//         Add a marker in Sydney and move the camera
        //LatLng latlng = new LatLng(-34, 151);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latlng).title(name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,16));
                 onMapReady(mMap);
                }
            }, 1000);
        }
    }
