package com.callba.phone.activity.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.GuideActivity;
import com.callba.phone.activity.MainTabActivity;
import com.callba.phone.activity.more.RetrievePasswordActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.logic.login.UserLoginErrorMsg;
import com.callba.phone.logic.login.UserLoginListener;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.DesUtil;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CleanableEditText;

import java.util.HashMap;
import java.util.Map;

@ActivityFragmentInject(
		contentViewId = R.layout.login,
		toolbarTitle=R.string.login,
		menuId=R.menu.menu_login,
		navigationId=R.drawable.press_cancel
)
public class LoginActivity extends BaseActivity implements OnClickListener {
	private Button bn_login, bn_retrievePass;
	private EditText  et_password;
	private CleanableEditText et_username;
	private ProgressDialog progressDialog;

	private String username; // 登录的用户名
	private String password; // 密码
	private SharedPreferenceUtil mPreferenceUtil;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void init() {
		bn_login = (Button) this.findViewById(R.id.bn_login_login);
		bn_retrievePass = (Button) this
				.findViewById(R.id.bn_login_retrievePass);

		bn_login.setOnClickListener(this);
		bn_retrievePass.setOnClickListener(this);

		et_username = (CleanableEditText) this.findViewById(R.id.et_login_name);
		et_password = (EditText) this.findViewById(R.id.et_login_password);
		mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
		if(getIntent().getStringExtra("number")!=null)
		{et_username.setText(getIntent().getStringExtra("number"));
		   et_password.setText(getIntent().getStringExtra("password"));}
		username = mPreferenceUtil.getString(Constant.LOGIN_USERNAME);
		if (!"".equals(username)) {
			et_username.setText(username);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_login_login:
			InputMethodManager imm = (InputMethodManager) this
					.getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(bn_login.getWindowToken(), 0);

			login();
			mPreferenceUtil.putBoolean(Constant.IS_FROMGUIDE, false, true);
			
			break;

		case R.id.bn_login_retrievePass:
			// 找回密码
			Log.i("login","receivePass");
			Intent intent_pass = new Intent(this,
					RetrievePasswordActivity.class);
			startActivity(intent_pass);
			
			break;
		default:
			break;
		}
	}

	/**
	 * 登录
	 */
	private void login() {
		username = et_username.getText().toString().trim();
		password = et_password.getText().toString().trim();

		//校验用户名是否为空
		if (TextUtils.isEmpty(username)) {
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_username);*/
			toast(getString(R.string.input_username));
			return;
		}

		//校验密码是否为空
		if (TextUtils.isEmpty(password)) {
		/*	CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_password);*/
			toast(getString(R.string.input_password));
			return;
		}

		// 加密，生成loginSign
		String source = username + "," + password;
		String sign = null;
		try {
			sign = DesUtil.encrypt(source, CalldaGlobalConfig.getInstance().getSecretKey());
		} catch (Exception e) {
			e.printStackTrace();
			
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.key_exception);*/
			toast(getString(R.string.key_exception));
			return;
		}

		progressDialog = ProgressDialog.show(this,null,
				getString(R.string.logining));

		
		Task task = new Task(Task.TASK_LOGIN);
		Map<String, Object> taskParams = new HashMap<String, Object>();
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
				
				/*CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), info);*/
				toast(info);
			}
			
			@Override
			public void loginSuccess(String[] resultInfo) {
				if(progressDialog != null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				//处理登录成功返回信息
				LoginController.parseLoginSuccessResult(LoginActivity.this, username, password, resultInfo);
				
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
	
	@Override
	public void refresh(Object... params) {
	}

	/**
	 * 跳转到主页面
	 */
	private void gotoMainActivity() {
		Intent intent = new Intent(LoginActivity.this, MainTabActivity.class);
		this.startActivity(intent);
	}

	/**
	 * 重写onkeyDown 捕捉返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			GuideActivity la = (GuideActivity) ActivityUtil.getActivityByName("GuideActivity");
			if (la == null) {
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, GuideActivity.class);
				startActivity(intent);
			}
			finish();
			return true;
		}
		 return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.register:
				Intent intent = new Intent(this, RegisterActivity.class);
				startActivity(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
