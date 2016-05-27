package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.util.StringUtils;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/5/27.
 */
public class ChatReceivedViewHolder extends BaseViewHolder<EMMessage> {

    @InjectView(R.id.timestamp)
    TextView timestamp;
    @InjectView(R.id.iv_userhead)
    ImageView ivUserhead;
    @InjectView(R.id.tv_chatcontent)
    TextView tvChatcontent;
    @InjectView(R.id.bubble)
    RelativeLayout bubble;
    @InjectView(R.id.tv_userid)
    TextView tvUserid;

    public ChatReceivedViewHolder(ViewGroup parent) {
        super(parent, R.layout.ease_row_received_message);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(EMMessage data) {
        timestamp.setText(StringUtils.friendly_time(data.getMsgTime()));
        EMTextMessageBody txtBody = (EMTextMessageBody) data.getBody();
        tvChatcontent.setText(txtBody.getMessage());
    }
}
