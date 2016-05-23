package com.callba.phone.activity.login;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.MainTabActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.logic.login.UserLoginErrorMsg;
import com.callba.phone.logic.login.UserLoginListener;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PhoneUtils;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.util.TimeFormatUtil;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

public class OnekeyRegisterAcitvity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = OnekeyRegisterAcitvity.class.getCanonicalName();
	
	private Button bn_back, bn_manual;
	private LinearLayout bn_register;
	private TextView tv_countdown;
	private String currPhoneNumber=""; // 本机号码
	private String password; // 密码
	private MyProgressDialog progressDialog;
	private SharedPreferenceUtil mPreferenceUtil;
	private Handler delayHandler;
	private String language;
	private RegisterCountDownTimer mRegisterCountDownTimer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.onekey_register);
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public void init() {
		bn_back = (Button) this.findViewById(R.id.bn_onekey_back);
		bn_manual = (Button) this.findViewById(R.id.bn_onekey_soudong);
		bn_register = (LinearLayout) this.findViewById(R.id.bn_onekey_register);
		tv_countdown = (TextView) this.findViewById(R.id.tv_countdown);
		
		bn_back.setOnClickListener(this);
		bn_manual.setOnClickListener(this);
		bn_register.setOnClickListener(this);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		currPhoneNumber = tm.getLine1Number();

		mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
		
		delayHandler = new Handler();
		
		
		delayHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(!TextUtils.isEmpty(currPhoneNumber)) {
					verifyRegisterUserExist(currPhoneNumber);
				}
			}
		}, 500);
		
		Locale locale = getResources().getConfiguration().locale;
		 language = locale.getCountry();
	}

	/**
	 * 验证当前号码是否已注册
	 * 
	 * @param phoneNumber
	 */
	private void verifyRegisterUserExist(String phoneNumber) {
		Task task = new Task(Task.TASK_VERIFY_USER_EXIST);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		
		String secretKey = mPreferenceUtil.getString(Constant.SECRET_KEY);
		if(TextUtils.isEmpty(secretKey)) {
			Logger.i(TAG, "secretKey为空");
			
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.getkey_error);
			return;
		}
 		
		if(phoneNumber.startsWith("86")) {
			phoneNumber = phoneNumber.substring(2);
		} else if(phoneNumber.startsWith("+86")) {
			phoneNumber = phoneNumber.substring(3);
		}
		
		String encryptedTime = "";
		try {
			encryptedTime = DesUtil.encrypt(phoneNumber+","+TimeFormatUtil.formatChineseTimeStr(), secretKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		taskParams.put("lan", language);
		taskParams.put("v", encryptedTime);
		taskParams.put("lan", language);
		task.setTaskParams(taskParams );
		
		MainService.newTask(task);
		
		if (progressDialog == null) {
			progressDialog = new MyProgressDialog(this, getString(R.string.onekey_verifynumber));
		}
		progressDialog.show();
		
		Logger.d(TAG, "send verifyRegisterUserExist TASK.");
	}

	@Override
	public void refresh(Object... params) {
		if(progressDialog != null&&progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		Message msg = (Message) params[0];
		
		if(msg.what == Task.TASK_VERIFY_USER_EXIST) {
			//验证用户是否存在
			
			try {
				String result = (String) msg.obj;
				
				result = result.replace("\t", "").replace("\n", "").replace("\r", "");

				Logger.d(TAG, "receive TASK_VERIFY_USER_EXIST result :　" + result);
				
				String[] content = result.split("\\|");
				
				if("0".equals(content[0])) {
					String message = content[1];
					if("autologin".equals(message)) {
						//可以自动登陆（密码为默认密码）
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), R.string.onekey_reg_checkuser_autologin);
						
						//登录
						tryLogion();
						
					} else if("manuallogin".equals(message)) {
						//手动登陆,跳转到登陆界面
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), R.string.onekey_reg_checkuser_manuallogin);

						Intent intent = new Intent();
						intent.setClass(OnekeyRegisterAcitvity.this, LoginActivity.class);
						startActivity(intent);
						
						finish();
					} else {
						//该用户未注册过
					}
				} else {
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_onekey_back:
			this.finish();
			break;

		case R.id.bn_onekey_soudong:
			Intent intent = new Intent(this, RegisterActivity.class);
			this.startActivity(intent);
			break;

		case R.id.bn_onekey_register:
			onkeyRegister();
			break;
			
		default:
			break;
		}
	}

	private void onkeyRegister() {
		if(progressDialog == null) {
			progressDialog = new MyProgressDialog(this,
					getString(R.string.onekey_registering));
		} else {
			progressDialog.setProgressMessage(getString(R.string.onekey_registering));
		}
		progressDialog.show();
		
		mRegisterCountDownTimer = new RegisterCountDownTimer(60*1000, 1000);
		mRegisterCountDownTimer.start();
		
		tv_countdown.setVisibility(View.VISIBLE);
		bn_register.setClickable(false);

		final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == -1) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), (String) msg.obj);
					if (progressDialog!=null&&progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					return;
				}
				
				try {
					sendLocalMobileMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 尝试登录
				tryLogion();

			}

			/**
			 * 调用手机短发送短消息
			 * @param msg
			 */
			private void sendLocalMobileMessage(Message msg) {
				Bundle bundle = (Bundle) msg.obj;
				String sendNum = bundle.getString("sendNum");
				String content = bundle.getString("msgContent");
				
				// 调用系统发送注册短信
				SmsManager manager = SmsManager.getDefault();
				ArrayList<String> list = manager.divideMessage(content); // 因为一条短信有字数限制，因此要将长短信拆分
				for (String text : list) {
					manager.sendTextMessage(sendNum, null, text, null, null);
				}
				if(progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				try {
					ContentValues values = new ContentValues();
					// 发送时间
					values.put("date", System.currentTimeMillis());
					// 阅读状态
					values.put("read", 0);
					// 1为收 2为发
					values.put("type", 2);
					// 送达号码
					values.put("address", sendNum);
					// 送达内容
					values.put("body", content);
					// 插入短信库
					getContentResolver().insert(Uri.parse("content://sms/sent"),
							values);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		new Thread() {
			public void run() {
				String result = "";
				Message msg = mHandler.obtainMessage();
				try {
					ActivityUtil activityUtil=new ActivityUtil();
					String lan=activityUtil.language(OnekeyRegisterAcitvity.this);
					result = HttpUtils
							.getDataFromHttpGet(Interfaces.OneKeyRegister+"&lan="+lan);
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = getString(R.string.onekey_reg_faild);
					mHandler.sendMessage(msg);
				}

				try {
					Logger.i("发送一键注册请求结果", result);
					String[] content = result.split("\\|");
					if ("0".equals(content[0])) {
						// 成功
						Bundle bundle = new Bundle();
						bundle.putString("sendNum", content[1]);
						bundle.putString("msgContent", content[2]);
						msg.obj = bundle;

						mHandler.sendMessage(msg);

					} else if ("1".equals(content[0])) {
						// 失败
						msg.what = -1;
						msg.obj = content[1];
						mHandler.sendMessage(msg);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					if(progressDialog != null&&progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					e.printStackTrace();
					msg.what = -1;
					msg.obj = getString(R.string.onekey_reg_faild);
					mHandler.sendMessage(msg);
				}
			}
		}.start();
	}

	/**
	 * 尝试自动登录
	 */
	protected void tryLogion() {
		if (TextUtils.isEmpty(currPhoneNumber) || currPhoneNumber.length() < 6) {
			Intent intent = new Intent();
			intent.setClass(OnekeyRegisterAcitvity.this, LoginActivity.class);
			startActivity(intent);
			finish();
			
			return;
		}
		login();
		
		if(progressDialog == null) {
			progressDialog = new MyProgressDialog(this,
					getString(R.string.logining));
		} else {
			progressDialog.setProgressMessage(getString(R.string.logining));
		}
		progressDialog.show();
	}

	BroadcastReceiver sendMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(Context c, Intent intent) {
			// 判断短信是否发送成功
			CalldaToast calldaToast = new CalldaToast();
			switch (getResultCode()) {
				case Activity.RESULT_OK:
					calldaToast.showToast(c, R.string.send_successful);
					break;
				default:
					calldaToast.showToast(c, R.string.send_failed);
					break;
			}
		}
	};

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 表示对方成功收到短信
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(context, R.string.send_successful_access);
			
			progressDialog = new MyProgressDialog(OnekeyRegisterAcitvity.this,
					getString(R.string.logining));
			progressDialog.show();
			
			delayHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					tryLogion();
				}
			}, 5000);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		getApplicationContext().registerReceiver(sendMessage,
				new IntentFilter(Constant.SENT_SMS_ACTION));
		getApplicationContext().registerReceiver(receiver,
				new IntentFilter(Constant.DELIVERED_SMS_ACTION));
	}

	protected void onDestroy() {
		super.onDestroy();
		//取消计时器
		if(mRegisterCountDownTimer != null) {
			mRegisterCountDownTimer.cancel();
		}
	}

	class RegisterCountDownTimer extends CountDownTimer {

		public RegisterCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@SuppressLint("SimpleDateFormat") 
		@Override
		public void onTick(long millisUntilFinished) {
			Date date = new Date(millisUntilFinished);
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
			String str = sdf.format(date);
			System.out.println(str);
			tv_countdown.setText("(" + millisUntilFinished / 1000 + ")");
		}

		@Override
		public void onFinish() {
			tv_countdown.setVisibility(View.GONE);
			bn_register.setClickable(true);
		}
	}

	/**
	 * 登录
	 */
	private void login() {
		currPhoneNumber = PhoneUtils.formatAvailLoginUser(currPhoneNumber);
		
		password = currPhoneNumber.substring(currPhoneNumber.length() - 6,
				currPhoneNumber.length());
		
		if (TextUtils.isEmpty(password)) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_password);
			return;
		}

		if ("".equals(CalldaGlobalConfig.getInstance().getSecretKey())) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.getkey_error);
			return;
		}
		
		Logger.d(TAG, "Auto login username is : " + currPhoneNumber);
		
		//加密，生成loginSign
		String source = currPhoneNumber + "," + password;
		String sign = null;
		try {
			sign = DesUtil.encrypt(source, CalldaGlobalConfig.getInstance().getSecretKey());
		} catch (Exception e) {
			e.printStackTrace();
			
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.key_exception);
			return;
		}

		Task task = new Task(Task.TASK_LOGIN);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("fromPage", "onekeyregisteracitvity");
		taskParams.put("loginSign", sign);
		taskParams.put("loginType", "0");
		task.setTaskParams(taskParams);

		//登录
		LoginController.getInstance().userLogin(this, task, new UserLoginListener() {
			@Override
			public void serverLoginFailed(String info) {
				if(progressDialog != null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), info);
			}
			
			@Override
			public void loginSuccess(String[] resultInfo) {
				if(progressDialog != null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				//处理登录成功返回信息
				LoginController.parseLoginSuccessResult(OnekeyRegisterAcitvity.this, currPhoneNumber, password, resultInfo);
				
				//转到主页面
				gotoMainActivity();
			}
			
			@Override
			public void localLoginFailed(UserLoginErrorMsg errorMsg) {
				if(progressDialog != null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				//解析登录失败信息
				LoginController.parseLocalLoginFaildInfo(getApplicationContext(), errorMsg);
			}
		});
	}

	/**
	 * 跳转到主页面
	 */
	private void gotoMainActivity() {
		if(mRegisterCountDownTimer != null) {
			mRegisterCountDownTimer.onFinish();
		}
		Intent intent = new Intent(OnekeyRegisterAcitvity.this,
				MainTabActivity.class);
		this.startActivity(intent);
	}
}
