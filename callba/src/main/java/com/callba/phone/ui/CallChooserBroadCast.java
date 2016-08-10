package com.callba.phone.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;

import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;

/** 
 * 拦截呼叫电话广播
 * @Author  zhw
 * @Version V1.0  
 * @Createtime：2014年5月26日 下午5:24:45 
 */
public class CallChooserBroadCast extends BroadcastReceiver {
	private static final String TAG = CallChooserBroadCast.class.getCanonicalName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.d(TAG, "CallChooserBroadCast receive new_outgoing_call broadcast.");
		String phoneNumber = getResultData();
		
		if(PhoneNumberUtils.isEmergencyNumber(phoneNumber)) {
			setResultData(phoneNumber);
			return;
			
		} else if(System.currentTimeMillis() - 
				GlobalConfig.getInstance().getLastInterceptCallTime() < 5000) {
			setResultData(phoneNumber);
			return;
			
		} else {
			Logger.d(TAG, "CallChooserBroadCast start CallChooserActivity");

			SharedPreferenceUtil mPreferenceUtil = SharedPreferenceUtil.getInstance(context);
			boolean isMonitorDialer = mPreferenceUtil.getBoolean(Constant.SYSTEM_DIAL_SETTING, false);
			
			if(isMonitorDialer) {
				Intent intent1 = new Intent(context, CallChooserActivity.class);
				intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent1.putExtra("phoneNumber", phoneNumber);
				context.startActivity(intent1);
				
				setResultData(null);
				abortBroadcast();
			} else {
				setResultData(phoneNumber);
				return;
			}
			
		}
	}
}
 