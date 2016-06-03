package com.callba.phone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.util.EaseImageUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.util.DateUtils;

import java.io.File;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/5/30.
 */
public class ChatReceivedPictureViewHolder extends BaseChatViewHolder {
    @InjectView(R.id.timestamp)
    TextView timestamp;
    @InjectView(R.id.iv_userhead)
    ImageView ivUserhead;
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    @InjectView(R.id.percentage)
    TextView percentage;
    @InjectView(R.id.ll_loading)
    LinearLayout llLoading;
    @InjectView(R.id.bubble)
    RelativeLayout bubble;
    @InjectView(R.id.tv_userid)
    TextView tvUserid;
    private EMCallBack messageSendCallback;
    public ChatReceivedPictureViewHolder(ViewGroup parent) {
        super(parent, R.layout.ease_row_received_picture);
        ButterKnife.inject(this, itemView);
    }

    @Override
    public void setData(final EMMessage data) {
        timestamp.setText(DateUtils.getTimestampString(new Date(data.getMsgTime())));
        EMImageMessageBody imgBody = (EMImageMessageBody)data.getBody();
        Glide.with(getContext()).load(imgBody.getRemoteUrl()).into(image);
    }
}
