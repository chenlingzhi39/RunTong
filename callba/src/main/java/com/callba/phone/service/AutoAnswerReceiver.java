package com.callba.phone.service;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;
import com.callba.phone.MyApplication;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.SPUtils;

public class AutoAnswerReceiver {
//	private final static String TAG = AutoAnswerReceiver.class.getCanonicalName();
	
	//超过该时长，如果未接听到来电，则取消自动接听
	private static final int LISTEN_INCOMING_CALL_LAST_TIME = 20 * 1000; 

	/**
	 * 开启监听来电自动接听操作
	 * @param context
	 */
	public static void answerPhone(final Context context) {
		final TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		
			
		final PhoneStateListener phoneStateListener = new PhoneStateListener() {
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					try {
						ActivityUtil.finishCallBackDisplayPages();
						if (!(boolean)SPUtils.get(context, Constant.SETTINGS,Constant.BackCall_AutoAnswer,true)) {
							//不自动接听来电
							return;
						}
						answerPhoneAidl(context);
					} catch (Exception e) {
						e.printStackTrace();
						answerPhoneHeadsethook(context);
					}
					ActivityUtil.finishCallBackDisplayPages();
					mTelephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
					break;
			/*	case TelephonyManager.CALL_STATE_IDLE:
					MyApplication.getInstance().setaBoolean(true);
					MyApplication.getInstance().startCount();
					break;*/
				default:
					break;
				}
			}
		};
		
		mTelephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		//延迟关闭
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				ActivityUtil.finishCallBackDisplayPages();
				mTelephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
			}
		}, LISTEN_INCOMING_CALL_LAST_TIME);
	}

	/**
	 * 使用低版本自动接听
	 * @param context
	 * @throws Exception
	 */
	private static void answerPhoneAidl(Context context) throws Exception {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Class<?> c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService;
		telephonyService = (ITelephony)m.invoke(tm);

		telephonyService.silenceRinger();
		telephonyService.answerRingingCall();
	}
	
	/**
	 * 使用高版本系统自动接听
	 * @param context
	 */
	private static void answerPhoneHeadsethook(Context context) {
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown,
				"android.permission.CALL_PRIVILEGED");

		// froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp,
				"android.permission.CALL_PRIVILEGED");
	}
}
