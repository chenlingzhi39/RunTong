package com.callba.phone.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Member;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersAdapter;
import com.callba.phone.util.Logger;

/**
 * Created by PC-20160514 on 2016/10/25.
 */

public class GroupMemberAdapter extends RecyclerArrayAdapter<Member> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    public GroupMemberAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupMemberViewHolder(parent);
    }
    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String showValue =  String.valueOf(getItem(position).getSortLetters().charAt(0));
        Logger.i("showValue",showValue);
        if ("$".equals(showValue))
            textView.setText("群主");
        else
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
        return getItem(position).getSortLetters().charAt(0);
    }



    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = getItem(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;

    }
}
