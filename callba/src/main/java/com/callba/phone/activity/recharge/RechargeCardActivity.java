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
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.cfg.Constant;
import com.callba.phone.pay.alipay.YeePayUtils;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NetworkDetector;
import com.callba.phone.view.CalldaToast;

/**
 * 面额选择
 * @author zhanghw
 * @version 创建时间：2013-9-30 上午10:38:35
 */
public class RechargeCardActivity extends BaseActivity implements OnClickListener {
	private TextView tv_title, tv_info;
	private Button bn_back, bn_submit;
	private EditText et_cardNo, et_cardPass;
	/**
	 * 充值方式 8:移动卡支付 9:联通卡支付 10:电信卡支付
	 */
	private String cardTypeS;
	private int cardType;
	/**
	 * 卡号密码合法标签
	 */
	private Boolean legalTable;
	private String cardNo;
	private String cardPass;
	/**
	 * 要支付的面额
	 */
	private String rechargeMoney;
	private String TAG = "RechargeCardActivity";
	private Context context = RechargeCardActivity.this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.recharge_card);
		initViews();
		super.onCreate(savedInstanceState);
		
	}

	private void initViews() {
		 cardType = getIntent().getIntExtra("cardType", -1);
		 cardTypeS = cardType + "";
		 rechargeMoney = getIntent().getStringExtra("rechargeMoney");
		
		tv_title = (TextView) findViewById(R.id.tv_recharge_title);
		tv_info = (TextView) findViewById(R.id.tv_recharge_info);
		
		if(RechargeActivity.RECHARGE_CARD_YIDONG == cardType) {
			tv_title.setText(String.format(getString(R.string.reccard_title), getString(R.string.reccard_yd)));
			tv_info.setText(String.format(getString(R.string.reccard_info), getString(R.string.reccard_yd)));
		}else if(RechargeActivity.RECHARGE_CARD_LIANTONG == cardType) {
			tv_title.setText(String.format(getString(R.string.reccard_title), getString(R.string.reccard_lt)));
			tv_info.setText(String.format(getString(R.string.reccard_info), getString(R.string.reccard_lt)));
		}else if(RechargeActivity.RECHARGE_CARD_DIANXIN == cardType) {
			tv_title.setText(String.format(getString(R.string.reccard_title), getString(R.string.reccard_dx)));
			tv_info.setText(String.format(getString(R.string.reccard_info), getString(R.string.reccard_dx)));
		}
		
		bn_back = (Button) findViewById(R.id.bn_recharge_back);
		bn_submit = (Button) findViewById(R.id.bn_recharge_submit);
		bn_back.setOnClickListener(this);
		bn_submit.setOnClickListener(this);
		
		et_cardNo = (EditText) findViewById(R.id.et_cardno);
		et_cardPass = (EditText) findViewById(R.id.et_cardpass);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_recharge_back:
			finish();
			break;

		case R.id.bn_recharge_submit:
			recharge();
			break;
		default:
			break;
		}
	}

	private void recharge() {
		cardNo = et_cardNo.getText().toString().trim();
		cardPass = et_cardPass.getText().toString().trim();

		if ("".equals(cardNo) || cardNo.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.reccard_srczkkm);
			return;
		}
		if ("".equals(cardPass) || cardPass.length() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.reccard_srczkmm);
			return;
		}
		/**
		 * 充值方式 8:移动卡支付 9:联通卡支付 10:电信卡支付
		 */
		switch (cardType) {
		case RechargeActivity.RECHARGE_CARD_YIDONG:
			legalTable = YeePayUtils.checkYDCard(cardNo, cardPass);
			cardTypeS = Constant.ydCode;
			break;
		case RechargeActivity.RECHARGE_CARD_LIANTONG:
			legalTable = YeePayUtils.checkLTCard(cardNo, cardPass);
			cardTypeS = Constant.ltCode;
			break;
		case RechargeActivity.RECHARGE_CARD_DIANXIN:
			legalTable = YeePayUtils.checkDXCard(cardNo, cardPass);
			cardTypeS = Constant.dxCode;
			break;
		default:
			break;
		}
		if (!legalTable) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.reccard_error);
			return;
		}
		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == Task.TASK_YEE_PAY) {
					try {
						String result =msg.obj.toString();

						String[] content = result.split("\\|");
						if ("0".equals(content[0])) {
							// 成功
							try {
								new AlertDialog.Builder(context).setTitle(R.string.sacc_tip)
										.setMessage(content[1])
										.setPositiveButton(R.string.ok, null).show();
							} catch (Exception e) {
								e.printStackTrace();
								
								CalldaToast calldaToast = new CalldaToast();
								calldaToast.showToast(getApplicationContext(), R.string.result_data_error);
							}
						} else if ("1".equals(content[0])) {
							// 失败
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
				String lan=activityUtil.language(RechargeCardActivity.this);
				Map<String, String> taskParams = new HashMap<String, String>();
				taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
				taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
				taskParams.put("softType", "android");
				taskParams.put("payMoney", rechargeMoney);
				taskParams.put("payMethod", cardTypeS);
				taskParams.put("cardNumber", cardNo);
				taskParams.put("cardPwd", cardPass);
				taskParams.put("lan", lan);
				Logger.i(TAG, taskParams.toString());
				Message msg = mHandler.obtainMessage();

				try {
					if (NetworkDetector.detect(RechargeCardActivity.this)) {
						String result = HttpUtils.getDataFromHttpPost(
								Interfaces.YEE_PAY, taskParams);
						msg.what = Task.TASK_YEE_PAY;
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
	}

	@Override
	public void refresh(Object... params) {
	}
}
