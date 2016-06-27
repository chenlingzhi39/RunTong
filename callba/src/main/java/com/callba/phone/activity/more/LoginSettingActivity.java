package com.callba.phone.activity.more;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.SharedPreferenceUtil;

public class LoginSettingActivity extends BaseActivity implements OnClickListener {
	private Button bn_back;
	private LinearLayout ll_auto, ll_manual;
	private ImageView iv_auto, iv_manual;
	
	private SharedPreferenceUtil mPreferenceUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_loginsetting);
		super.onCreate(savedInstanceState);
		
		initView();
		
		mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
		
		if(mPreferenceUtil.getBoolean(Constant.Auto_Login, true)) {
			iv_auto.setVisibility(View.VISIBLE);
		}else {
			iv_manual.setVisibility(View.VISIBLE);
		}
	}
	/**
	 * 界面初始化
	 */
	private void initView() {
		bn_back = (Button) this.findViewById(R.id.bn_loginsetting_back);
		bn_back.setOnClickListener(this);
		
		ll_auto = (LinearLayout) this.findViewById(R.id.ll_auto);
		ll_manual = (LinearLayout) this.findViewById(R.id.ll_manual);
		ll_auto.setOnClickListener(this);
		ll_manual.setOnClickListener(this);
		
		iv_auto = (ImageView) this.findViewById(R.id.iv_auto);
		iv_manual = (ImageView) this.findViewById(R.id.iv_manual);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_loginsetting_back:
			finish();
			break;

		case R.id.ll_auto:
			iv_manual.setVisibility(View.INVISIBLE);
			iv_auto.setVisibility(View.VISIBLE);
			
			mPreferenceUtil.putBoolean(Constant.Auto_Login, true, true);
			break;
			
		case R.id.ll_manual:
			iv_auto.setVisibility(View.INVISIBLE);
			iv_manual.setVisibility(View.VISIBLE);
			
			mPreferenceUtil.putBoolean(Constant.Auto_Login, false, true);
			break;
			
		default:
			break;
		}
	}
	@Override
	public void refresh(Object... params) {
	}
}
