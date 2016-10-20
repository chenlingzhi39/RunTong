package com.callba.phone.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersAdapter;

/**
 * Created by PC-20160514 on 2016/10/20.
 */

public class WebContactAdapter extends RecyclerArrayAdapter<EaseUser> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    public WebContactAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new WebContactViewHolder(parent);
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).getInitialLetter().charAt(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String showValue = String.valueOf(getItem(position).getInitialLetter().charAt(0));
        textView.setText(showValue);
    }
    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = getData().get(i).getInitialLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;

    }
}
