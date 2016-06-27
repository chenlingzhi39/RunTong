package com.callba.phone.activity.more;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.SharedPreferenceUtil;

public class SystemDialSettingActivity extends BaseActivity {
	public final static String TAG = "KeyboardSettingActivity";
	
	private SharedPreferenceUtil mPreferenceUtil;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.systemdial_setting);
		
		Button bn_back = (Button) this.findViewById(R.id.bn_answer_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SystemDialSettingActivity.this.finish();
			}
		});
		
		mPreferenceUtil = SharedPreferenceUtil.getInstance(SystemDialSettingActivity.this);
		
		ToggleButton tb_MonitorDialer = (ToggleButton) findViewById(R.id.tb_keyboardyin);
		tb_MonitorDialer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mPreferenceUtil.putBoolean(Constant.SYSTEM_DIAL_SETTING, isChecked, true);
			}
		});
			
		tb_MonitorDialer.setChecked(mPreferenceUtil.getBoolean(Constant.SYSTEM_DIAL_SETTING, false));
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

	@Override
	public void refresh(Object... params) {
	}
}
