package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.manager.UserManager;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/10/26.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_group_user_info,
        toolbarTitle = R.string.group_user_info,
        navigationId = R.drawable.press_back
)
public class GroupUserInfoActivity extends BaseActivity {
    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.phone_number)
    TextView phoneNumber;
    @BindView(R.id.sign)
    TextView sign;
    @BindView(R.id.add_friend)
    Button addFriend;
    @BindView(R.id.send_message)
    Button sendMessage;
    String username;
    EaseUser user;
    Gson gson;
    ProgressDialog progressDialog;
    private EMGroupChangeListener emGroupChangeListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        emGroupChangeListener = new EMGroupChangeListener() {
            @Override
            public void onInvitationReceived(String s, String s1, String s2, String s3) {

            }

            @Override
            public void onApplicationReceived(String s, String s1, String s2, String s3) {

            }

            @Override
            public void onApplicationAccept(String s, String s1, String s2) {

            }

            @Override
            public void onApplicationDeclined(String s, String s1, String s2, String s3) {

            }

            @Override
            public void onInvitationAccepted(String s, String s1, String s2) {

            }

            @Override
            public void onInvitationDeclined(String s, String s1, String s2) {

            }

            @Override
            public void onUserRemoved(String s, String s1) {
                if (s.equals(getIntent().getStringExtra("group_id")))
                    finish();
            }

            @Override
            public void onGroupDestroyed(String s, String s1) {
                if (s.equals(getIntent().getStringExtra("group_id")))
                    finish();
            }

            @Override
            public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {

            }
        };
        EMClient.getInstance().groupManager().addGroupChangeListener(emGroupChangeListener);
        username = getIntent().getStringExtra("username");
        user = EaseUserUtils.getUserInfo(username);
        if (username.equals(EMClient.getInstance().getCurrentUser())) {
            addFriend.setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);
            Glide.with(this).load(UserManager.getUserAvatar(this)).into(avatar);
            phoneNumber.setText(UserManager.getUsername(this));
            name.setText(UserManager.getNickname(this));
            sign.setText(UserManager.getSignature(this));
        }else
        if (user != null) {
            addFriend.setVisibility(View.GONE);
            if(!TextUtils.isEmpty(user.getAvatar()))
            Glide.with(this).load(user.getAvatar()).into(avatar);
            phoneNumber.setText(username.substring(0, 11));
            name.setText(user.getNick());
            sign.setText(user.getSign());
        } else {
            phoneNumber.setText(username.substring(0,11));
            gson = new Gson();
            OkHttpUtils.post().url(Interfaces.USER_INFO)
                    .addParams("loginName", getUsername())
                    .addParams("loginPwd", getPassword())
                    .addParams("phoneNumber", username.substring(0, 11))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onAfter(int id) {
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onBefore(Request request, int id) {
                            progressDialog = ProgressDialog.show(GroupUserInfoActivity.this, "", "正在获取用户信息");
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            showException(e);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                String[] result = response.split("\\|");
                                if (result[0].equals("0")) {
                                    BaseUser baseUser = gson.fromJson(result[1], new TypeToken<BaseUser>() {
                                    }.getType());
                                    user = new EaseUser(baseUser.getPhoneNumber());
                                    user.setRemark(baseUser.getRemark());
                                    user.setSign(baseUser.getSign());
                                    user.setAvatar(baseUser.getUrl_head());
                                    if (!TextUtils.isEmpty(baseUser.getUrl_head()))
                                        Glide.with(GroupUserInfoActivity.this).load(baseUser.getUrl_head()).into(avatar);
                                    if (!TextUtils.isEmpty(baseUser.getNickname()))
                                        name.setText(baseUser.getNickname());
                                    if (!TextUtils.isEmpty(baseUser.getSign())) {
                                        sign.setText(baseUser.getSign());
                                    }
                                } else toast(result[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                toast(R.string.getserverdata_exception);
                            }
                        }
                    });
        }
    }

    @OnClick({R.id.add_friend, R.id.send_message})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_friend:
                OkHttpUtils
                        .post()
                        .url(Interfaces.ADD_FRIEND)
                        .addParams("loginName", getUsername())
                        .addParams("loginPwd", getPassword())
                        .addParams("phoneNumber", username.substring(0, 11))
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                e.printStackTrace();
                                showException(e);
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                try {
                                    Logger.i("add_result", response);
                                    String[] result = response.split("\\|");
                                    if (result[0].equals("0")) {
                                        try {
                                            //demo写死了个reason，实际应该让用户手动填入
                                            String s = getResources().getString(R.string.Add_a_friend);
                                            //EMClient.getInstance().contactManager().addContact(toAddUsername+"-callba", s);
                                            sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                            OkHttpUtils
                                                    .post()
                                                    .url(Interfaces.GET_FRIENDS)
                                                    .addParams("loginName", getUsername())
                                                    .addParams("loginPwd", getPassword())
                                                    .build().execute(new StringCallback() {
                                                @Override
                                                public void onError(Call call, Exception e, int id) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onResponse(String response, int id) {
                                                    try {
                                                        Logger.i("get_result", response);
                                                        String[] result = response.split("\\|");
                                                        if (result[0].equals("0")) {
                                                            ArrayList<BaseUser> list;
                                                            list = gson.fromJson(result[1], new TypeToken<ArrayList<BaseUser>>() {
                                                            }.getType());
                                                            List<EaseUser> mList = new ArrayList<EaseUser>();
                                                            for (BaseUser baseUser : list) {
                                                                EaseUser user = new EaseUser(baseUser.getPhoneNumber() + "-callba");
                                                                user.setAvatar(baseUser.getUrl_head());
                                                                user.setNick(baseUser.getNickname());
                                                                user.setSign(baseUser.getSign());
                                                                user.setRemark(baseUser.getRemark());
                                                                EaseCommonUtils.setUserInitialLetter(user);
                                                                mList.add(user);
                                                            }
                                                            DemoHelper.getInstance().updateContactList(mList);
                                                            LocalBroadcastManager.getInstance(GroupUserInfoActivity.this).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                                            setResult(RESULT_OK);
                                                        }
                                                    } catch (Exception e) {
                                                        toast(R.string.getserverdata_exception);
                                                    }
                                                }
                                            });
                                            addFriend.setVisibility(View.GONE);
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressDialog.dismiss();
                                                    String s1 = "添加成功";
                                                    Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (final Exception e) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    progressDialog.dismiss();
                                                    String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                                    Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else {
                                        toast(result[1]);
                                        progressDialog.dismiss();
                                    }
                                } catch (Exception e) {
                                    toast(R.string.getserverdata_exception);
                                }
                            }
                        });
                break;
            case R.id.send_message:
                startActivityForResult(new Intent(this, ChatActivity.class).putExtra(Constant.EXTRA_USER_ID, getIntent().getStringExtra("username")),0);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().groupManager().removeGroupChangeListener(emGroupChangeListener);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
           setResult(RESULT_OK);
    }
}
