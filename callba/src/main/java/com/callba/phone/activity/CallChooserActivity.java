package com.callba.phone.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.callba.R;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.util.CallUtils;
import com.callba.phone.util.CallUtils.onCallModeDialogDismissListener;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CalldaToast;

/** 
 * 呼叫选择页面（拦截系统呼叫）
 * @author  zhw
 * @version V1.0
 * @createtime：2014年5月26日 下午4:18:28 
 */
public class CallChooserActivity extends Activity implements OnClickListener {
	private static final String TAG = CallChooserActivity.class.getCanonicalName();

	private LinearLayout llCallda, llPhone, llCancel;
	
	private String phoneNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.phonecall_chooser);
		super.onCreate(savedInstanceState);
		
		phoneNumber = getIntent().getStringExtra("phoneNumber");
		if(TextUtils.isEmpty(phoneNumber)) {
			Intent intent = getIntent();
			if(intent == null) {
				Logger.w(TAG, "CallChooserActivity getIntent returns null finish..");
				finish();
				return;
			}
			
			Logger.d(TAG, "CallChooserActivity Get phone number from Intent.");
			try {
				phoneNumber = PhoneNumberUtils.getNumberFromIntent(getIntent(), this);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(TextUtils.isEmpty(phoneNumber)) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.call_choose_num_null);
					
					finish();
					
					return;
				}
			}
			
			
			//直接通过软件拨打
			callByCallda();
		}
		
		init();
	}
	
	public void init() {
		llCallda = (LinearLayout) findViewById(R.id.ll_call_callda);
		llPhone  = (LinearLayout) findViewById(R.id.ll_call_phone);
		llCancel = (LinearLayout) findViewById(R.id.ll_call_cancel);
		
		llCallda.setOnClickListener(this);
		llPhone.setOnClickListener(this);
		llCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.ll_call_callda:
			callByCallda();
			break;
			
		case R.id.ll_call_phone:
			CalldaGlobalConfig.getInstance().setLastInterceptCallTime(System.currentTimeMillis());
			
			//手机拨打
			if(!TextUtils.isEmpty(phoneNumber)) {
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel://" + phoneNumber));
				startActivity(intent);
			}
			
			finish();
			break;
			
		case R.id.ll_call_cancel:
			//取消
			finish();
			break;
			
		default:
			break;
		}
	}

	/**
	 * 通过软件拨打
	 * @author zhw
	 */
	private void callByCallda() {
		CalldaGlobalConfig.getInstance().setLastInterceptCallTime(System.currentTimeMillis());
		
		//闰通电话
		boolean isUserLogin = LoginController.getInstance().getUserLoginState();
		Logger.i(TAG, "CallChooser get loginState is : " + isUserLogin);
		if(isUserLogin) {
			CallUtils callUtils = new CallUtils();
			callUtils.judgeCallMode(this, phoneNumber, new onCallModeDialogDismissListener() {
				@Override
				public void onDialogDismiss() {
					finish();
				}
			});
		} else {
			Intent intent = new Intent(this, WelcomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			
			SharedPreferenceUtil preferenceUtil = SharedPreferenceUtil.getInstance(this);
			preferenceUtil.putString(Constant.SYS_DIALER_CALLEE, phoneNumber);
			preferenceUtil.putLong(Constant.SYS_DIALER_CALLTIME, System.currentTimeMillis());
			preferenceUtil.commit();
			
			finish();
		}
	}
}
 