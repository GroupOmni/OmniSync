package com.groupomni.omnisync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceListArrayAdapter extends ArrayAdapter<DeviceListItem> {

    public DeviceListArrayAdapter(Context context, List<DeviceListItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceListItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_layout, parent, false);
        }

        TextView textView1 = convertView.findViewById(R.id.text_view1);
        TextView textView2 = convertView.findViewById(R.id.text_view2);

        textView1.setText(item.getHostIP());
        textView2.setText(String.valueOf(item.getPort()));

        return convertView;
    }
}
