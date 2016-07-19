package com.callba.phone.cfg;

import android.content.Context;

import com.callba.phone.util.SharedPreferenceUtil;

public class GlobalSetting {
	
	public static void initEnvirment(Context context) {
		SharedPreferenceUtil mSharedPreferenceUtil = SharedPreferenceUtil.getInstance(context);
		
		//回拨自动接听
		boolean isCallbackAutoAnswer = mSharedPreferenceUtil.getBoolean(Constant.BackCall_AutoAnswer, true);
		//自动登陆
		boolean isAutoLogin = mSharedPreferenceUtil.getBoolean(Constant.Auto_Login, false);
		//键盘音是否开启
		boolean iskeyboard = mSharedPreferenceUtil.getBoolean(Constant.KeyboardSetting, true);
		//拨打方式
		String callSetting = mSharedPreferenceUtil.getString(Constant.CALL_SETTING, Constant.CALL_SETTING_HUI_BO);
		
		GlobalConfig.getInstance().setCallBackAutoAnswer(isCallbackAutoAnswer);
		GlobalConfig.getInstance().setAutoLogin(isAutoLogin);
		GlobalConfig.getInstance().setKeyBoardSetting(iskeyboard);
		GlobalConfig.getInstance().setCallSetting(callSetting);
	}
}
