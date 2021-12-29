package com.example.womensafety;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;


public class contact extends AppCompatActivity {
    Button add;
    ArrayList items;
    ListView list;
    private CustomAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        final SQLiteDatabase mydatabase = openOrCreateDatabase("DATABASE", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS contact(name VARCHAR2,phone VARCHAR2);");
        list = (ListView) findViewById(R.id.list);
        add = (Button) findViewById(R.id.add);
        items = new ArrayList();
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
        };

        Cursor phone = null;
        try {
            phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        } catch (SecurityException e) {
        }

        if (phone != null) {
            try {
                HashSet<String> normalizedNumbersAlreadyFound = new HashSet<>();
                int indexOfNormalizedNumber = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                int indexOfDisplayName = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int indexOfDisplayNumber = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (phone.moveToNext()) {
                    String normalizedNumber = phone.getString(indexOfNormalizedNumber);
                    if (normalizedNumbersAlreadyFound.add(normalizedNumber)) {
                        String name = phone.getString(indexOfDisplayName);
                        String phoneNumber = phone.getString(indexOfDisplayNumber);
                        phoneNumber = phoneNumber.replace(" ", "");
                        if(phoneNumber.contains("+91"))
                        {
                            phoneNumber=phoneNumber.replace("+91","");
                        }
                        if(phoneNumber.charAt(0)=='0')
                        {
                            phoneNumber=phoneNumber.substring(1);
                        }
                        Cursor cursor = mydatabase.rawQuery("Select * from contact where phone='" + phoneNumber + "'", null);
                        if (cursor.getCount() > 0) {
                            items.add(new DataModel(name, phoneNumber, true));
                        } else {
                            items.add(new DataModel(name, phoneNumber, false));
                        }
                    }
                }
            } finally {
                phone.close();
            }
            adapter = new CustomAdapter(items, getApplicationContext());
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {
                    DataModel dataModel = (DataModel) items.get(position);
                    dataModel.checked = !dataModel.checked;
                    adapter.notifyDataSetChanged();
                }

            });
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mydatabase.execSQL("DELETE FROM contact");
                    int len = list.getCount();
                    for (int i = 0; i < len; i++) {
                        DataModel dataModel = (DataModel) items.get(i);
                        String name = dataModel.name;
                        String phone = dataModel.phone;
                        if (dataModel.checked == true)
                        {
                            mydatabase.execSQL("INSERT INTO Contact VALUES('" + name + "','" + phone + "');");
                        }
                    }

                    startActivity(new Intent(contact.this, MainActivity.class));
                    finish();
                }
            });
        }
    }
}
