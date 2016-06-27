package com.callba.phone.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.ConversationAdapter;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerItemDecoration;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Administrator on 2016/5/15.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_message,
        toolbarTitle = R.string.message,
        menuId = R.menu.menu_message
)

public class MessageActivity extends BaseActivity {
    @InjectView(R.id.conversation_list)
    RecyclerView conversationListview;
    //EaseConversationList conversationListView;
    protected List<EMConversation> conversationList = new ArrayList<>();
    private ConversationAdapter adapter;
    private ChatReceiver chatReceiver;
    private AsReadReceiver asReadReceiver;
    private int index=-1;
    public interface EaseConversationListItemClickListener {
        /**
         * 会话listview item点击事件
         *
         * @param conversation 被点击item所对应的会话
         */
        void onListItemClicked(EMConversation conversation);
    }

    private EaseConversationListItemClickListener listItemClickListener;

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        String language = Locale.getDefault().getLanguage();
        Logger.i("language",language);
        Locale.setDefault(new Locale("zh"));
        Logger.i("language",Locale.getDefault().getLanguage());
        conversationList.addAll(loadConversationList());
        conversationListview.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        conversationListview.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ConversationAdapter(this);
        adapter.addAll(conversationList);
        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
             adapter.getData().get(position).markAllMessagesAsRead();
             Intent intent=new Intent(MessageActivity.this,ChatActivity.class);
                intent.putExtra("username",conversationList.get(position).getUserName());
                startActivity(intent);
                adapter.notifyItemChanged(position);
            }
        });
        conversationListview.setAdapter(adapter);
        IntentFilter filter = new IntentFilter(
                "com.callba.chat");
        IntentFilter filter1 = new IntentFilter(
                "com.callba.asread");
        chatReceiver=new ChatReceiver();
        registerReceiver(chatReceiver,filter);
        asReadReceiver=new AsReadReceiver();
        registerReceiver(asReadReceiver,filter1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Intent intent = new Intent(MessageActivity.this, PostMessageActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取会话列表
     *
     * @param context
     * @return +
     */
    protected List<EMConversation> loadConversationList() {
        // 获取所有会话，包括陌生人
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        // 过滤掉messages size为0的conversation
        /**
         * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
         * 影响排序过程，Collection.sort会产生异常
         * 保证Conversation在Sort过程中最后一条消息的时间不变
         * 避免并发问题
         */
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    //if(conversation.getType() != EMConversationType.ChatRoom){
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                    //}
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(chatReceiver);
        unregisterReceiver(asReadReceiver);
        super.onDestroy();
    }

    /**
     * 根据最后一条消息的时间排序
     *
     * @param usernames
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }
    class ChatReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            conversationList.clear();
            conversationList.addAll(loadConversationList());
            adapter.clear();
            adapter.addAll(conversationList);
        }
    }
    class AsReadReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            conversationList.clear();
            conversationList.addAll(loadConversationList());
            adapter.clear();
            adapter.addAll(conversationList);
        }
    }
    /**
     * 重写onkeyDown 捕捉返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 转到后台运行
            ActivityUtil.moveAllActivityToBack();
            return true;
        }
        return false;
    }
}
