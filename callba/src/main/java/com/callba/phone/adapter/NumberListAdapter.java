package com.callba.phone.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.activity.SelectDialPopupWindow;

import java.util.List;

/**
 * Created by PC-20160514 on 2016/7/21.
 */
public class NumberListAdapter extends ArrayAdapter<String> {
    String name;
    public NumberListAdapter(Context context, int resource, List objects,String name) {
        super(context, resource, objects);
        this.name=name;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView= ((Activity)getContext()).getLayoutInflater().inflate(R.layout.contact_detail_lv_item,null);
        LinearLayout root=(LinearLayout) convertView.findViewById(R.id.root);
        TextView textView=(TextView)convertView.findViewById(R.id.tv_phonelist_number);
        Button send=(Button)convertView.findViewById(R.id.send_message);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SelectDialPopupWindow.class);
                intent.putExtra("name", name);
                intent.putExtra("number", getItem(position));
                getContext().startActivity(intent);
            }
        });
        textView.setText(getItem(position));
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri smsToUri = Uri.parse("smsto://" +getItem(position));
                Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                getContext().startActivity(mIntent);
            }
        });
        return convertView;
    }
}
