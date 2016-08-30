package com.callba.phone.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.db.InviteMessgeDao;
import com.callba.phone.db.UserDao;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.EaseAlertDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.UnknownHostException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by PC-20160514 on 2016/8/26.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.user_info,
        toolbarTitle = R.string.user_info,
        navigationId = R.drawable.press_back
)
public class UserInfoActivity extends BaseActivity {
    @InjectView(R.id.avatar)
    ImageView avatar;
    @InjectView(R.id.remark)
    TextView tv_remark;
    @InjectView(R.id.number)
    TextView number;
    @InjectView(R.id.nick_name)
    TextView nickName;
    String userName;
    EaseUser user;
    @InjectView(R.id.set_remark)
    Button setRemark;
    @InjectView(R.id.frame)
    FrameLayout frame;
    Gson gson;
    ProgressDialog progressDialog;
    @InjectView(R.id.clear_chat)
    Button clearChat;
    @InjectView(R.id.delete_friend)
    Button deleteFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        userName = getIntent().getStringExtra("username");
        user = EaseUserUtils.getUserInfo(userName);
        if (userName.length() > 10) {
            number.setHint("手机号:" + userName.substring(0, 11));
            if (user != null) {
                deleteFriend.setVisibility(View.VISIBLE);
                frame.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(user.getNick())) {
                    tv_remark.setText(user.getNick());
                    nickName.setHint("昵称:" + user.getNick());
                }
                if (!TextUtils.isEmpty(user.getRemark())) {
                    tv_remark.setText(user.getRemark());
                    nickName.setVisibility(View.VISIBLE);
                } else nickName.setVisibility(View.GONE);
                if (!user.getAvatar().equals(""))
                    Glide.with(this).load(user.getAvatar()).into(avatar);


            } else {
                gson = new Gson();
                OkHttpUtils.post().url(Interfaces.USER_INFO)
                        .addParams("loginName", getUsername())
                        .addParams("loginPwd", getPassword())
                        .addParams("phoneNumber", userName.substring(0, 11))
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onAfter(int id) {
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onBefore(Request request, int id) {
                                progressDialog = ProgressDialog.show(UserInfoActivity.this, "", "正在获取用户信息");
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                if (e instanceof UnknownHostException) {
                                    toast(R.string.conn_failed);
                                } else toast(R.string.network_error);
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                try {
                                    String[] result = response.split("\\|");
                                    if (result[0].equals("0")) {
                                        BaseUser baseUser = gson.fromJson(result[1], new TypeToken<BaseUser>() {
                                        }.getType());
                                        if (!baseUser.getUrl_head().equals(""))
                                            Glide.with(UserInfoActivity.this).load(baseUser.getUrl_head()).into(avatar);
                                        if (!baseUser.getNickname().equals(""))
                                            tv_remark.setText(baseUser.getNickname());
                                    } else toast(result[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    toast(R.string.getserverdata_exception);
                                }
                            }
                        });
            }
        } else {
            tv_remark.setText(userName);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String remark = data.getStringExtra("remark");
            if (!TextUtils.isEmpty(remark)) {
                tv_remark.setText(remark);
                nickName.setVisibility(View.VISIBLE);
                nickName.setHint("昵称:" + user.getNick());
            } else {
                tv_remark.setText(user.getNick());
                nickName.setVisibility(View.GONE);
            }
        }
    }

    @OnClick({R.id.set_remark, R.id.clear_chat, R.id.delete_friend})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_remark:
                Intent intent = new Intent(UserInfoActivity.this, RemarkActivity.class);
                intent.putExtra("username", userName);
                startActivityForResult(intent, 0);
                break;
            case R.id.clear_chat:
                new EaseAlertDialog(UserInfoActivity.this, null, "是否清空聊天记录？", null, new EaseAlertDialog.AlertDialogUser() {

                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        if (confirmed) {
                            EMClient.getInstance().chatManager().deleteConversation(userName, true);

                        }
                    }
                }, true).show();
                break;
            case R.id.delete_friend:
                new EaseAlertDialog(UserInfoActivity.this, null, "是否删除好友？", null, new EaseAlertDialog.AlertDialogUser() {

                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        if (confirmed) {
                            OkHttpUtils
                                    .post()
                                    .url(Interfaces.DELETE_FRIENDS)
                                    .addParams("loginName", getUsername())
                                    .addParams("loginPwd", getPassword())
                                    .addParams("phoneNumber", userName.substring(0, 11))
                                    .build().execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    toast("删除失败");
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    try {
                                        String[] result = response.split("\\|");
                                        Logger.i("delete_result", response);
                                        if (result[0].equals("0")) {
                                            // 删除相关的邀请消息
                                            InviteMessgeDao dao = new InviteMessgeDao(UserInfoActivity.this);
                                            dao.deleteMessage(user.getUsername());
                                            // 删除此联系人
                                            deleteContact(user);
                                            setResult(RESULT_OK);
                                        } else {
                                            toast("删除失败");
                                        }
                                    } catch (Exception e) {
                                        toast(R.string.getserverdata_exception);
                                    }
                                }
                            });

                        }
                    }
                }, true).show();
                break;
        }
    }

    /**
     * 删除联系人
     *
     * @param toDeleteUser
     */
    public void deleteContact(final EaseUser tobeDeleteUser) {
        String st1 = getResources().getString(R.string.deleting);
        final String st2 = getResources().getString(R.string.Delete_failed);
        final ProgressDialog pd = new ProgressDialog(UserInfoActivity.this);
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(tobeDeleteUser.getUsername());
                    // 删除db和内存中此用户的数据
                    UserDao dao = new UserDao(UserInfoActivity.this);
                    dao.deleteContact(tobeDeleteUser.getUsername());
                    DemoHelper.getInstance().getContactList().remove(tobeDeleteUser.getUsername());
                    finish();
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(UserInfoActivity.this, st2 + e.getMessage(), 1).show();
                        }
                    });

                }

            }
        }).start();

    }

}
