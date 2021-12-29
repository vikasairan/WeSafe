package com.example.womensafety;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {

    private ArrayList dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName,txtPhone;
        CheckBox checkBox;
    }

    public CustomAdapter(ArrayList data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext = context;

    }
    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public DataModel getItem(int position) {
        return (DataModel) dataSet.get(position);
    }


    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.txtPhone = (TextView) convertView.findViewById(R.id.txtPhone);
            viewHolder.txtPhone = (TextView) convertView.findViewById(R.id.txtPhone);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            result=convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        DataModel item = getItem(position);
        viewHolder.txtName.setText(item.name);
        viewHolder.txtPhone.setText(item.phone);
        viewHolder.checkBox.setChecked(item.checked);


        return result;
    }

}
