package com.callba.phone.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.callba.R;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMMessage;

/**
 * Created by PC-20160514 on 2016/5/27.
 */
public class ChatAdapter extends RecyclerArrayAdapter<EMMessage> {
    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_EXPRESSION = 12;
    private static final int MESSAGE_TYPE_RECV_EXPRESSION = 13;
    public ChatAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case MESSAGE_TYPE_SENT_TXT:
                return new ChatSentTxtViewHolder(parent);

            case MESSAGE_TYPE_RECV_TXT:
                return new ChatReceivedTxtViewHolder(parent);

            case MESSAGE_TYPE_SENT_IMAGE:
                return new ChatSentPictureViewHolder(parent);

            case MESSAGE_TYPE_RECV_IMAGE:
                return new ChatReceivedPictureViewHolder(parent);

        }

        return null;
    }

    @Override
    public final int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message == null) {
            return -1;
        }
        if (getData().get(position).getType() == EMMessage.Type.TXT)
            return message.direct() == EMMessage.Direct.SEND ? MESSAGE_TYPE_SENT_TXT : MESSAGE_TYPE_RECV_TXT;
        if (getData().get(position).getType() == EMMessage.Type.IMAGE)
            return message.direct() == EMMessage.Direct.SEND ? MESSAGE_TYPE_SENT_IMAGE : MESSAGE_TYPE_RECV_IMAGE;
        return -1;
    }



}
