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

import com.callba.phone.activity.WelcomeActivity;
import com.callba.phone.util.ActivityUtil;
import com.umeng.analytics.MobclickAgent;

public class MyApplication extends Application {
	/**
	 * 保存所有打开的Activity
	 */
	public static List<Activity> activities = new ArrayList<Activity>();
//	private PushAgent mPushAgent;
	
	private long lastRestartTimeMillis = System.currentTimeMillis();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				try {
					ex.printStackTrace();
					//如果开发者调用 Process.kill 或者 System.exit 之类的方法杀死进程，请务必在此之前调用此方法，用来保存统计数据
					MobclickAgent.reportError(MyApplication.this, ex);
					MobclickAgent.onKillProcess(MyApplication.this);
					
					//避免软件出现问题后，不停的重复启动
					if(System.currentTimeMillis() - lastRestartTimeMillis > 30*1000) {
						//重启应用
						restartAppcation();
					}
					
					ActivityUtil.finishAllActivity();
			        Process.killProcess(Process.myPid());
			        
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		initUmengAnalytics();
//		initUmengPush();
	}
	
	/**
	 * 重启应用程序
	 * @author zhw
	 */
	private void restartAppcation() {
		Intent intent = new Intent();
		intent.setClass(this, WelcomeActivity.class);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
				intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		//100毫秒后重启应用 
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);    
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
		
	public void addActivity(Activity activity){
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
		MobclickAgent.updateOnlineConfig(this);
		//设置时间间隔为5分钟
		MobclickAgent.setSessionContinueMillis(5*60*1000);
	}
	
}
