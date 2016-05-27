package com.callba.phone.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.ChatAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.widget.refreshlayout.EasyRecyclerView;
import com.hyphenate.chat.EMMessage;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        chatAdapter=new ChatAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        messages=getIntent().getParcelableArrayListExtra("messages");
        chatAdapter.addAll(messages);
        list.setAdapter(chatAdapter);
        list.showRecycler();
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }
}
