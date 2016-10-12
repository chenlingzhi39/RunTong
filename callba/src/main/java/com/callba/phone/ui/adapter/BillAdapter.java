package com.callba.phone.ui.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.callba.R;
import com.callba.phone.bean.Commodity;
import com.callba.phone.bean.Flow;
import com.callba.phone.util.Logger;
import com.umeng.socom.Log;

import java.util.List;

/**
 * Created by PC-20160514 on 2016/9/1.
 */
public class BillAdapter extends RadioAdapter<Commodity> {
    private int size;

    public BillAdapter(Context context, List<Commodity> items, int size) {
        super(context, items);
        this.size = size;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.item_bill, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RadioAdapter.ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        Logger.i("title",mItems.get(i).getTitle());
        Spannable spannable = new SpannableString(mItems.get(i).getPrice() + "元\n\n" + mItems.get(i).getTitle());
        spannable.setSpan(new AbsoluteSizeSpan(size), 0, (mItems.get(i).getPrice() + "元").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AbsoluteSizeSpan(size / 2), 4, spannable.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        viewHolder.mRadio.setText(spannable);
        if (mItems.get(i).getActivity().size()>0)
            viewHolder.corner.setVisibility(View.VISIBLE);
        else
            viewHolder.corner.setVisibility(View.GONE);

    }
}
