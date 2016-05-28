package com.callba.phone.activity;


import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.MyApplication;
import com.callba.phone.activity.contact.ContactActivity;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.adapter.ConversationAdapter;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主界面
 *
 * @author zxf
 */
@SuppressWarnings("deprecation")
public class MainTabActivity extends TabActivity {
    private TabHost mTabhost;

    private String mTabTextArray[] = null;
    private boolean isConflictDialogShow;
    private boolean isAccountRemovedDialogShow;
    @SuppressWarnings("rawtypes")
    private Class[] mTabClassArray = {MainCallActivity.class,
            ContactActivity.class, HomeActivity.class,
            MessageActivity.class, UserActivity.class};

    private int[] mTabImageArray = {R.drawable.menu1_selector,
            R.drawable.menu2_selector, R.drawable.menu3_selector,
            R.drawable.menu4_selector, R.drawable.menu5_selector};
    EMMessageListener msgListener;
    NotificationManager mNotificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(Color.TRANSPARENT);
        }
        MyApplication.activities.add(this);

        if (savedInstanceState != null) {
            //恢复保存到数据
            CalldaGlobalConfig.getInstance().restoreGlobalCfg(savedInstanceState);
        }

        //关闭登录模块的页面
        ActivityUtil.finishLoginPages();

        mTabhost = this.getTabHost();

        mTabTextArray = getResources().getStringArray(R.array.maintab_texts);

        //放入底部状态栏数据
        for (int i = 0; i < mTabClassArray.length; i++) {
            TabSpec tabSpec = mTabhost.newTabSpec(mTabTextArray[i])
                    .setIndicator(getTabItemView(i))
                    .setContent(getTabItemIntent(i));
            mTabhost.addTab(tabSpec);
            mTabhost.getTabWidget().getChildAt(i);
        }

        //获取第一个tabwidget
        final View view = mTabhost.getTabWidget().getChildAt(0);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTabhost.getCurrentTab() == 0) {
                    Intent intent = new Intent("com.runtong.phone.diallayout.show");
                    ImageView iv = (ImageView) view
                            .findViewById(R.id.iv_maintab_icon);
                    if (BaseActivity.flag) {
                        iv.setBackgroundDrawable(getResources().getDrawable(
                                R.drawable.call_menu_up));
                        intent.putExtra("action", "hide");
                    } else {
                        iv.setBackgroundDrawable(getResources().getDrawable(
                                R.drawable.call_menu_downs));
                        intent.putExtra("action", "show");
                    }

                    MainTabActivity.this.sendBroadcast(intent);
                    BaseActivity.flag = !BaseActivity.flag;
                }
                mTabhost.setCurrentTab(0);
            }
        });

        mTabhost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (!tabId.equals(getString(R.string.call))) {
                    ImageView iv = (ImageView) view
                            .findViewById(R.id.iv_maintab_icon);
                    iv.setBackgroundResource(R.drawable.menu1_selector);
                } else {
                    ImageView iv = (ImageView) view
                            .findViewById(R.id.iv_maintab_icon);
                    iv.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.call_menu_downs));
                    Intent intent = new Intent("com.runtong.phone.diallayout.show");
                    intent.putExtra("action", "show");
                    MainTabActivity.this.sendBroadcast(intent);
                    BaseActivity.flag = true;
                }
            }
        });

        //异常启动，跳转到第一个页签
        if (savedInstanceState != null) {
            try {
                String frompage = getIntent().getStringExtra("frompage");
                if (!TextUtils.isEmpty(frompage)
                        && frompage.equals("WelcomeActivity")) {
                    savedInstanceState.remove("currentTab");
                }
            } catch (Exception e) {
            }
        }
        msgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                for (EMMessage message : messages) {
                    Log.i("get_message", message.getBody().toString());
                    EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
                    sendNotification1(ChatActivity.class, "你有一条新消息", message.getFrom() + ":" + txtBody.getMessage(),message.getFrom());
                    Intent intent = new Intent("com.callba.chat");
                    intent.putExtra("username",message.getFrom());
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }

            @Override
            public void onMessageReadAckReceived(List<EMMessage> messages) {
                //收到已读回执
            }

            @Override
            public void onMessageDeliveryAckReceived(List<EMMessage> message) {
                //收到已送达回执
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
            }
        };
        if (getIntent().getBooleanExtra(Constant.ACCOUNT_CONFLICT, false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(Constant.ACCOUNT_CONFLICT, false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (intent.getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }
    }

    private View getTabItemView(int index) {
        View view = View.inflate(this, R.layout.maintab_item, null);
        ImageView imageView = (ImageView) view
                .findViewById(R.id.iv_maintab_icon);
        TextView textview = (TextView) view.findViewById(R.id.tv_maintab_text);
        imageView.setBackgroundResource(mTabImageArray[index]);
        textview.setText(mTabTextArray[index]);
        return view;
    }

    private Intent getTabItemIntent(int index) {
        Intent intent = new Intent(this, mTabClassArray[index]);
        intent.putExtra("frompage", "all");
        return intent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //保存全局参数
        CalldaGlobalConfig.getInstance().saveGlobalCfg(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //延迟发送广播（让新来电更新数据库）
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Constant.ACTION_TAB_ONRESUME);
                sendBroadcast(intent);
            }
        }, 300);
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }

    @Override
    protected void onStop() {


        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MyApplication.activities.remove(this);
        if(mNotificationManager!=null)
        mNotificationManager.cancel(10);
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        super.onDestroy();
    }

    private void sendNotification1(Class<?> clazz, String title, String content,String username) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext())
                .setSmallIcon(R.drawable.logo_notification)
                .setLargeIcon(
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.logo))
                .setContentTitle(title)
                .setContentText(content);
        Intent notificationIntent = new Intent(getApplicationContext(), clazz);
        notificationIntent.putExtra("username",username);
        // TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // stackBuilder.addParentStack(clazz);
        // stackBuilder.addNextIntent(notificationIntent);
        // PendingIntent resultPendingIntent =
        // stackBuilder.getPendingIntent(
        // 0,
        // PendingIntent.FLAG_UPDATE_CURRENT
        // );
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder.setFullScreenIntent(contentIntent, true);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(10, notification);
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
    /**
     * 显示帐号在别处登录dialog
     */
    private void showConflictDialog() {
        isConflictDialogShow = true;
        logout();
       AlertDialog.Builder builder= new AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CalldaGlobalConfig.getInstance().setUsername("");
                CalldaGlobalConfig.getInstance().setPassword("");
                CalldaGlobalConfig.getInstance().setIvPath("");
                LoginController.getInstance().setUserLoginState(false);
                SharedPreferenceUtil.getInstance(MainTabActivity.this).putString(Constant.LOGIN_PASSWORD, "", true);
                Intent intent0 = new Intent("com.callba.location");
                intent0.putExtra("action", "logout");
                sendBroadcast(intent0);
                Intent intent = new Intent();
                intent.setClass(MainTabActivity.this, LoginActivity.class);
                for (Activity activity : MyApplication.activities) {
                    activity.finish();
                }
                startActivity(intent);
            }
        });
        builder.setTitle(getString(R.string.Logoff_notification));
        builder.setMessage(getString(R.string.connect_conflict));
        builder.setCancelable(false);
        builder.create().show();
    }

    /**
     * 帐号被移除的dialog
     */
    private void showAccountRemovedDialog() {
        isAccountRemovedDialogShow = true;
        logout();
        Dialog dialog = new AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CalldaGlobalConfig.getInstance().setUsername("");
                CalldaGlobalConfig.getInstance().setPassword("");
                CalldaGlobalConfig.getInstance().setIvPath("");
                LoginController.getInstance().setUserLoginState(false);
                SharedPreferenceUtil.getInstance(MainTabActivity.this).putString(Constant.LOGIN_PASSWORD, "", true);
                Intent intent0 = new Intent("com.callba.location");
                intent0.putExtra("action", "logout");
                sendBroadcast(intent0);
                Intent intent = new Intent();
                intent.setClass(MainTabActivity.this, LoginActivity.class);
                for (Activity activity : MyApplication.activities) {
                    activity.finish();
                }
                startActivity(intent);
            }
        }).create();
        dialog.setCancelable(false);
        dialog.show();

    }

    public void logout() {
        EMClient.getInstance().logout(false, new EMCallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.d("main", "退出聊天服务器成功！");
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub
                Log.d("main", "退出聊天服务器失败！");
            }
        });
    }



}
