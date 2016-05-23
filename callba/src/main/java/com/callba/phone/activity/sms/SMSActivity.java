package com.callba.phone.activity.sms;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.util.ActivityUtil;

public class SMSActivity extends BaseActivity implements OnClickListener {
	private Button bn_new;
	private LinearLayout ll_new, ll_history;
	
	@Override
	public void init() {
		bn_new = (Button) this.findViewById(R.id.bn_sms_new);
		ll_new = (LinearLayout) this.findViewById(R.id.ll_sms_new);
		ll_history = (LinearLayout) this.findViewById(R.id.ll_sms_his);
		
		bn_new.setOnClickListener(this);
		ll_new.setOnClickListener(this);
		ll_history.setOnClickListener(this);
	}

	@Override
	public void refresh(Object... params) {

	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.tab_sms);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_sms_new:
		case R.id.ll_sms_new:
			Intent intent = new Intent(this, NewSMSActivity.class);
			startActivity(intent);
			break;
			
		case R.id.ll_sms_his:
			
			break;

		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//转到后台运行
			ActivityUtil.moveAllActivityToBack();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_exit) {
			super.exitApp();
		}
		return true;
	}
}
