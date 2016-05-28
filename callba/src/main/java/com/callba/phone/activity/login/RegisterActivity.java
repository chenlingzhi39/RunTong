package com.callba.phone.activity.login;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.MainTabActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.service.MainService;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.umeng.socialize.utils.Log;


@ActivityFragmentInject(
		contentViewId = R.layout.manual_register,
		toolbarTitle=R.string.register,
		menuId=R.menu.menu_register,
		navigationId=R.drawable.press_cancel
)
public class RegisterActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_ok_register;
	private Button  bn_register, bn_login;
	private ProgressDialog progressDialog;
	private EditText et_account, et_verification, et_password,et_yzm,et_confirm;
	private TextView send_yzm;
	private InputMethodManager imm;
	private String username;	//获取验证码的手机号
	private String password ;
	private String code;
	private String language="";
	private SharedPreferenceUtil preferenceUtil;
	String[] results;
	UserDao userDao;
	String key;

	private static class MyHandler extends Handler {
		private final WeakReference<RegisterActivity> mActivity;
		static TimeCount time;
		public MyHandler(RegisterActivity activity) {
			mActivity = new WeakReference<RegisterActivity>(activity);

		}

		@Override
		public void handleMessage(Message msg) {
			RegisterActivity activity = mActivity.get();
			if (activity != null) {
				// ...
				switch (msg.what){
					case Interfaces.GET_KEY_START:
						activity.bn_register.setClickable(false);
						break;
					case Interfaces.GET_KEY_SUCCESS:
						activity.key=(String)msg.obj;
						break;
					case Interfaces.GET_KEY_FAILURE:
						activity.bn_register.setClickable(true);
						activity.toast((String)msg.obj);
						break;
					case Interfaces.GET_CODE_START:
						activity.toast("发送验证码请求");
						break;
					case Interfaces.GET_CODE_SUCCESS:
						//activity.et_yzm.setText((String)msg.obj);
						activity.toast((String)msg.obj);
						time =new TimeCount(60000, 1000);
						time.start();
						activity.bn_register.setClickable(true);
						break;
					case Interfaces.GET_CODE_FAILURE:
						activity.toast((String)msg.obj);
						activity.bn_register.setClickable(true);
					    break;

				}
			}
		}
		class TimeCount extends CountDownTimer {
			RegisterActivity activity;
			public TimeCount(long millisInFuture, long countDownInterval) {
				super(millisInFuture, countDownInterval);
				activity = mActivity.get();
			}

			@Override
			public void onFinish() {// 计时完毕
				activity.send_yzm.setTextColor(activity.getResources().getColor(R.color.orange));
				activity.send_yzm.setText(activity.getString(R.string.send_yzm));
				activity.send_yzm.setClickable(true);
			}

			@Override
			public void onTick(long millisUntilFinished) {// 计时过程
				activity.send_yzm.setClickable(false);//防止重复点击
				activity.send_yzm.setTextColor(activity.getResources().getColor(R.color.light_black));
				activity.send_yzm.setText(millisUntilFinished / 1000 + "秒后重新发送");
			}
		}
	}



	private final MyHandler mHandler = new MyHandler(this);


	@Override
	public void init() {
		userDao=new UserDao(this,mHandler);
		preferenceUtil=SharedPreferenceUtil.getInstance(this);
		Locale locale = getResources().getConfiguration().locale;
		 language = locale.getCountry();
		et_confirm=(EditText) this.findViewById(R.id.confirm_password);
		bn_back = (Button) this.findViewById(R.id.bn_mre_back);
		bn_ok_register = (Button) this.findViewById(R.id.bn_mre_yijianzhuce);
		bn_back.setOnClickListener(this);
		bn_ok_register.setOnClickListener(this);
		et_yzm=(EditText)findViewById(R.id.et_yzm);
        send_yzm=(TextView) findViewById(R.id.send_yzm);
		send_yzm.setOnClickListener(this);
		bn_register = (Button) this.findViewById(R.id.bn_mre_register);
		bn_register.setOnClickListener(this);

		et_account = (EditText) this.findViewById(R.id.et_mre_account);
		if(!"".equals(CalldaGlobalConfig.getInstance().getUsername())){
			et_account.setText(CalldaGlobalConfig.getInstance().getUsername());
		}
		et_verification = (EditText) this.findViewById(R.id.et_mre_yzm);
		et_password = (EditText) this.findViewById(R.id.et_mre_password);
		
		imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.send_yzm:

				final String searchNumber = et_account.getText().toString().trim();
				if ( searchNumber.length()>10) {
				/*	String address = NumberAddressService.getAddress(
							searchNumber, Constant.DB_PATH,
							RegisterActivity.this);
				if(!address.equals(""))
				{ */userDao.getRegisterKey(searchNumber);
				/*}
					else
				toast("请输入正确的手机号!");*/
				}else
				toast("请输入正确的手机号!");

				break;
		case R.id.bn_mre_back:
			this.finish();
			break;

		case R.id.bn_mre_yijianzhuce:
			Intent intent = new Intent(this, OnekeyRegisterAcitvity.class);
			this.startActivity(intent);
			this.finish();
			break;
		
		case R.id.bn_mre_register:
			//隐藏键盘
			imm.hideSoftInputFromWindow(bn_register.getWindowToken(), 0);
			
			 username = et_account.getText().toString().trim();
			 password = et_password.getText().toString().trim();
			 code = et_yzm.getText().toString().trim();
			if (et_account.getText().toString().equals("")) {
				toast( "手机号不能为空!");
				break;
			}
			if (et_password.getText().toString().equals("")) {
				toast("密码不能为空!");
				break;
			}
			if(code.equals("")){
				toast("验证码不能为空!");
				break;
			}
			if (et_confirm.getText().toString().equals("")) {
				toast("请再输入一遍密码!");
				break;
			}
			if (!et_password.getText().toString().equals(et_confirm.getText().toString())) {
				toast( "密码不匹配!");
				break;
			}
			boolean isinputOK = verification(username, password, code);

			if(!isinputOK) return;
			if ( username.length()>10) {
			/*	String address = NumberAddressService.getAddress(
						username, Constant.DB_PATH,
						RegisterActivity.this);
				if(!address.equals(""))
				{*/
					try {
						Log.i("register","click");
						Log.i("code",code);
                        Log.i("key",key);
						sendRegisterRequest(username, password,DesUtil.encrypt(code,key));
					}catch(Exception e){}
				/*}else {toast("请输入正确的手机号!");
				break;
			    }*/}else {toast("请输入正确的手机号!");
				break;
				}

			
			/*progressDialog = new MyProgressDialog(this, getString(R.string.registering));
			progressDialog.show();*/


		    /*try {
				userDao.register(username,password,DesUtil.encrypt(code,key));
			}catch(Exception e){

		   }*/
			break;

			
		default:
			break;
		}
	}

	/**
	 * 获取短信key
	 */
	private void getVerifiCode() {
		username = et_account.getText().toString().trim();
		if("".equals(username) || username.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_phonenum);
			return;
		}
		if(username.length() < 11) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.wrong_phonenum);
			return;
		}
		
		progressDialog = new MyProgressDialog(this, getString(R.string.getting_code));
		progressDialog.show();
		
		Task task = new Task(Task.TASK_GET_SMS_KEY);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("phoneNum", username);
		taskParams.put("fromPage", "RegisterActivity");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		MainService.newTask(task);
	}

	/**
	 * 注册之前校验
	 * @param username
	 * @param password
	 * @param rePass
	 * @return 校验通过返回 true 反之 false
	 */
	private boolean verification(String username, String password, String verifiCode) {
		if("".equals(username) || username.length() < 1) {
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_phonenum);*/
			toast(getString(R.string.input_phonenum));
			return false;
		}
		if("".equals(password) || password.length() < 1) {
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_pwd);*/
			toast(getString(R.string.input_pwd));
			return false;
		}
		/*if("".equals(verifiCode) || verifiCode.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_yzm);
			return false;
		}*/
		//验证密码是否规范 6~16位的字母、数字或下划线
		Pattern p = Pattern.compile("\\w{6,16}");
		Matcher m = p.matcher(password);
		if(!m.matches()) {
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.pwd_type);*/
			toast(getString(R.string.pwd_type));
			return false;
		}
		return true;
	}

	/**
	 * 发送注册任务
	 * @param username
	 * @param password
	 * @param verifiCode
	 */
	private void sendRegisterRequest(String username, String password,
			String code) {
		
		Task task = new Task(Task.TASK_REGISTER);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("phoneNumber", username);
		taskParams.put("password", password);
		taskParams.put("code", code);
		taskParams.put("softType", "android");
		taskParams.put("countryCode","86");
		task.setTaskParams(taskParams);
		Logger.i("发送注册", taskParams.toString());
		MainService.newTask(task);
	}
	
	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];
		if(msg.what == Task.TASK_GET_SMS_KEY) {
			//获取短信key
			if(msg.arg1 == Task.TASK_SUCCESS) {
				try {
					Bundle bundle = (Bundle) msg.obj;
					String result = bundle.getString("result");
					Logger.v("获取短信key", result);
					String[] content = result.split("\\|");
					if("0".equals(content[0])) {
						String smsCodeKey = content[1];	//get_sms_key 返回的加密密钥
						String verificaSign = content[2];	//获取验证码 附加参数 key
						
						//发送获取短信验证码请求
						sendGetVifricaRequst(smsCodeKey, verificaSign);
					}else {
						if (progressDialog!=null&&progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);
					}
				} catch (Exception e) {
					if (progressDialog!=null&&progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.sin_hqsjsb);
					
					e.printStackTrace();
				}
			}else {
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.sin_hqsjsb);
			}
		}else if(msg.what == Task.TASK_GET_VERFICA_CODE) {
			//获取验证码
			if(msg.arg1 == Task.TASK_SUCCESS) {
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				String result = (String) msg.obj;
				Logger.v("获取验证码", result);
				String[] content = result.split("\\|");
				
				if("0".equals(content[0])) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.receive_yzm);
				}else {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), content[1]);
				}
			}else {
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getyzm_failed);
			}
		}else if(msg.what == Task.TASK_REGISTER) {
			if (progressDialog!=null&&progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			//注册
			if(msg.arg1 == Task.TASK_SUCCESS) {
				String result = (String) msg.obj;
				Logger.i("手动注册发送短信", result);
				String[] content = result.split("\\|");
				if("0".equals(content[0])) {
					toast(content[1]);
					preferenceUtil.putString(Constant.LOGIN_USERNAME, username);
					preferenceUtil.putString(Constant.LOGIN_PASSWORD, password);
					preferenceUtil.commit();
					Intent intent = new Intent(this, MainTabActivity.class);
					LoginController.getInstance().setUserLoginState(false);
					CalldaGlobalConfig.getInstance().setAutoLogin(true);
					intent.putExtra("isLogin", false);
					new Thread(new Runnable() {
						@Override
						public void run() {
							try{
								EMClient.getInstance().createAccount(username,password); }catch(HyphenateException e){
								e.printStackTrace();
							}
						}
					}).start();

					startActivity(intent);
				}else {
					/*CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), content[1]);*/
					toast(content[1]);
				}
			}else {
				/*CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.register_failed);*/
				toast(getString(R.string.register_failed));
			}
		}
	}

	/**
	 * 发送 获取短信验证码 任务
	 */
	private void sendGetVifricaRequst(String smsCodeKey, String verificaSign) {
		try {
			//发送短信之前,获取密钥
			String encryptedPhoneNum = DesUtil.encrypt(username, smsCodeKey);
			
			Task task = new Task(Task.TASK_GET_VERFICA_CODE);
			Map<String, Object> taskParams = new HashMap<String, Object>();
			taskParams.put("encryptedPhoneNum", encryptedPhoneNum);
			taskParams.put("verificaSign", verificaSign);
			taskParams.put("lan", language);
			task.setTaskParams(taskParams);
			Logger.i("获取密钥", taskParams.toString());
			MainService.newTask(task);
		} catch (Exception e) {
			if (progressDialog!=null&&progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.getyzm_failed);
			
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.login:
				Intent intentLogin = new Intent(this, LoginActivity.class);
				startActivity(intentLogin);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
