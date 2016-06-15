package com.callba.phone;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.callba.R;
import com.callba.phone.activity.MainTabActivity;
import com.callba.phone.activity.WelcomeActivity;
import com.callba.phone.cfg.Constant;
import com.callba.phone.controller.EaseUI;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.util.StorageUtils;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.umeng.socialize.utils.Log;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MyApplication extends Application {
    /**
     * 保存所有打开的Activity
     */
    public static List<Activity> activities = new ArrayList<Activity>();
//	private PushAgent mPushAgent;

    private long lastRestartTimeMillis = System.currentTimeMillis();
    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
        }
        @Override
        public void onDisconnected(final int error) {

            if (error == EMError.USER_REMOVED) {
                Log.i("user","removed");
                onCurrentAccountRemoved();
            }else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                Log.i("user","another");
                onConnectionConflict();
            }
        }}
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/STXIHEI.TTF").setFontAttrId(R.attr.fontPath).build());
        EMOptions options = new EMOptions();
// 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        options.setAutoLogin(false);
        EaseUI.getInstance().init(this,options);
/*//初始化
        EMClient.getInstance().init(this, options);
//在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
//注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());*/


       /* Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                try {
                    ex.printStackTrace();
                    //如果开发者调用 Process.kill 或者 System.exit 之类的方法杀死进程，请务必在此之前调用此方法，用来保存统计数据
                    MobclickAgent.reportError(MyApplication.this, ex);
                    MobclickAgent.onKillProcess(MyApplication.this);

                    //避免软件出现问题后，不停的重复启动
                    if (System.currentTimeMillis() - lastRestartTimeMillis > 30 * 1000) {
                        //重启应用
                        restartAppcation();
                    }

                    ActivityUtil.finishAllActivity();
                    Process.killProcess(Process.myPid());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
      /*  EMChat.getInstance().init(this);

*//**
 * debugMode == true 时为打开，SDK会在log里输入调试信息
 * @param debugMode
 * 在做代码混淆的时候需要设置成false
 */
        /*
        EMChat.getInstance().setDebugMode(true);*/
        GlideBuilder builder = new GlideBuilder(this);
        builder.setMemoryCache(new LruResourceCache(5 * 1024 * 1024));
        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);
        builder.setDiskCache(
                new ExternalCacheDiskCacheFactory(this, StorageUtils.getCacheDirectory(getApplicationContext()).getPath(), 10 * 1024 * 1024));
        initUmengAnalytics();

//		initUmengPush();
    }

    /**
     * 重启应用程序
     *
     * @author zhw
     */
    private void restartAppcation() {
        Intent intent = new Intent();
        intent.setClass(this, WelcomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, Intent.FLAG_ACTIVITY_NEW_TASK);

        //100毫秒后重启应用
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                pendingIntent);
    }

//	private void initUmengPush() {
//		mPushAgent = PushAgent.getInstance(this);
//		mPushAgent.setDebugMode(true);
//		/**
//		 * 该Handler是在BroadcastReceiver中被调用，故
//		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
//		 * */
//		UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
//			@Override
//			public void dealWithCustomAction(Context context, UMessage msg) {
////				Intent intent =new Intent();
////				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////				intent.setClass(context, DialogActivity.class);
////				startActivity(intent);
//				
//				CalldaToast calldaToast = new CalldaToast();
//				calldaToast.showToast(context, msg.custom);
//			}
//		};
//		mPushAgent.setNotificationClickHandler(notificationClickHandler);
//	}

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void onTerminate() {
        super.onTerminate();
        for (Activity activity : activities) {
            activity.finish();
        }
        System.exit(0);
    }

    /**
     * 初始化友盟统计参数设置
     */
    private void initUmengAnalytics() {
       /* MobclickAgent.updateOnlineConfig(this);
        //设置时间间隔为5分钟
        MobclickAgent.setSessionContinueMillis(5 * 60 * 1000);*/
    }
    /**
     * 账号在别的设备登录
     */
    protected void onConnectionConflict(){
        Intent intent = new Intent(getApplicationContext(), MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_CONFLICT, true);
        getApplicationContext().startActivity(intent);
    }

    /**
     * 账号被移除
     */
    protected void onCurrentAccountRemoved(){
        Intent intent = new Intent(getApplicationContext(), MainTabActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        getApplicationContext().startActivity(intent);
    }
}
