package com.callba.phone.activity;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.service.AutoAnswerReceiver;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.NetworkDetector;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

/** 
 * 类说明 
 * @author  zhwei
 * @version V1.0  创建时间：2013-10-28 下午4:20:32 
 */
public class SendCallActivity extends BaseActivity implements OnClickListener{
	private LinearLayout ll_hangup;
	
	private String phoneNum;

	public void init() {
		ll_hangup = (LinearLayout) findViewById(R.id.callover);
		ll_hangup.setOnClickListener(this);
		
		phoneNum = PhoneNumberUtils.getNumberFromIntent(getIntent(), this);
		if("".equals(phoneNum)) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(this, R.string.emptycallnum);
			
			finish();
		}else {
			dialCallback();
//			callUtils.judgeCallMode(this, phoneNum, quhao);
		}
	}

	@Override
	public void refresh(Object... params) {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.sendcall);
		init();
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.callover:
			//挂断
			this.finish();
			break;

		default:
			break;
		}
	}
	
	/**
	 * 回拨
	 */
	private void dialCallback() {
		final String mUsername;
		final String mPassword;
		
		if("".equals(CalldaGlobalConfig.getInstance().getUsername())) {
			SharedPreferences sharedPreferences = this.getSharedPreferences(Constant.PACKAGE_NAME, MODE_PRIVATE);
			mUsername = sharedPreferences.getString("username", "");
			mPassword = sharedPreferences.getString("desPassword", "");
		}else {
			mUsername = CalldaGlobalConfig.getInstance().getUsername();
			mPassword = CalldaGlobalConfig.getInstance().getPassword();
		}
		
		final MyProgressDialog progressDialog = new MyProgressDialog(this, getString(R.string.backcalling));
		progressDialog.show();
		
		if(CalldaGlobalConfig.getInstance().isCallBackAutoAnswer()) {
			//自动接听
			AutoAnswerReceiver.answerPhone(this);
		}
		
		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				CalldaToast calldaToast = new CalldaToast();
				
				if(msg.what == Task.TASK_SUCCESS) {
					try {
						String result = (String) msg.obj;
						String[] content = result.split("\\|");

						calldaToast.showToast(SendCallActivity.this, content[1]);
					} catch (Exception e) {
						calldaToast.showToast(SendCallActivity.this, R.string.server_error);
					}finally {
						finish();
					}
				}else if(msg.what == Task.TASK_NETWORK_ERROR) {
					calldaToast.showToast(SendCallActivity.this, R.string.network_error);
					finish();
				}else {
					calldaToast.showToast(SendCallActivity.this, R.string.unknownerror);
					finish();
				}
			}
		};
		
		new Thread() {
			public void run() {
				ActivityUtil activityUtil=new ActivityUtil();
				String lan=activityUtil.language(SendCallActivity.this);
				Map<String, String> params = new HashMap<String, String>();
				params.put("loginName", mUsername);
				params.put("loginPwd", mPassword);
				params.put("softType", "android");
				params.put("caller", CalldaGlobalConfig.getInstance().getUsername());
				params.put("callee", phoneNum);
				params.put("lan", lan);
				
				Message msg = mHandler.obtainMessage();
				
				try {
					if(NetworkDetector.detect(getApplicationContext())) {
						String result = HttpUtils.getDataFromHttpPost(Interfaces.DIAL_CALLBACK, params);
						msg.what = Task.TASK_SUCCESS;
						msg.obj = result.replace("\n", "").replace("\r", "");
					}else {
						msg.what = Task.TASK_NETWORK_ERROR;
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = Task.TASK_FAILED;
				}finally {
					mHandler.sendMessage(msg);
				}
			}
		}.start();
	}
}
