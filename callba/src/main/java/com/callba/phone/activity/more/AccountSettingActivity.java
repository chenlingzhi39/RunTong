package com.callba.phone.activity.more;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.logic.login.UserLoginErrorMsg;
import com.callba.phone.logic.login.UserLoginListener;
import com.callba.phone.service.MainService;
import com.callba.phone.util.DesUtil;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;
/**
 * 切换账号
 * @author Administrator
 *
 */
public class AccountSettingActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_ok, bn_switch;
	private EditText et_uname, et_upass;
	
	private MyProgressDialog progressDialog;
	
	private String username;	//切换登录名
	private String password;

	public void init() {
		bn_back = (Button) this.findViewById(R.id.bn_account_back);
		bn_ok = (Button) this.findViewById(R.id.bn_account_ok);
		bn_switch = (Button) this.findViewById(R.id.bn_account_switch);
		bn_back.setOnClickListener(this);
		bn_ok.setOnClickListener(this);
		bn_switch.setOnClickListener(this);
		
		et_uname = (EditText) this.findViewById(R.id.et_account_name);
		et_upass = (EditText) this.findViewById(R.id.et_account_password);
		
		if(CalldaGlobalConfig.getInstance().getUsername() != null) {
			et_uname.setText(CalldaGlobalConfig.getInstance().getUsername());
		}
	}

	@Override
	public void refresh(Object... params) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_account_setting);
		init();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_account_back:
			this.finish();
			break;

		case R.id.bn_account_ok:
			
			break;
			
		case R.id.bn_account_switch:
			checkAndSwitch();
			break;
		default:
			break;
		}
	}
	/**
	 * 切换账户
	 */
	private void checkAndSwitch() {
		username = et_uname.getText().toString().trim();
		password = et_upass.getText().toString().trim();

		if (TextUtils.isEmpty(username)) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_username);
			return;
		}
		if (TextUtils.isEmpty(password)) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_password);
			return;
		}
		
		progressDialog = new MyProgressDialog(this, getString(R.string.switching_account));
		progressDialog.show();
		
		// 加密，生成loginSign
		String source = username + "," + password;
		String sign = null;
		try {
			sign = DesUtil.encrypt(source, CalldaGlobalConfig.getInstance().getSecretKey());
		} catch (Exception e) {
			e.printStackTrace();
			
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.keywrong_failed);
			
			if (progressDialog!=null&&progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			return;
		}
		
		Task task = new Task(Task.TASK_LOGIN);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginSign", sign);
		taskParams.put("loginType", "0");
		task.setTaskParams(taskParams);

		//设置为未登录
		LoginController.getInstance().setUserLoginState(false);
		
		//登录
		LoginController.getInstance().userLogin(this, task, new UserLoginListener() {
			@Override
			public void serverLoginFailed(String info) {
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), info);
			}
			
			@Override
			public void loginSuccess(String[] resultInfo) {
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				//处理登录成功返回信息
				LoginController.parseLoginSuccessResult(AccountSettingActivity.this, username, password, resultInfo);
				
				//提示用户切换成功
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.switch_account_ok, Toast.LENGTH_LONG);
				
				//更新余额信息
				refreshBalance();
				
				//关闭当前页面
				finish();
			}
			
			@Override
			public void localLoginFailed(UserLoginErrorMsg errorMsg) {
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				//解析登录失败信息
				LoginController.parseLocalLoginFaildInfo(getApplicationContext(), errorMsg);
			}
		});
	}
	
	/**
	 * 刷新余额
	 * @author zhw
	 */
	private void refreshBalance() {
		String username = CalldaGlobalConfig.getInstance().getUsername();
		String password = CalldaGlobalConfig.getInstance().getPassword();
		
		if(TextUtils.isEmpty(username)
				|| TextUtils.isEmpty(password)) {
			return;
		}
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		 Task task = new Task(Task.TASK_GET_USER_BALANCE);
		 Map<String, Object> taskParams = new HashMap<String, Object>();
		 taskParams.put("loginName", username);
		 taskParams.put("loginPwd", password);
		 taskParams.put("softType", "android");
		 taskParams.put("lan", language);
		 task.setTaskParams(taskParams);
		 
		 MainService.newTask(task);
	}
}
