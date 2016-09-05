package com.callba.phone.ui.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.MyApplication;
import com.callba.phone.SystemBarTintManager;
import com.callba.phone.bean.ApiService;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.ui.BalanceActivity;
import com.callba.phone.ui.ContactDetailActivity;
import com.callba.phone.ui.HomeActivity;
import com.callba.phone.ui.LoginActivity;
import com.callba.phone.ui.MainCallActivity;
import com.callba.phone.ui.MainTabActivity;
import com.callba.phone.ui.MessageActivity;
import com.callba.phone.ui.UserActivity;
import com.callba.phone.ui.ContactActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.manager.UserManager;
import com.callba.phone.service.MainService;
import com.callba.phone.service.UpdateService;
import com.callba.phone.ui.WelcomeActivity;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.AppVersionChecker;
import com.callba.phone.util.Logger;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.List;


import javax.inject.Inject;

import rx.Subscription;

public class BaseActivity extends AppCompatActivity {
    public static Boolean flag = true;
    /**
     * 布局的id
     */
    protected int mContentViewId;
    /**
     * 返回键图片id
     */
    private int navigationId;
    /**
     * 菜单的id
     */
    private int mMenuId;
    /**
     * Toolbar标题
     */
    private int mToolbarTitle;
    /**
     * 当前页面是否发送通知
     */
    private boolean isSendNotification = true;

    /**
     * 初始化界面
     */

    public Toolbar toolbar;

