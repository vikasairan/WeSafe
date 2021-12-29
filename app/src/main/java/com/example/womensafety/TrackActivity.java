package com.example.womensafety;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class TrackActivity extends AppCompatActivity {
    ArrayList items;
    ListView list;
    private TrackAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        list = (ListView) findViewById(R.id.list);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.VISIBLE);

        items = new ArrayList();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dbRef = database.getReference().child("Emergency");
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                for (final DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if(dsp.child(auth.getCurrentUser().getUid()).exists())
                    {
                        final String key=dsp.getKey();
                        final DatabaseReference mydb = database.getReference().child("Contact");
                        mydb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child(key).child("name").getValue().toString();
                            String phone = dataSnapshot.child(key).child("phone").getValue().toString();
                            items.add(new TrackModel(name,phone));
                            }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    }
                }
                adapter = new TrackAdapter(items, getApplicationContext());
                list.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                TrackModel Model = (TrackModel) items.get(position);
                String name=Model.name;
                String phone=Model.phone;
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("phone", phone);
                Intent i=new Intent(TrackActivity.this,RealTimeMap.class);
                i.putExtras(bundle);
                startActivity(i);
            }

        });
    }
}
