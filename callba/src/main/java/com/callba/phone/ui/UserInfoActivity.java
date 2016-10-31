package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.callba.R;
import com.callba.phone.Constant;
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

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.remark)
    TextView tv_remark;
    @BindView(R.id.number)
    TextView number;
    @BindView(R.id.nick_name)
    TextView nickName;
    String userName;
    EaseUser user;
    @BindView(R.id.set_remark)
    Button setRemark;
    @BindView(R.id.frame)
    FrameLayout frame;
    Gson gson;
    ProgressDialog progressDialog;
    @BindView(R.id.clear_chat)
    Button clearChat;
    @BindView(R.id.delete_friend)
    Button deleteFriend;
    @BindView(R.id.signature)
    Button signature;
    @BindView(R.id.send_message)
    Button sendMessage;
    @BindView(R.id.frame1)
    FrameLayout frame1;
    @BindView(R.id.add_friend)
    Button addFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        /*if (getIntent().getBooleanExtra("is_group", false))
            sendMessage.setVisibility(View.VISIBLE);
        else sendMessage.setVisibility(View.GONE);*/
        gson = new Gson();
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
                if (!TextUtils.isEmpty(user.getAvatar()))
                    Glide.with(this).load(user.getAvatar()).into(avatar);
                if (!TextUtils.isEmpty(user.getSign())) {
                    frame1.setVisibility(View.VISIBLE);
                    signature.setText("个性签名:" + user.getSign());
                }

            } else {
                addFriend.setVisibility(View.VISIBLE);
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
                                            Glide.with(UserInfoActivity.this).load(baseUser.getUrl_head()).into(avatar);
                                        if (!TextUtils.isEmpty(baseUser.getNickname()))
                                            tv_remark.setText(baseUser.getNickname());
                                        if (!TextUtils.isEmpty(baseUser.getSign())) {
                                            signature.setText("个性签名:" + baseUser.getSign());
                                            frame1.setVisibility(View.VISIBLE);
                                        }
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

    @OnClick({R.id.set_remark, R.id.clear_chat, R.id.delete_friend, R.id.signature, R.id.send_message,R.id.add_friend})
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
                            LocalBroadcastManager.getInstance(UserInfoActivity.this).sendBroadcast(new Intent(Constant.ACTION_MESSAGE_CHANGED));
                        }
                    }
                }, true).show();
                break;
            case R.id.delete_friend:
                new EaseAlertDialog(UserInfoActivity.this, null, "是否删除好友？", null, new EaseAlertDialog.AlertDialogUser() {

                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        if (confirmed) {
                            InviteMessgeDao dao = new InviteMessgeDao(UserInfoActivity.this);
                            dao.deleteMessage(user.getUsername());
                            // 删除此联系人
                            deleteContact(user);
                            setResult(RESULT_OK);
                        /*    OkHttpUtils
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
                            });*/

                        }
                    }
                }, true).show();
                break;
            case R.id.signature:
                if (!TextUtils.isEmpty(user.getSign())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(user.getSign());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setCancelable(true);
                    alertDialog.show();
                }
                break;
            case R.id.send_message:
                intent = new Intent(this, ChatActivity.class);
                intent.putExtra(Constant.EXTRA_USER_ID, getIntent().getStringExtra("username"));
                startActivity(intent);
                break;
            case R.id.add_friend:
                if(EMClient.getInstance().getCurrentUser().equals(userName)){
                    new EaseAlertDialog(this, R.string.not_add_myself).show();
                    return;
                }

                if(DemoHelper.getInstance().getContactList().containsKey(userName)){
                    //提示已在好友列表中(在黑名单列表里)，无需添加
                    if(EMClient.getInstance().contactManager().getBlackListUsernames().contains(userName)){
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
                            EMClient.getInstance().contactManager().addContact(userName, s);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                    String s1 = getResources().getString(R.string.send_successful);
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
                    }
                }).start();
                break;
        }
    }
    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private EditText change;

        public DialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_change_number, null);
            change = (EditText) mView.findViewById(R.id.et_change);
            change.setInputType(InputType.TYPE_CLASS_TEXT);
            change.requestFocus();
            Timer timer = new Timer(); //设置定时器
            timer.schedule(new TimerTask() {
                @Override
                public void run() { //弹出软键盘的代码
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInputFromWindow(change.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }, 300); //设置300毫秒的时长
        }

        private String getNumber() {
            return change.getText().toString();
        }

        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            mDialog = null;
        }

        public void setDialog(Dialog mDialog) {
            this.mDialog = mDialog;
        }

        public View getView() {
            return mView;
        }
    }

    public void showDialog() {
        final DialogHelper helper = new DialogHelper();
        Dialog dialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setView(helper.getView())
                .setTitle("请输入验证信息")
                .setOnDismissListener(helper)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(EMClient.getInstance().getCurrentUser().equals(userName)){
                            new EaseAlertDialog(UserInfoActivity.this, R.string.not_add_myself).show();
                            return;
                        }

                        if(DemoHelper.getInstance().getContactList().containsKey(userName)){
                            //提示已在好友列表中(在黑名单列表里)，无需添加
                            if(EMClient.getInstance().contactManager().getBlackListUsernames().contains(userName)){
                                new EaseAlertDialog(UserInfoActivity.this, R.string.user_already_in_contactlist).show();
                                return;
                            }
                            new EaseAlertDialog(UserInfoActivity.this, R.string.This_user_is_already_your_friend).show();
                            return;
                        }

                        progressDialog = new ProgressDialog(UserInfoActivity.this);
                        String stri = getResources().getString(R.string.Is_sending_a_request);
                        progressDialog.setMessage(stri);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    //demo写死了个reason，实际应该让用户手动填入
                                    String s = helper.getNumber();
                                    EMClient.getInstance().contactManager().addContact(userName, s);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                            String s1 = getResources().getString(R.string.send_successful);
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
                            }
                        }).start();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        helper.setDialog(dialog);
        dialog.show();
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
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(UserInfoActivity.this, st2 + e.getMessage(), 1).show();
                        }
                    });

                }finally {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            addFriend.setVisibility(View.VISIBLE);
                            deleteFriend.setVisibility(View.GONE);
                            pd.dismiss();}
                    });
                }

            }
        }).start();

    }

}