    /*@Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }*/
    public Subscription subscription;
    public AlertDialog dialog;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getClass().isAnnotationPresent(ActivityFragmentInject.class)) {
            ActivityFragmentInject annotation = getClass()
                    .getAnnotation(ActivityFragmentInject.class);
            mContentViewId = annotation.contentViewId();
            navigationId = annotation.navigationId();
            mMenuId = annotation.menuId();
            mToolbarTitle = annotation.toolbarTitle();
        } else if (getClass() != WelcomeActivity.class) {
            throw new RuntimeException(
                    "Class must add annotations of ActivityFragmentInitParams.class");
        }
        if (getClass() != WelcomeActivity.class) {
            setContentView(mContentViewId);
            initToolbar();
            if (mToolbarTitle != -1)
                setToolbarTitle(mToolbarTitle);
        }

        MyApplication.activities.add(this);

	/*	if (this.getClass() != MainCallActivity.class
                && this.getClass() != WelcomeActivity.class
				&& this.getClass() != FunIntroduceActivity.class
				&& this.getClass() != GuideActivity.class
				&& this.getClass() != LoginActivity.class
				&& this.getClass() != RegisterActivity.class
				&& this.getClass() != OnekeyRegisterAcitvity.class
				&& this.getClass() != RetrievePasswordActivity.class) {
			// 检查内存数据是否正常
			String username = GlobalConfig.getInstance().getUsername();
			String password = GlobalConfig.getInstance().getPassword();
			if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
				// 重新打开
				Intent intent = new Intent();
				intent.setClass(this, WelcomeActivity.class);
				startActivity(intent);

				// 关闭主tab页面
				finish();
				ActivityUtil.finishMainTabPages();
			}
		}*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.getClass() != UserActivity.class && this.getClass() != ContactDetailActivity.class) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) && !Build.MANUFACTURER.equals("Xiaomi") && !Build.MANUFACTURER.equals("Meizu")) {
            if (this.getClass() == MainCallActivity.class ||
                    this.getClass() == ContactActivity.class ||
                    this.getClass() == HomeActivity.class ||
                    this.getClass() == MessageActivity.class) {
                SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
                systemBarTintManager.setStatusBarTintEnabled(true);
                systemBarTintManager.setStatusBarTintResource(R.color.gray_status);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (this.getClass() != UserActivity.class && this.getClass() != ContactDetailActivity.class) {
                if (Build.MANUFACTURER.equals("Xiaomi"))
                    ActivityUtil.MIUISetStatusBarLightMode(getWindow(), true);
                if (Build.MANUFACTURER.equals("Meizu"))
                    ActivityUtil.FlymeSetStatusBarLightMode(getWindow(), true);
            }
        }
        Log.i("manufacturer", Build.MANUFACTURER);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null) subscription.unsubscribe();
        MyApplication.activities.remove(this);

        if (MyApplication.activities.size() < 1) {
            cancelNotification();

            // 停止服务
            try {
                stopService(new Intent(this, MainService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (navigationId != -1) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbar.setNavigationIcon(navigationId);
            }
            getSupportActionBar().setTitle("");


        }
    }

    protected void setToolbarTitle(int strId) {
        if (getSupportActionBar() != null) {
            TextView title = (TextView) findViewById(R.id.title);
            title.setText(getResources().getString(mToolbarTitle));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mMenuId != -1 && getClass() != WelcomeActivity.class)
            getMenuInflater().inflate(mMenuId, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i("base", "finish");
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 发送通知栏广播
     *
     * @author zhw
     */
    private void showBackRunNotification() {
        Class<?> clazz = this.getClass();
        if (clazz.getName()
                .equals("com.callba.phone.ui.MainCallActivity")
                || clazz.getName().equals(
                "com.callba.phone.activity.contact.ContactActivity")
                || clazz.getName()
                .equals("com.callba.phone.ui.HomeActivity")
                || clazz.getName().equals(
                "com.callba.phone.ui.MessageActivity")
                || clazz.getName().equals(
                "com.callba.phone.ui.UserActivity")) {

            clazz = MainTabActivity.class;
//			clazz = OpenTabHelperActivity.class;
        }

        sendNotification1(clazz);
    }

    /**
     * 设置通知默认打开页面为 ManinTab
     *
     * @author zhw
     */
    public void restoreNotificationOpenPage() {
        sendNotification1(MainTabActivity.class);
    }

    // /**
    // * 发送通知
    // * @author zhw
    // * @param clazz
    // */
    // @SuppressLint("NewApi")
    // private void sendNotification(Class<?> clazz) {
    // Notification notification = new Notification(R.drawable.logo_notication,
    // "", System.currentTimeMillis());
    // NotificationManager notificationManager = (NotificationManager)
    // getSystemService(NOTIFICATION_SERVICE);
    //
    // Context context = getApplicationContext();
    // CharSequence contentTitle = getString(R.string.contenttitle);
    // CharSequence contentText = getString(R.string.contenttext);
    // notification.flags = Notification.FLAG_ONGOING_EVENT;//设置常驻
    // Intent notificationIntent = new Intent(context, clazz);
    // PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
    // notificationIntent, 0);
    // notification.setLatestEventInfo(context, contentTitle, contentText,
    // contentIntent);
    // if(Build.VERSION.SDK_INT >= 11) {
    // notification.largeIcon = BitmapFactory.decodeResource(getResources(),
    // R.drawable.logo);
    // }
    //
    // notificationManager.notify(10, notification);
    // }

    /**
     * 发送通知
     *
     * @param clazz
     * @author zhw
     */
    private void sendNotification1(Class<?> clazz) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext())
                .setSmallIcon(R.drawable.logo_notification)
                .setLargeIcon(
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.logo))
                .setContentTitle(getString(R.string.contenttitle))
                .setContentText(getString(R.string.contenttext));

        Intent notificationIntent = new Intent(getApplicationContext(), clazz);

        // TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // stackBuilder.addParentStack(clazz);
        // stackBuilder.addNextIntent(notificationIntent);
        // PendingIntent resultPendingIntent =
        // stackBuilder.getPendingIntent(
        // 0,
        // PendingIntent.FLAG_UPDATE_CURRENT
        // );
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, 0);
        mBuilder.setContentIntent(contentIntent);

        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(10, notification);
    }

    /**
     * Android判断Intent是否存在，是否可用
     *
     * @param context
     * @param intent
     * @return
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

    /**
     * 关闭所有通知
     */
    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onResume() {
        if (isSendNotification) {
            //showBackRunNotification();
        }
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    public boolean isSendNotification() {
        return isSendNotification;
    }

    public void setSendNotification(boolean isSendNotification) {
        this.isSendNotification = isSendNotification;
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void toast(int id) {
        Toast.makeText(this, getString(id), Toast.LENGTH_SHORT).show();
    }

    public void check2Upgrade(final AppVersionChecker.AppVersionBean appVersionBean,final boolean is_toast) {
        if (appVersionBean.isHasNewVersion()) {
            if (appVersionBean.isForceUpgrade()) {
                // 强制升级
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sjts);
                builder.setMessage(R.string.sjtsxx);
                builder.setPositiveButton(R.string.upgrade,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!UpdateService.is_downloading) {
                                    toast("开始下载更新");
                                    Intent updateIntent = new Intent(
                                            getApplicationContext(),
                                            UpdateService.class);
                                    updateIntent.putExtra(
                                            "url",
                                            appVersionBean
                                                    .getDownloadUrl());
                                    updateIntent.putExtra("version_code", appVersionBean.getServerVersionCode());
                                    startService(updateIntent);

                                } else {
                                    toast("正在下载更新");
                                }

                            }
                        });
                dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();

            } else {


                // 存在新版本
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sjts);
                builder.setMessage("发现新版本" + appVersionBean.getServerVersionCode() + "，是否现在升级？");
                builder.setPositiveButton(R.string.upgrade,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                if (!UpdateService.is_downloading) {
                                    toast("开始下载更新");
                                    Intent updateIntent = new Intent(
                                            getApplicationContext(),
                                            UpdateService.class);
                                    updateIntent.putExtra(
                                            "url",
                                            appVersionBean
                                                    .getDownloadUrl());
                                    updateIntent.putExtra("version_code", appVersionBean.getServerVersionCode());
                                    startService(updateIntent);
                                } else {
                                    toast("正在下载更新");
                                }

                            }
                        });
                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {


                            }
                        });

                dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!is_toast)
                            showActivity();
                    }
                });
                dialog.show();

            }
        } else {
            // 无新版本
            if (is_toast) toast("已是最新版本");
            if (!is_toast) showActivity();
        }
    }

    public String getUsername() {
        return UserManager.getUsername(this);
    }

    public String getPassword() {
        return UserManager.getPassword(this);
    }

    public void showActivity() {

    }
}
