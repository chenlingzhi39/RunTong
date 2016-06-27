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
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.DesUtil;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

public class ChangePasswordActivity extends BaseActivity implements OnClickListener {
	private TextView tv_account;
	private TextView tv_account_all;
	private Button bn_back, bn_ok;
	private EditText et_old, et_new, et_confirm;
	
	private MyProgressDialog progressDialog;
	
	private String confrimPass;	//新密码

	public void init() {
		bn_back = (Button) this.findViewById(R.id.bn_changepass_back);
		bn_ok = (Button) this.findViewById(R.id.bn_changepass_ok);
		bn_back.setOnClickListener(this);
		bn_ok.setOnClickListener(this);
		
		et_old = (EditText) this.findViewById(R.id.et_changepass_old);
		et_new = (EditText) this.findViewById(R.id.et_changepass_new);
		et_confirm = (EditText) this.findViewById(R.id.et_changepass_confirm);
		
		tv_account = (TextView) this.findViewById(R.id.tv_account);
		tv_account_all = (TextView) this.findViewById(R.id.tv_account_all);
		String number=tv_account_all.getText().toString()+CalldaGlobalConfig.getInstance().getUsername();
		tv_account_all.setText(number);
//		tv_account.setText(CalldaGlobalConfig.getInstance().getUsername());
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];
		if (progressDialog!=null&&progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		if(msg.arg1 == Task.TASK_SUCCESS) {
			String result = (String) msg.obj;
			String[] content = result.split("\\|");
			if("1".equals(content[0])) {	
				//fail
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), content[1]);
			}else if("0".equals(content[0])) {
				//ok
				try {
					//修改成功，重设当前密码
					String newPassword = DesUtil.encrypt(confrimPass, CalldaGlobalConfig.getInstance().getLoginToken());
					CalldaGlobalConfig.getInstance().setPassword(newPassword);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), content[1]);
				this.finish();
			}else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.server_error);
			}
		}else {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.getdata_fail);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_changepass);
		init();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_changepass_back:
			this.finish();
			break;

		case R.id.bn_changepass_ok:
			InputMethodManager imm = (InputMethodManager) this
			.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(bn_ok.getWindowToken(), 0);
				
			changePassword();
			break;
			
		default:
			break;
		}
	}

	private void changePassword() {
		String oldPass = et_old.getText().toString().trim();
		String newPass = et_new.getText().toString().trim();
		confrimPass = et_confirm.getText().toString().trim();
		
		if("".equals(oldPass) || oldPass.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_jmm);
			return;
		}
		if("".equals(newPass) || newPass.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.input_xmm);
			return;
		}
		//验证密码是否规范 6~16位的字母、数字或下划线
		Pattern p = Pattern.compile("\\w{6,16}");
		Matcher m = p.matcher(newPass);
		if(!m.matches()) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.pwd_type);
			return;
		}
		if(!newPass.equals(confrimPass)) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.mmbyz);
			return;
		}
		
		progressDialog = new MyProgressDialog(this, getString(R.string.zzxg));
		progressDialog.show();
		
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		
		Task task = new Task(Task.TASK_CHANGE_PWD);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("oldPwd", oldPass);
		taskParams.put("newPwd", confrimPass);
		taskParams.put("lan", language);

		task.setTaskParams(taskParams);
		MainService.newTask(task);
	}
}
