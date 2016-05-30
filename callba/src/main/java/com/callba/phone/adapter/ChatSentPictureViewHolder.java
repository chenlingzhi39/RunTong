package com.callba.phone.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.DateUtils;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/5/30.
 */
public class ChatSentPictureViewHolder extends BaseChatViewHolder {
    @InjectView(R.id.timestamp)
    TextView timestamp;
    @InjectView(R.id.iv_userhead)
    ImageView ivUserhead;
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.bubble)
    RelativeLayout bubble;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    @InjectView(R.id.percentage)
    TextView percentage;
    @InjectView(R.id.ll_loading)
    LinearLayout llLoading;
    @InjectView(R.id.msg_status)
    ImageView msgStatus;
    @InjectView(R.id.tv_ack)
    TextView tvAck;
    @InjectView(R.id.tv_delivered)
    TextView tvDelivered;
    private EMCallBack messageSendCallback;
    public ChatSentPictureViewHolder(ViewGroup parent) {
        super(parent, R.layout.ease_row_sent_picture);
        ButterKnife.inject(this, itemView);
    }

    @Override
    public void setData(final EMMessage data) {
        timestamp.setText(DateUtils.getTimestampString(new Date(data.getMsgTime())));

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
