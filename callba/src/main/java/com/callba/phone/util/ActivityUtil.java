package com.callba.phone.util;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;

import com.callba.phone.MyApplication;
import com.callba.phone.activity.GuideActivity;
import com.callba.phone.activity.MainTabActivity;
import com.callba.phone.activity.calling.CallbackDisplayActivity;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.activity.login.OnekeyRegisterAcitvity;
import com.callba.phone.activity.login.RegisterActivity;
import com.callba.phone.activity.more.RetrievePasswordActivity;

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

		OnekeyRegisterAcitvity ora = (OnekeyRegisterAcitvity) getActivityByName("OnekeyRegisterAcitvity");
		if (ora != null) {
			ora.finish();
			Logger.d(TAG, "finishLoginPages finish  -> OnekeyRegisterAcitvity");
		}

		RetrievePasswordActivity rpa = (RetrievePasswordActivity) getActivityByName("RetrievePasswordActivity");
		if (rpa != null) {
			rpa.finish();
			Logger.d(TAG,
					"finishLoginPages finish  -> RetrievePasswordActivity");
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
}
