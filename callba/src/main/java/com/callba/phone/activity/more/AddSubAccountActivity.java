package com.callba.phone.activity.more;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

public class AddSubAccountActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_yzm, bn_register, bn_sendVoice;
	private EditText et_phoneNo, et_yzm, et_password, et_ghNum;
	
	private MyProgressDialog progressDialog;
	
	private static final String SUB_ACCOUNT_PHONE = "1";
	private static final String SUB_ACCOUNT_GH = "2";
	private String language="";
	@Override
	public void init() {
		Locale locale = getResources().getConfiguration().locale;
		 language = locale.getCountry();
		bn_back = (Button) findViewById(R.id.bn_addsubaccount_back);
		bn_yzm = (Button) findViewById(R.id.bn_addaccount_yzm);
		bn_register = (Button) findViewById(R.id.bn_addaccount_register);
		bn_sendVoice = (Button) findViewById(R.id.bn_addaccount_fsyy);
		bn_back.setOnClickListener(this);
		bn_yzm.setOnClickListener(this);
		bn_register.setOnClickListener(this);
		bn_sendVoice.setOnClickListener(this);
		
		et_phoneNo = (EditText) findViewById(R.id.et_addaccount_name);
		et_yzm = (EditText) findViewById(R.id.et_addaccount_yzm);
		et_password = (EditText) findViewById(R.id.et_addaccount_password);
		et_ghNum = (EditText) findViewById(R.id.et_addaccount_ghname);
	}

	@Override
	public void refresh(Object... params) {
		if (progressDialog!=null&&progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		Message msg = (Message) params[0];
		
		if(msg.what == Task.TASK_GET_SUBACCOUNT_YZM) {
			//获取验证码
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), result[1]);
				} catch (Exception e) {
					e.printStackTrace();
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
		}else if(msg.what == Task.TASK_ADD_SUBACCOUNT) {
			//添加手机子账户
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
						
						setResult(RESULT_OK);
						finish();
					} else if ("1".equals(result[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
		}else if(msg.what == Task.TASK_ADD_SUBACCOUNT_GH) {
			//添加固话子账户
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
						
						finish();
					} else if ("1".equals(result[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_addsubaccount);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_addsubaccount_back:
			finish();
			break;
		
		case R.id.bn_addaccount_yzm:
			InputMethodManager imm = (InputMethodManager) this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(bn_yzm.getWindowToken(), 0);
			
			getVerificationCode();
			break;
			
		case R.id.bn_addaccount_register:
			InputMethodManager imm1 = (InputMethodManager) this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm1.hideSoftInputFromWindow(bn_register.getWindowToken(), 0);
			
			addPhoneSubAccount();
			break;
			
		case R.id.bn_addaccount_fsyy:
			InputMethodManager imm2 = (InputMethodManager) this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm2.hideSoftInputFromWindow(bn_sendVoice.getWindowToken(), 0);
			
			addGHSubAccount();
			break;
		default:
			break;
		}
	}

	/**
	 * 添加固话子账户
	 */
	private void addGHSubAccount() {
		String ghNum = et_ghNum.getText().toString().trim();
		if("".equals(ghNum) || ghNum.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_telphonenum);
			return;
		}
		
		progressDialog = new MyProgressDialog(this, getString(R.string.sending_yyyz));
		progressDialog.show();
		
		Task task = new Task(Task.TASK_ADD_SUBACCOUNT_GH);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("childPhoneNumber", ghNum);
		taskParams.put("softType", "android");
		taskParams.put("type", SUB_ACCOUNT_GH);
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		
		MainService.newTask(task);
	}

	/**
	 * 添加手机子账户
	 * @param subAccountType (1:手机号码 2:固话号码)
	 */
	private void addPhoneSubAccount() {
		String phoneNum = et_phoneNo.getText().toString().trim();
		if("".equals(phoneNum) || phoneNum.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_phonenum);
			return;
		}
		String veficationCode = et_yzm.getText().toString().trim();
		if("".equals(veficationCode) || veficationCode.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_yzm);
			return;
		}
		String password = et_password.getText().toString().trim();
		if("".equals(password) || password.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_pwd);
			return;
		}
		//验证密码是否规范 6~16位的字母、数字或下划线
		Pattern p = Pattern.compile("\\w{6,16}");
		Matcher m = p.matcher(password);
		if(!m.matches()) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.pwd_type);
			return;
		}
		
		progressDialog = new MyProgressDialog(this, getString(R.string.adding_subaccount));
		progressDialog.show();
		
		Task task = new Task(Task.TASK_ADD_SUBACCOUNT);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("childPhoneNumber", phoneNum);
		taskParams.put("softType", "android");
		taskParams.put("childPwd", password);
		taskParams.put("childCode", veficationCode);
		taskParams.put("type", SUB_ACCOUNT_PHONE);
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		
		MainService.newTask(task);
	}

	/**
	 * 获取验证码
	 */
	private void getVerificationCode() {
		String phoneNum = et_phoneNo.getText().toString().trim();
		if("".equals(phoneNum) || phoneNum.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_phonenum);
			return;
		}
		
		progressDialog = new MyProgressDialog(this, getString(R.string.getting_code));
		progressDialog.show();
		
		Task task = new Task(Task.TASK_GET_SUBACCOUNT_YZM);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("childPhoneNumber", phoneNum);
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		
		MainService.newTask(task);
	}
}
