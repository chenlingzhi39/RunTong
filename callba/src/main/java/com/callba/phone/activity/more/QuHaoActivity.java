package com.callba.phone.activity.more;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.cfg.Constant;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.CalldaToast;

public class QuHaoActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_ok;
	private EditText et_inputNum;
	
	private SharedPreferenceUtil mPreferenceUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_quhao);
		
		bn_back = (Button) findViewById(R.id.bn_quhao_back);
		bn_ok = (Button) findViewById(R.id.bn_quhao_ok);
		bn_back.setOnClickListener(this);
		bn_ok.setOnClickListener(this);
		
		et_inputNum = (EditText) findViewById(R.id.et_quhao_number);
		
		mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
		
		String quhao = mPreferenceUtil.getString(Constant.QU_HAO);
		et_inputNum.setText(quhao);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_quhao_back:
			finish();
			break;

		case R.id.bn_quhao_ok:
			String quhao = et_inputNum.getText().toString().trim();
			mPreferenceUtil.putString(Constant.QU_HAO, quhao, true);
			
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.setup_success);
			break;
			
		default:
			break;
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void refresh(Object... params) {
	}
}
