package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

/**
 * Created by PC-20160514 on 2016/5/27.
 */
public class ChatAdapter extends RecyclerArrayAdapter<EMMessage>{
    public final int RECEIVED=0;
    public final int SENT=1;
    public ChatAdapter(Context context) {
        super(context);
    }
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==RECEIVED)
        return new ChatReceivedViewHolder(parent);
        else return new ChatSentViewHolder(parent);
    }
    @Override
    public final int getItemViewType(int position) {
    if(getData().get(position).getFrom().equals(EMClient.getInstance().getCurrentUser()))
        return SENT;
        else return RECEIVED;
    }
}
