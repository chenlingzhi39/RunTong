package com.callba.phone.activity.more;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.MyApplication;
import com.callba.phone.activity.login.LoginActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.logic.login.LoginController;
import com.callba.phone.util.SharedPreferenceUtil;
@ActivityFragmentInject(
		contentViewId = R.layout.more_exit
)
public class ExitActivity extends BaseActivity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Button bn_back = (Button) findViewById(R.id.bn_exit_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExitActivity.this.finish();
			}
		});

		Button bn_exit = (Button) findViewById(R.id.bn_exit);
		bn_exit.setOnClickListener(this);
	}

	@Override
	public void refresh(Object... params) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_exit:
			// super.exitApp();
//			for (Activity activity : MyApplication.activities) {
//				activity.finish();
//			}
			zhuxiao();
			break;

		default:
			break;
		}
	}
	private void deleteAll() {

		CalldaGlobalConfig.getInstance().setUsername("");
		CalldaGlobalConfig.getInstance().setPassword("");
		CalldaGlobalConfig.getInstance().setIvPath("");
		LoginController.getInstance().setUserLoginState(false);
	}
	private void zhuxiao() {
		deleteAll();
		SharedPreferenceUtil.getInstance(this).putString(Constant.LOGIN_PASSWORD, "",true);

		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		for (Activity activity : MyApplication.activities) {
			activity.finish();
		}
		startActivity(intent);
	}
}
