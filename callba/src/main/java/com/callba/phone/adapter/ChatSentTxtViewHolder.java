package com.callba.phone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.util.StringUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.util.DateUtils;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/5/27.
 */
public class ChatSentTxtViewHolder extends BaseChatViewHolder {
    @InjectView(R.id.timestamp)
    TextView timestamp;
    @InjectView(R.id.iv_userhead)
    ImageView ivUserhead;
    @InjectView(R.id.tv_chatcontent)
    TextView tvChatcontent;
    @InjectView(R.id.bubble)
    RelativeLayout bubble;
    @InjectView(R.id.msg_status)
    ImageView msgStatus;
    @InjectView(R.id.tv_ack)
    TextView tvAck;
    @InjectView(R.id.tv_delivered)
    TextView tvDelivered;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    public ChatSentTxtViewHolder(ViewGroup parent) {
        super(parent, R.layout.ease_row_sent_message);
        ButterKnife.inject(this,itemView);
    }

    @Override
    public void setData(final EMMessage data) {
        timestamp.setText(DateUtils.getTimestampString(new Date(data.getMsgTime())));
        EMTextMessageBody txtBody = (EMTextMessageBody) data.getBody();
        tvChatcontent.setText(txtBody.getMessage());
        updateView(data,progressBar,null,msgStatus);
        data.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                updateView(data,progressBar,null,msgStatus);
            }

            @Override
            public void onError(int i, String s) {
                updateView(data,progressBar,null,msgStatus);
            }

            @Override
            public void onProgress(int i, String s) {
                updateView(data,progressBar,null,msgStatus);
            }
        });
    }

}
