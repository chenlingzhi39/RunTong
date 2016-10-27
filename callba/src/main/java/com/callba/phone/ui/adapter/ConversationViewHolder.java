package com.callba.phone.ui.adapter;

import android.graphics.Color;
import android.text.TextUtils;
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
import com.hyphenate.util.DateUtils;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/5/26.
 */
public class ConversationViewHolder extends BaseViewHolder<EMConversation> {
    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.unread_msg_number)
    TextView unreadLabel;
    @BindView(R.id.avatar_container)
    RelativeLayout avatarContainer;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.msg_state)
    ImageView msgState;
    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.list_itease_layout)
    RelativeLayout listIteaseLayout;
    public ConversationViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_conversation);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(EMConversation conversation) {
        String username = conversation.getUserName();
        if (conversation.getType() == EMConversation.EMConversationType.GroupChat) {
            // 群聊消息，显示群聊头像
            avatar.setImageResource(R.drawable.ease_group_icon);
            EMGroup group = EMClient.getInstance().groupManager().getGroup(username);
            name.setText(group != null ? group.getGroupName() : username);
            name.setTextColor(Color.parseColor("#000000"));
        } else if(conversation.getType() == EMConversation.EMConversationType.ChatRoom){
           avatar.setImageResource(R.drawable.ease_group_icon);
            EMChatRoom room = EMClient.getInstance().chatroomManager().getChatRoom(username);
            name.setText(room != null && !TextUtils.isEmpty(room.getName()) ? room.getName() : username);
            name.setTextColor(Color.parseColor("#000000"));
        }else {
            EaseUserUtils.setUserAvatar(getContext(), username,avatar);
            EaseUserUtils.setUserNick(username,name);
        }
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
            Locale.setDefault(new Locale("zh"));
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
