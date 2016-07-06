package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.chat.EMConversation;

/**
 * Created by PC-20160514 on 2016/7/6.
 */
public class ContactNumberAdapter extends RecyclerArrayAdapter<String>{
    public ContactNumberAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactNumberViewHolder(parent);
    }

}
