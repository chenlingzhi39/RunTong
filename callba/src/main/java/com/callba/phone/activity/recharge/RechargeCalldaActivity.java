package com.callba.phone.activity.recharge;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NetworkDetector;
import com.callba.phone.view.CalldaToast;

/**
 * @author zhanghw
 * @version 创建时间：2013-9-30 上午10:25:18
 */
public class RechargeCalldaActivity extends BaseActivity implements
		OnClickListener {
	private Button bn_back, bn_submit;
	private EditText et_cardPass, et_mobileNo;
	private String TAG = "RechargeCalldaActivity";
	private Context context = RechargeCalldaActivity.this;
	private String mobileNo;
	private String cardPass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.recharge_card_callda);

		bn_back = (Button) findViewById(R.id.bn_recharge_callda_back);
		bn_submit = (Button) findViewById(R.id.bn_recharge_callda);
		bn_back.setOnClickListener(this);
		bn_submit.setOnClickListener(this);

		et_mobileNo = (EditText) findViewById(R.id.et_cardno);

		et_cardPass = (EditText) findViewById(R.id.et_recharge_cardpass);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		et_mobileNo.setHint(CalldaGlobalConfig.getInstance().getUsername());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_recharge_callda_back:
			finish();
			break;

		case R.id.bn_recharge_callda:
			recharge();
			break;
		default:
			break;
		}
	}

	private void recharge() {
		mobileNo = et_mobileNo.getText().toString().trim();
		cardPass = et_cardPass.getText().toString().trim();
		if ("".equals(mobileNo) || mobileNo.length() < 1) {
			mobileNo = CalldaGlobalConfig.getInstance().getUsername();
		} else if (11 != mobileNo.length()) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.recallda_error);
			return;
		}
		if ("".equals(cardPass) || cardPass.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.recallda_xsrkm);
			return;
		}
		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == Task.TASK_CALLDA_YUE_PAY) {
					try {
						String result = msg.obj.toString();
						Logger.i(TAG, result);
						String[] content = result.split("\\|");
						if ("0".equals(content[0])) {
							// 成功
							try {
								new AlertDialog.Builder(context)
										.setTitle(R.string.sacc_tip)
										.setMessage(content[1])
										.setPositiveButton(R.string.ok, null)
										.show();
							} catch (Exception e) {
								e.printStackTrace();
								
								CalldaToast calldaToast = new CalldaToast();
								calldaToast.showToast(getApplicationContext(), R.string.result_data_error);
							}
						} else if ("1".equals(content[0])) {
							try {
								new AlertDialog.Builder(context)
										.setTitle(R.string.sacc_tip)
										.setMessage(content[1])
										.setPositiveButton(R.string.ok, null)
										.show();
							} catch (Exception e) {
								e.printStackTrace();
								
								CalldaToast calldaToast = new CalldaToast();
								calldaToast.showToast(getApplicationContext(), R.string.result_data_error);
							}
						}
					} catch (ClassCastException e) {
						// 登陆发生错误，msg.obj 返回的 异常对象
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), R.string.login_timeout);
						
						Logger.e(TAG, msg.obj.toString());
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
						
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), R.string.login_exception);
					}
				}
			}
		};
		new Thread() {
			public void run() {
				ActivityUtil activityUtil=new ActivityUtil();
				String lan=activityUtil.language(RechargeCalldaActivity.this);
				Map<String, String> taskParams = new HashMap<String, String>();
				taskParams.put("phoneNumber", mobileNo);
				taskParams.put("cardNumber", cardPass);
				taskParams.put("fromtel",CalldaGlobalConfig.getInstance().getUsername());
				taskParams.put("softType", "android");
				taskParams.put("lan", lan);
				Logger.i(TAG, taskParams.toString());
				Message msg = mHandler.obtainMessage();

				try {
					if (NetworkDetector.detect(context)) {
						String result = HttpUtils.getDataFromHttpPost(
								Interfaces.CALLDA_PAY, taskParams);
						msg.what = Task.TASK_CALLDA_YUE_PAY;
						msg.arg1 = Task.TASK_SUCCESS;
						msg.obj = result.replace("\n", "").replace("\r", "");
					} else {
						// 无网络连接
						msg.what = Task.TASK_NETWORK_ERROR;
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = Task.TASK_FAILED;
				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}.start();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object... params) {
		// TODO Auto-generated method stub

	}
}
