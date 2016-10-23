package com.callba.phone.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import com.callba.phone.MyApplication;
import com.callba.phone.ui.CallbackDisplayActivity;
import com.callba.phone.ui.GuideActivity;
import com.callba.phone.ui.LoginActivity;
import com.callba.phone.ui.MainTabActivity;
import com.callba.phone.ui.RegisterActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
/**
 * Activity操作工具类
 * 
 * @Author zhw
 * @Version V1.0
 * @Createtime：2014年5月22日 下午1:55:56
 */
public class ActivityUtil {
	private static final String TAG = ActivityUtil.class.getCanonicalName();

	/**
	 * 根据Activity的名字获取该Activity对象
	 * 
	 * @param name
	 * @return
	 */
	public static Activity getActivityByName(String name) {
		for (Activity ac : MyApplication.activities) {
			if (ac.getClass().getName().indexOf(name) > 0) {
				return ac;
			}
		}
		return null;
	}

	/**
	 * 关闭登录模块的界面
	 */
	public static void finishLoginPages() {
		GuideActivity ga = (GuideActivity) getActivityByName("GuideActivity");
		if (ga != null) {
			ga.finish();
			Logger.d(TAG, "finishLoginPages finish  -> GuideActivity");
		}

		LoginActivity la = (LoginActivity) getActivityByName("LoginActivity");
		if (la != null) {
			la.finish();
			Logger.d(TAG, "finishLoginPages finish  -> LoginActivity");
		}

		RegisterActivity ra = (RegisterActivity) getActivityByName("RegisterActivity");
		if (ra != null) {
			ra.finish();
			Logger.d(TAG, "finishLoginPages finish  -> RegisterActivity");
		}


	}

	/**
	 * 关闭MainTab页面
	 */
	public static void finishMainTabPages() {
		MainTabActivity mta = (MainTabActivity) getActivityByName("MainTabActivity");
		if (mta != null) {
			mta.finish();
			Logger.d(TAG, "finishMainTabPages finish  -> MainTabActivity");
		}
	}

	/**
	 * 关闭回拨过度页面
	 */
	public static void finishCallBackDisplayPages() {
		CallbackDisplayActivity mta = (CallbackDisplayActivity) getActivityByName("CallbackDisplayActivity");
		if (mta != null) {
			mta.finish();
			Logger.d(TAG,
					"finishCallBackPages finish  -> CallbackDisplayActivity");
		}
	}

	/**
	 * 关闭所有activity(清空activity栈)
	 */
	public static void finishAllActivity() {
		for (Activity ac : MyApplication.activities) {
			if (ac != null) {
				ac.finish();
			}
		}
	}

	/**
	 * 将activity转移到后台运行
	 */
	public static void moveAllActivityToBack() {
		for (Activity ac : MyApplication.activities) {
			if (ac != null) {
				ac.moveTaskToBack(true);
			}
		}
	}

	public String language(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getCountry();
		return language;
	}
	/**
	 * 设置状态栏字体图标为深色，需要MIUIV6以上
	 * @param window 需要设置的窗口
	 * @param dark 是否把状态栏字体及图标颜色设置为深色
	 * @return  boolean 成功执行返回true
	 *
	 */
	public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
		boolean result = false;
		if (window != null) {
			Class clazz = window.getClass();
			try {
				int darkModeFlag = 0;
				Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
				Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
				darkModeFlag = field.getInt(layoutParams);
				Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
				if(dark){
					extraFlagField.invoke(window,darkModeFlag,darkModeFlag);//状态栏透明且黑色字体
				}else{
					extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
				}
				result=true;
			}catch (Exception e){

			}
		}
		return result;
	}


	/**
	 * 设置状态栏图标为深色和魅族特定的文字风格
	 * 可以用来判断是否为Flyme用户
	 * @param window 需要设置的窗口
	 * @param dark 是否把状态栏字体及图标颜色设置为深色
	 * @return  boolean 成功执行返回true
	 *
	 */
	public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
		boolean result = false;
		if (window != null) {
			try {
				WindowManager.LayoutParams lp = window.getAttributes();
				Field darkFlag = WindowManager.LayoutParams.class
						.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
				Field meizuFlags = WindowManager.LayoutParams.class
						.getDeclaredField("meizuFlags");
				darkFlag.setAccessible(true);
				meizuFlags.setAccessible(true);
				int bit = darkFlag.getInt(null);
				int value = meizuFlags.getInt(lp);
				if (dark) {
					value |= bit;
				} else {
					value &= ~bit;
				}
				meizuFlags.setInt(lp, value);
				window.setAttributes(lp);
				result = true;
			} catch (Exception e) {

			}
		}
		return result;
	}
	public static boolean isAppForeground(Context context){
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
		if (runningAppProcessInfoList==null){
			return false;
		}
		for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfoList) {
			if (processInfo.processName.equals(context.getPackageName()) &&
					processInfo.importance==ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
				return true;
			}
		}
		return false;
	}

}
