package com.callba.phone.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.ChatAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.refreshlayout.EasyRecyclerView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;


import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/5/27.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.chat,
        navigationId = R.drawable.press_back
)
public class ChatActivity extends BaseActivity {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.content)
    EditText content;
    @InjectView(R.id.submit)
    Button submit;
    @InjectView(R.id.input_menu)
    LinearLayout inputMenu;
    @InjectView(R.id.messages)
    EasyRecyclerView list;
    private ChatAdapter chatAdapter;
    private ArrayList<EMMessage> messages;
    private String userName;
    private ChatReceiver chatReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        chatAdapter=new ChatAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setRefreshEnabled(false);
        messages=getIntent().getParcelableArrayListExtra("messages");
        userName=getIntent().getStringExtra("username");
        title.setText(userName);
        chatAdapter.addAll(messages);
        list.setAdapter(chatAdapter);
        list.showRecycler();
        list.scrollToPosition(messages.size()-1);
        IntentFilter filter = new IntentFilter(
                "com.callba.chat");
        chatReceiver=new ChatReceiver();
        registerReceiver(chatReceiver,filter);
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(chatReceiver);
        super.onDestroy();
    }
    @OnClick(R.id.submit)
    public void onClick(){
        Log.i("submit",userName);
        if(content.getText().toString().equals(""))
            return;
        EMMessage message = EMMessage.createTxtSendMessage(content.getText().toString(),userName);

//发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        chatAdapter.clear();
        messages=(ArrayList<EMMessage>)  EMClient.getInstance().chatManager().getConversation(userName).getAllMessages();
        chatAdapter.addAll(messages);
        list.scrollToPosition(messages.size()-1);


    }
    class ChatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
           if(intent.getStringExtra("username").equals((userName))){
               messages=(ArrayList<EMMessage>) EMClient.getInstance().chatManager().getConversation(userName).getAllMessages();
               chatAdapter.clear();
               chatAdapter.addAll(messages);
               list.scrollToPosition(messages.size()-1);
           }
        }
    }

}
