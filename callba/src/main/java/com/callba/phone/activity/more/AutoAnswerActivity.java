package com.callba.phone.activity.more;

import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import com.callba.phone.util.SharedPreferenceUtil;

public class AutoAnswerActivity extends BaseActivity {
	public final static String TAG = "AutoAnswerActivity";
	public final static String B_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;  
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_autoanswer);
		
		Button bn_back = (Button) this.findViewById(R.id.bn_answer_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AutoAnswerActivity.this.finish();
			}
		});
		
		ToggleButton tb_autoAnswer = (ToggleButton) findViewById(R.id.tb_autoanswer);
		tb_autoAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				SharedPreferenceUtil mPreferenceUtil = SharedPreferenceUtil.getInstance(AutoAnswerActivity.this);
				mPreferenceUtil.putBoolean(Constant.BackCall_AutoAnswer, isChecked, true);
				
				CalldaGlobalConfig.getInstance().setCallBackAutoAnswer(isChecked);
			}
		});
			
		tb_autoAnswer.setChecked(CalldaGlobalConfig.getInstance().isCallBackAutoAnswer());
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

	@Override
	public void init() {
	}

	@Override
	public void refresh(Object... params) {
	}
}
