package com.callba.phone.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/10/14.
 */

public class ContactAdapter extends RecyclerArrayAdapter<ContactMultiNumBean>implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {


    public ContactAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(parent);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String showValue = String.valueOf(getItem(position).getTypeName().charAt(0));
        textView.setText(showValue);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    public long getHeaderId(int position) {
        return getItem(position).getTypeName().charAt(0);
    }



    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = getData().get(i).getTypeName();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;

    }

}
