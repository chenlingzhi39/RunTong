package com.callba.phone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.util.DateUtils;
import com.umeng.socialize.utils.Log;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/5/26.
 */
public class ConversationViewHolder extends BaseViewHolder<EMConversation> {
    @InjectView(R.id.avatar)
    CircleImageView avatar;
    @InjectView(R.id.unread_msg_number)
    TextView unreadMsgNumber;
    @InjectView(R.id.avatar_container)
    RelativeLayout avatarContainer;
    @InjectView(R.id.name)
    TextView name;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.msg_state)
    ImageView msgState;
    @InjectView(R.id.message)
    TextView message;
    @InjectView(R.id.list_itease_layout)
    RelativeLayout listIteaseLayout;

    public ConversationViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_conversation);
        ButterKnife.inject(this, itemView);
    }

    @Override
    public void setData(EMConversation data) {
     name.setText(data.getUserName());
        if (data.getUnreadMsgCount() > 0) {
            // 显示与此用户的消息未读数
            unreadMsgNumber.setText(String.valueOf(data.getUnreadMsgCount()));
            unreadMsgNumber.setVisibility(View.VISIBLE);
        } else {
            unreadMsgNumber.setVisibility(View.INVISIBLE);
        }

        if (data.getAllMsgCount() != 0) {
            // 把最后一条消息的内容作为item的message内容
            EMMessage lastMessage = data.getLastMessage();
            EMTextMessageBody txtBody = (EMTextMessageBody) lastMessage.getBody();
            message.setText(txtBody.getMessage());
            Log.i("message",txtBody.getMessage());
            time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct() == EMMessage.Direct.SEND && lastMessage.status() == EMMessage.Status.FAIL) {
               msgState.setVisibility(View.VISIBLE);
            } else {
               msgState.setVisibility(View.GONE);
            }
        }
    }
}
