package com.callba.phone.activity.sms;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.contact.ContactChooseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyDialog;

public class NewSMSActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_title_send, bn_send, bn_contact;
	private EditText et_contact, et_content;
	private TextView tv_text, tv_sms;
	
	@Override
	public void init() {
		tv_text = (TextView) findViewById(R.id.tv_textcount);
		tv_sms = (TextView) findViewById(R.id.tv_smscount);
		
		bn_back = (Button) findViewById(R.id.bn_sms_back);
		bn_title_send = (Button) findViewById(R.id.bn_title_send);
		bn_send = (Button) findViewById(R.id.bn_newsms_send);
		bn_contact = (Button) findViewById(R.id.bn_get_contact);
		bn_back.setOnClickListener(this);
		bn_title_send.setOnClickListener(this);
		bn_send.setOnClickListener(this);
		bn_contact.setOnClickListener(this);
		
		et_contact = (EditText) findViewById(R.id.et_sms_number);
		et_content = (EditText) findViewById(R.id.et_sms_content);
		et_content.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int length = et_content.getText().toString().trim().length();
				tv_text.setText(length+"");
				if(length == 0) {
					tv_sms.setText(0);
				}else {
					tv_sms.setText(((length-1)/70+1) + "");
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		//跳转到新建短信界面，带号码传递过来
		String phoneNum = getIntent().getStringExtra("phoneNumber");
		et_contact.setText(phoneNum);
	}

	@Override
	public void refresh(Object... params) {
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.new_sms);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//选择联系人成功
		if(requestCode == 10 && resultCode == RESULT_OK) {
			et_contact.setText(data.getStringExtra("contacts"));
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_sms_back:
			confirm2Exit();
			break;
		
		case R.id.bn_title_send:
		case R.id.bn_newsms_send:
			sendSMS();
			break;
			
		case R.id.bn_get_contact:
			//选择联系人
			Intent intent = new Intent(this, ContactChooseActivity.class);
			startActivityForResult(intent, 10);
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * 发送短信
	 */
	private void sendSMS() {
		String number = et_contact.getText().toString().trim();
		String context = et_content.getText().toString().trim();
		
		CalldaToast calldaToast = new CalldaToast();
		
		if("".equals(number)) {
			calldaToast.showToast(this, R.string.sipcall_unavailable);
			return;
		}
		if("".equals(context)) {
			calldaToast.showToast(this, R.string.sms_wdxnr);
			return;
		}
		
		if(number.contains(";")) {
			//包含多个收件人
			String[] contacts = number.split(";");
			for(String contactNum : contacts) {
				sendSMSOneByOne(contactNum, context);
			}
		}else {
			sendSMSOneByOne(number, context);
		}
		
		this.finish();
		calldaToast.showToast(this, R.string.sms_dxjrfs);
	}
	
	private void sendSMSOneByOne(String number, String context) {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		Task task = new Task(Task.TASK_SEND_SMS);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("phoneNumber", number);
		taskParams.put("msg", context);
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		
		MainService.newTask(task);
	}

	/**
	 * 退出确认
	 */
	private void confirm2Exit() {
		String number = et_contact.getText().toString().trim();
		String context = et_content.getText().toString().trim();
		//如果联系人或短信内容不为空 提示用户
		if(!"".equals(number) || !"".equals(context)) {
			MyDialog.showDialog(this, getString(R.string.sms_wbjwc), new OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.bn_ok:
						finish();
						MyDialog.dismissDialog();
						break;
					case R.id.bn_cancel:
						MyDialog.dismissDialog();
						break;
					default:
						break;
					}
				}
			});
		}else {
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			confirm2Exit();
		}
		return true;
	}
}
