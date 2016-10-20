package com.callba.phone.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.SeparatedEMGroup;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersAdapter;

/**
 * Created by PC-20160514 on 2016/10/20.
 */

public class GroupAdapter extends RecyclerArrayAdapter<SeparatedEMGroup> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    public GroupAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(parent);
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).getType().charAt(0);
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
        String showValue = String.valueOf(getItem(position).getType().charAt(0));
        if(showValue.equals("0"))
        textView.setText("我创建的群");
        else textView.setText("我加入的群");
    }
}
