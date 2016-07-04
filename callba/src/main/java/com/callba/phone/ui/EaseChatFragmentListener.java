package com.callba.phone.ui;

import android.view.View;

import com.callba.phone.widget.chatrow.EaseCustomChatRowProvider;
import com.hyphenate.chat.EMMessage;

/**
 * Created by PC-20160514 on 2016/7/4.
 */
public interface EaseChatFragmentListener {
    /**
     * 设置消息扩展属性
     */
    void onSetMessageAttributes(EMMessage message);

    /**
     * 进入会话详情
     */
    void onEnterToChatDetails();

    /**
     * 用户头像点击事件
     *
     * @param username
     */
    void onAvatarClick(String username);

    /**
     * 消息气泡框点击事件
     */
    boolean onMessageBubbleClick(EMMessage message);

    /**
     * 消息气泡框长按事件
     */
    void onMessageBubbleLongClick(EMMessage message);

    /**
     * 扩展输入栏item点击事件,如果要覆盖EaseChatFragment已有的点击事件，return true
     *
     * @param view
     * @param itemId
     * @return
     */
    boolean onExtendMenuItemClick(int itemId, View view);

    /**
     * 设置自定义chatrow提供者
     * @return
     */
    EaseCustomChatRowProvider onSetCustomChatRowProvider();
}
