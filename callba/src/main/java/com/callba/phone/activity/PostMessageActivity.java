package com.callba.phone.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.umeng.socialize.utils.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by PC-20160514 on 2016/5/26.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.post_message,
        toolbarTitle = R.string.post_message,
        navigationId = R.drawable.press_back
)
public class PostMessageActivity extends BaseActivity {

    @InjectView(R.id.number)
    EditText number;
    @InjectView(R.id.content)
    EditText content;
    @InjectView(R.id.submit)
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }


    @OnClick(R.id.submit)
    public void onClick() {
        Log.i("submit","onclick");
        if(content.getText().toString().equals(""))
           return;
        EMMessage message = EMMessage.createTxtSendMessage(content.getText().toString(),number.getText().toString());

//发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
    }
}
