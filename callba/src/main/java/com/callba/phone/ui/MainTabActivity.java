package com.callba.phone.ui;


import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;

import com.callba.phone.MyApplication;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.widget.BadgeView;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

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
    NotificationManager mNotificationManager;
    private static final int FLING_MIN_DISTANCE = 100;
    private static final int FLING_MIN_VELOCITY = 0;
    BroadcastReceiver tabReceiver;
    private BadgeView badgeView;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;
   /* @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }*/


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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.MANUFACTURER.equals("Xiaomi"))
                ActivityUtil.MIUISetStatusBarLightMode(getWindow(), true);
            if (Build.MANUFACTURER.equals("Meizu"))
                ActivityUtil.FlymeSetStatusBarLightMode(getWindow(), true);
        }

        MyApplication.activities.add(this);


        //关闭登录模块的页面
        ActivityUtil.finishLoginPages();

        mTabhost = this.getTabHost();

        mTabTextArray = getResources().getStringArray(R.array.maintab_texts);
       /* try
        {
            Field current = mTabhost.getClass().getDeclaredField("mCurrentTab");
            current.setAccessible(true);
            current.setInt(mTabhost, 2);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        //放入底部状态栏数据
        for (int i = 0; i < mTabClassArray.length; i++) {
            TabSpec tabSpec = mTabhost.newTabSpec(mTabTextArray[i])
                    .setIndicator(getTabItemView(i))
                    .setContent(getTabItemIntent(i));
            mTabhost.addTab(tabSpec);
            mTabhost.getTabWidget().getChildAt(i);
        }
      /*  try
        {
            Field current = mTabhost.getClass().getDeclaredField("mCurrentTab");
            current.setAccessible(true);
            current.set(mTabhost, -1);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        mTabhost.setCurrentTab(2);
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
                                R.drawable.call_menu_down));
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
                if (!tabId.equals(mTabTextArray[0])) {
                    ImageView iv = (ImageView) view
                            .findViewById(R.id.iv_maintab_icon);
                    iv.setBackgroundResource(R.drawable.menu1_selector);
                } else {
                    ImageView iv = (ImageView) view
                            .findViewById(R.id.iv_maintab_icon);
                    iv.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.call_menu_down));
                    Intent intent = new Intent("com.runtong.phone.diallayout.show");
                    intent.putExtra("action", "show");
                    MainTabActivity.this.sendBroadcast(intent);
                    BaseActivity.flag = true;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.MANUFACTURER.equals("Xiaomi"))
                        ActivityUtil.MIUISetStatusBarLightMode(getWindow(), !tabId.equals(mTabTextArray[4]));
                    if (Build.MANUFACTURER.equals("Meizu"))
                        ActivityUtil.FlymeSetStatusBarLightMode(getWindow(), !tabId.equals(mTabTextArray[4]));

                }
            }
        });

        //异常启动，跳转到第一个页签
       /* if (savedInstanceState != null) {
            try {
                String frompage = getIntent().getStringExtra("frompage");
                if (!TextUtils.isEmpty(frompage)
                        && frompage.equals("WelcomeActivity")) {
                    savedInstanceState.remove("currentTab");
                }
            } catch (Exception e) {
            }
        }*/

        if (getIntent().getBooleanExtra(Constant.ACCOUNT_CONFLICT, false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }
        TabWidget tabs = (TabWidget) findViewById(android.R.id.tabs);
        badgeView=new BadgeView(this,tabs,3);
        int num=EMClient.getInstance().chatManager().getUnreadMsgsCount();
        if(num==0)badgeView.hide();
        else{badgeView.setText(num+"");
        badgeView.show();}
        tabReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ImageView iv = (ImageView) view
                        .findViewById(R.id.iv_maintab_icon);
                    iv.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.call_menu_up));
            }
        };
        registerReceiver(tabReceiver,new IntentFilter("toggle_tab"));
       broadcastManager=LocalBroadcastManager.getInstance(this);
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int i=EMClient.getInstance().chatManager().getUnreadMsgsCount();
                if(i==0)badgeView.hide();
                else {badgeView.setText(i+"");
                    badgeView.show();
            }
        }
        };
        broadcastManager.registerReceiver(broadcastReceiver,new IntentFilter(Constant.ACTION_MESSAGR_NUM_CHANGED));
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
        GlobalConfig.getInstance().saveGlobalCfg(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        GlobalConfig.getInstance().restoreGlobalCfg(state);
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("maintab", "onresume");
        //延迟发送广播（让新来电更新数据库）
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(com.callba.phone.cfg.Constant.ACTION_TAB_ONRESUME);
                sendBroadcast(intent);
            }
        }, 300);
    }

    @Override
    protected void onDestroy() {
        MyApplication.activities.remove(this);
        if (mNotificationManager != null)
            mNotificationManager.cancel(10);
        unregisterReceiver(payReceiver);
        unregisterReceiver(tabReceiver);
        super.onDestroy();
    }

    private void sendNotification1(Class<?> clazz, String title, String content, String username) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext())
                .setSmallIcon(R.drawable.logo_notification)
                .setLargeIcon(
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.logo))
                .setContentTitle(title)
                .setContentText(content);
        Intent notificationIntent = new Intent(getApplicationContext(), clazz);
        notificationIntent.putExtra("username", username);
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
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder.setFullScreenIntent(contentIntent, true);*/
        mBuilder.setTicker(content);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        //notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(10, notification);
    }

    /**
     * 显示帐号在别处登录dialog
     */
    private void showConflictDialog() {
        isConflictDialogShow = true;
        logout();
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserManager.putOriginalPassword(MainTabActivity.this,"");
                UserManager.putPassword(MainTabActivity.this,"");
                LoginController.getInstance().setUserLoginState(false);
                Intent intent0 = new Intent("com.callba.location");
                intent0.putExtra("action", "logout");
                sendBroadcast(intent0);
                Intent intent = new Intent();
                intent.setClass(MainTabActivity.this, LoginActivity.class);
            /*    for (Activity activity : MyApplication.activities) {
                    activity.finish();
                }*/
                finish();
                dialog.dismiss();
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
                UserManager.putUsername(MainTabActivity.this,"");
                UserManager.putPassword(MainTabActivity.this,"");
                LoginController.getInstance().setUserLoginState(false);
                UserManager.putOriginalPassword(MainTabActivity.this, "");
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