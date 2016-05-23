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
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SharedPreferenceUtil;

public class KeyboardSettingActivity extends BaseActivity {
	public final static String TAG = "KeyboardSettingActivity";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.keyboard_setting);
		
		Button bn_back = (Button) this.findViewById(R.id.bn_answer_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KeyboardSettingActivity.this.finish();
			}
		});
		
		ToggleButton tb_autoAnswer = (ToggleButton) findViewById(R.id.tb_keyboardyin);
		tb_autoAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferenceUtil mPreferenceUtil = SharedPreferenceUtil.getInstance(KeyboardSettingActivity.this);
				mPreferenceUtil.putBoolean(Constant.KeyboardSetting, isChecked, true);
				
				CalldaGlobalConfig.getInstance().setKeyBoardSetting(isChecked);
				Logger.i("键盘音设置", isChecked+"");
			}
		});
		
		tb_autoAnswer.setChecked(CalldaGlobalConfig.getInstance().getKeyBoardSetting());
	}
	
	@Override
	public void init() {
	}

	@Override
	public void refresh(Object... params) {
	}
}
