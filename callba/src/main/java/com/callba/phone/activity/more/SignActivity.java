package com.callba.phone.activity.more;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

public class SignActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_sign;
	private MyProgressDialog progressDialog;

	public void init() {
		bn_back = (Button) findViewById(R.id.bn_sign_back);
		bn_sign = (Button) findViewById(R.id.bn_sign_submit);
		bn_back.setOnClickListener(this);
		bn_sign.setOnClickListener(this);
		
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];
		if (progressDialog!=null&&progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		CalldaToast mCalldaToast = new CalldaToast();
		
		if(msg.arg1 == Task.TASK_SUCCESS) {
			String result = (String) msg.obj;
			String[] content = result.split("\\|");
			if("1".equals(content[0])) {	
				//fail
//				Toast.makeText(this, content[1], 0).show();
				mCalldaToast.showToast(getApplicationContext(), content[1]);
			}else if("0".equals(content[0])) {
				//ok
				mCalldaToast.showToast(getApplicationContext(), content[1]);
//				this.finish();
			}else {
				mCalldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
			}
		}else {
			mCalldaToast.showToast(getApplicationContext(), R.string.sin_hqsjsb);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_sign);
		init();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_sign_back:
			finish();
			break;

		case R.id.bn_sign_submit:
			sign();
			break;
			
		default:
			break;
		}
	}
	/**
	 * 签到
	 */
	private void sign() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		Task task = new Task(Task.TASK_SIGN);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
		
		progressDialog = new MyProgressDialog(this, getString(R.string.sin_zzqd));
		progressDialog.show();
	}
}
