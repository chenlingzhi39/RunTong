package com.callba.phone.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.widget.EaseAlertDialog;
import com.hyphenate.chat.EMClient;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/22.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.add_contact,
        toolbarTitle = R.string.add_friend,
        navigationId = R.drawable.press_back,
        menuId = R.menu.menu_add_contact
)
public class AddContactActivity extends BaseActivity {

    @InjectView(R.id.edit_note)
    EditText editText;
    @InjectView(R.id.avatar)
    ImageView avatar;
    @InjectView(R.id.name)
    TextView nameText;
    @InjectView(R.id.indicator)
    Button indicator;
    @InjectView(R.id.ll_user)
    LinearLayout searchedUserLayout;
    String toAddUsername;
    ProgressDialog progressDialog;
    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.inject(this);
    }
    /**
     * 查找contact
     * @param v
     */
    public void searchContact() {
        final String name = editText.getText().toString();


            toAddUsername = name;
            if(TextUtils.isEmpty(name)) {
                new EaseAlertDialog(this, R.string.Please_enter_a_username).show();
                return;
            }

            // TODO 从服务器获取此contact,如果不存在提示不存在此用户

            //服务器存在此用户，显示此用户和添加按钮
            searchedUserLayout.setVisibility(View.VISIBLE);
            nameText.setText(toAddUsername);

        }


    /**
     *  添加contact
     * @param view
     */
    public void addContact(View view){
        if(EMClient.getInstance().getCurrentUser().equals(nameText.getText().toString())){
            new EaseAlertDialog(this, R.string.not_add_myself).show();
            return;
        }

        if(DemoHelper.getInstance().getContactList().containsKey(nameText.getText().toString())){
            //提示已在好友列表中(在黑名单列表里)，无需添加
            if(EMClient.getInstance().contactManager().getBlackListUsernames().contains(nameText.getText().toString())){
                new EaseAlertDialog(this, R.string.user_already_in_contactlist).show();
                return;
            }
            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo写死了个reason，实际应该让用户手动填入
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMClient.getInstance().contactManager().addContact(toAddUsername, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, 1).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), 1).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.find:
                searchContact();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
