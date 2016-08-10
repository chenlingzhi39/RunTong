package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.phone.bean.TxRecord;

/**
 * Created by PC-20160514 on 2016/7/18.
 */
public class TXRecordAdapter extends RecyclerArrayAdapter<TxRecord>{
    public TXRecordAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new TXRecordViewHolder(parent);
    }
}
