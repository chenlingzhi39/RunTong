package com.callba.phone.activity.more;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.bean.UserDao;
import com.callba.phone.cfg.Constant;
import com.callba.phone.service.MainService;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.view.MyProgressDialog;
@ActivityFragmentInject(
		contentViewId = R.layout.more_retrievepass,
		toolbarTitle = R.string.find_password,
		navigationId = R.drawable.press_back
)
public class RetrievePasswordActivity extends BaseActivity implements OnClickListener {

	private Button  bn_submit;
	private EditText et_phoneNum;
	
	private MyProgressDialog progressDialog;
	UserDao userDao;
	private String language="";
	private final MyHandler mHandler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<RetrievePasswordActivity> mActivity;
        private TimeCount time;
		public MyHandler(RetrievePasswordActivity activity) {
			mActivity = new WeakReference<RetrievePasswordActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			RetrievePasswordActivity activity = mActivity.get();
			if (activity != null) {
				// ...
				switch (msg.what){
					case Interfaces.GET_KEY_START:
						activity.bn_submit.setClickable(false);
						break;
					case Interfaces.GET_KEY_FAILURE:
						activity.bn_submit.setClickable(true);
						break;
					case Interfaces.GET_KEY_SUCCESS:
						activity.toast((String)msg.obj);
						break;
					case Interfaces.GET_CODE_START:
						activity.toast("发送短信请求");
						break;
					case Interfaces.GET_CODE_FAILURE:
						activity.toast((String)msg.obj);
						activity.bn_submit.setClickable(true);
						break;
					case Interfaces.GET_CODE_SUCCESS:
						activity.toast((String)msg.obj);
						time =new TimeCount(60000, 1000);
						time.start();
						break;

				}
			}}
		class TimeCount extends CountDownTimer {
			RetrievePasswordActivity activity;
			public TimeCount(long millisInFuture, long countDownInterval) {
				super(millisInFuture, countDownInterval);
				activity = mActivity.get();
			}

			@Override
			public void onFinish() {// 计时完毕
				activity.bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.orange));
				activity.bn_submit.setText(activity.getString(R.string.send_yzm));
				activity.bn_submit.setClickable(true);
			}

			@Override
			public void onTick(long millisUntilFinished) {// 计时过程
				activity.bn_submit.setClickable(false);//防止重复点击
				activity.bn_submit.setBackgroundColor(activity.getResources().getColor(R.color.light_black));
				activity.bn_submit.setText(millisUntilFinished / 1000 + "秒后重新发送");
			}
		}

	}
	public void init() {
		Locale locale = getResources().getConfiguration().locale;
		 language = locale.getCountry();
		bn_submit = (Button) this.findViewById(R.id.bn_retrieve_pass);
		bn_submit.setOnClickListener(this);
		
		et_phoneNum = (EditText) this.findViewById(R.id.et_retrpass_phone);
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
					String[] content = result.split("\\|");
					if("0".equals(content[0])) {
						String smsCodeKey = content[1];	//get_sms_key 返回的加密密钥
						String verificaSign = content[2];	//获取验证码 附加参数 key
						
						//发送获取短信验证码请求
						retrievePwd(smsCodeKey, verificaSign);
					}else {
						if (progressDialog!=null&&progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						toast(content[1]);
						/*CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);*/
					}
				} catch (Exception e) {
					if (progressDialog!=null&&progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					toast(getString(R.string.getserverdata_exception));
					/*CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);*/
					e.printStackTrace();
				}
			}else {
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				toast(getString(R.string.rpwd_hqdxmsb));
				/*CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.rpwd_hqdxmsb);*/
			}
		}else if(msg.what == Task.TASK_RETRIEVE_PWD){
			//找回密码
			if (progressDialog!=null&&progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			
			if(msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					String[] content = result.split("\\|");
					if("0".equals(content[0])) {
						/*CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);*/
						toast(content[1]);
						finish();
					}else {
						/*CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);*/
						toast(content[1]);
					}
				} catch (Exception e) {
					/*CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);*/
					toast(getString(R.string.getserverdata_exception));
					e.printStackTrace();
				}
			}else {
				/*CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.rpwd_zhmmsb);*/
				toast(getString(R.string.rpwd_zhmmsb));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		userDao=new UserDao(this,mHandler);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_retrieve_pass:
			InputMethodManager imm = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(bn_submit.getWindowToken(), 0);
			String num=et_phoneNum.getText().toString().trim();
			if (num.equals("")) {
				toast( "手机号不能为空!");
				return;
			}
			if ( num.length()>10) {
			/*	String address = NumberAddressService.getAddress(
						num, Constant.DB_PATH,
						RetrievePasswordActivity.this);
				if(!address.equals(""))
				{*/
					userDao.getFindKey(num);
				/*}else {toast("请输入正确的手机号!");
					return;
				}*/}else {toast("请输入正确的手机号!");
				return;
			}

			//getSMSCode();
			break;
			
		default:
			break;
		}
	}

	/**
	 * 获取短信key
	 */
	private void getSMSCode() {
		String phoneNumber = et_phoneNum.getText().toString().trim();
		
		if("".equals(phoneNumber) || phoneNumber.length() < 1) {
			toast(getString(R.string.input_phonenum));
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_phonenum);*/
			return;
		}
		if(phoneNumber.length() < 11) {
			toast(getString(R.string.wrong_phonenum));
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.wrong_phonenum);*/
			return;
		}
		
		progressDialog = new MyProgressDialog(this, getString(R.string.rpwd_zzzhmm));
		progressDialog.show();
		
		Task task = new Task(Task.TASK_GET_SMS_KEY);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("phoneNum", phoneNumber);
		taskParams.put("fromPage", "RetrievePasswordActivity");
		taskParams.put("lan",language);
		task.setTaskParams(taskParams);
		MainService.newTask(task);
	}
	/**
	 * 找回密码
	 * @param smsCodeKey
	 * @param sign
	 */
	private void retrievePwd(String smsCodeKey, String sign) {
		String phoneNumber = et_phoneNum.getText().toString().trim();
		String encodePhoneNum = "";
		try {
			encodePhoneNum = DesUtil.encrypt(phoneNumber, smsCodeKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Task task = new Task(Task.TASK_RETRIEVE_PWD);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("phoneNuber", encodePhoneNum);
		taskParams.put("sign", sign);
		taskParams.put("lan",language);

		task.setTaskParams(taskParams);
		MainService.newTask(task);
	}
}
