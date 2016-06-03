package com.callba.phone.adapter;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.EaseSmileUtils;
import com.callba.phone.util.EaseUserUtils;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
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
    TextView unreadLabel;
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
    protected int primaryColor;
    protected int secondaryColor;
    protected int timeColor;
    protected int primarySize;
    protected int secondarySize;
    protected float timeSize;
    public ConversationViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_conversation);
        ButterKnife.inject(this, itemView);
    }

    @Override
    public void setData(EMConversation conversation) {
        String username = conversation.getUserName();
            EaseUserUtils.setUserAvatar(getContext(), username, avatar);
            EaseUserUtils.setUserNick(username, name);


        if (conversation.getUnreadMsgCount() > 0) {
            // 显示与此用户的消息未读数
           unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
           unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }

        if (conversation.getAllMsgCount() != 0) {
            // 把最后一条消息的内容作为item的message内容
            EMMessage lastMessage = conversation.getLastMessage();
            message.setText(EaseSmileUtils.getSmiledText(getContext(), EaseCommonUtils.getMessageDigest(lastMessage, (this.getContext()))),
                    TextView.BufferType.SPANNABLE);

           time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct() == EMMessage.Direct.SEND && lastMessage.status() == EMMessage.Status.FAIL) {
                msgState.setVisibility(View.VISIBLE);
            } else {
                msgState.setVisibility(View.GONE);
            }
        }

        //设置自定义属性
      /*  name.setTextColor(primaryColor);
        message.setTextColor(secondaryColor);
        time.setTextColor(timeColor);
        if(primarySize != 0)
            name.setTextSize(TypedValue.COMPLEX_UNIT_PX, primarySize);
        if(secondarySize != 0)
           message.setTextSize(TypedValue.COMPLEX_UNIT_PX, secondarySize);
        if(timeSize != 0)
            time.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);*/

    }
}
