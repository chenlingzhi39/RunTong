package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.chat.EMConversation;

/**
 * Created by PC-20160514 on 2016/5/26.
 */
public class ConversationAdapter extends RecyclerArrayAdapter<EMConversation>{
    public ConversationAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationViewHolder(parent);
    }
}
