package com.callba.phone.activity.recharge;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.pay.alipay.AlipayClient;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;
/**
 * 普通支付选择面额
 * @author zxf
 *
 */
public class RechargeActivity extends BaseActivity implements OnClickListener {
	protected static final String TAG = RechargeActivity.class.getCanonicalName();

	private Button bn_back;
	private LinearLayout ll_30, ll_50, ll_100, ll_200, ll_300, ll_callda;
	private TextView tv_30, tv_50, tv_100, tv_200, tv_300;
	private TextView tv_title;

	private int rechargeWay = 0; // 充值方式
	
	private String lan;

	public static final int RECHARGE_ALIPAY_WAP = 1; // 支付宝网页
	public static final int RECHARGE_ALIPAY_CLIENT = 2; // 支付宝客户端
	public static final int RECHARGE_CARD_CALLDA = 3; // 闰通充值卡
	public static final int RECHARGE_CARD_YIDONG = 4; // 移动充值卡
	public static final int RECHARGE_CARD_LIANTONG = 5; // 联通充值卡
	public static final int RECHARGE_CARD_DIANXIN = 6; // 电信充值卡

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.recharge);
		init();
		rechargeWay = getIntent().getIntExtra("rechargeWay", 0);

		super.onCreate(savedInstanceState);

		if(rechargeWay == RECHARGE_ALIPAY_WAP) {
			tv_title.setText(R.string.rech_zfbwy);
		}else if(rechargeWay == RECHARGE_ALIPAY_CLIENT) {
			tv_title.setText(R.string.rech_zfbkhd);
		}else if(rechargeWay == RECHARGE_CARD_YIDONG) {
			tv_title.setText(R.string.rech_ydk);
		}else if(rechargeWay == RECHARGE_CARD_LIANTONG) {
			tv_title.setText(R.string.rech_ltk);
		}else if(rechargeWay == RECHARGE_CARD_DIANXIN) {
			tv_title.setText(R.string.rech_dxk);
		}
	}

	public void init() {
		ActivityUtil activityUtil=new ActivityUtil();
		lan=activityUtil.language(RechargeActivity.this);
		
		getRechargeInfoTask();

		bn_back = (Button) findViewById(R.id.bn_recharge_back);
		// bn_recharge_more = (Button) findViewById(R.id.bn_recharege_more);
		bn_back.setOnClickListener(this);
		// bn_recharge_more.setOnClickListener(this);

		tv_30 = (TextView) findViewById(R.id.tv_recharge_30);
		tv_50 = (TextView) findViewById(R.id.tv_recharge_50);
		tv_100 = (TextView) findViewById(R.id.tv_recharge_100);
		tv_200 = (TextView) findViewById(R.id.tv_recharge_200);
		tv_300 = (TextView) findViewById(R.id.tv_recharge_300);

		ll_30 = (LinearLayout) findViewById(R.id.ll_recharge_30);
		ll_50 = (LinearLayout) findViewById(R.id.ll_recharge_50);
		ll_100 = (LinearLayout) findViewById(R.id.ll_recharge_100);
		ll_200 = (LinearLayout) findViewById(R.id.ll_recharge_200);
		ll_300 = (LinearLayout) findViewById(R.id.ll_recharge_300);
		ll_callda = (LinearLayout) findViewById(R.id.ll_recharge_callda);
		ll_30.setOnClickListener(this);
		ll_50.setOnClickListener(this);
		ll_100.setOnClickListener(this);
		ll_200.setOnClickListener(this);
		ll_300.setOnClickListener(this);
		ll_callda.setOnClickListener(this);
		
		tv_title = (TextView) findViewById(R.id.tv_recharge_title);
	}

	private void getRechargeInfoTask() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		Task task = new Task(Task.TASK_GET_RECHARGE_INFO);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];

		if (msg.arg1 == Task.TASK_SUCCESS) {
			try {
				String result = (String) msg.obj;
				String[] content = result.split("\\|");
				if ("1".equals(content[0])) {
					// fail
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), content[1]);
				} else if ("0".equals(content[0])) {
					// ok
					JSONArray jsonArray = new JSONArray(content[1]);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject json = jsonArray.getJSONObject(i);
						String money = json.getString("money");
						if ("30".equals(money)) {
							tv_30.setText(json.getString("showInfo"));
						} else if ("50".equals(money)) {
							tv_50.setText(json.getString("showInfo"));
						} else if ("100".equals(money)) {
							tv_100.setText(json.getString("showInfo"));
						} else if ("200".equals(money)) {
							tv_200.setText(json.getString("showInfo"));
						} else if ("300".equals(money)) {
							tv_300.setText(json.getString("showInfo"));
						}
					}

				} else {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} catch (Exception e) {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				e.printStackTrace();
			}
		} else {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.sin_hqsjsb);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_recharge_back:
			finish();
			break;

		// case R.id.bn_recharege_more:
		// Intent intent = new Intent(this, RechargeWayActivity.class);
		// startActivity(intent);
		// break;

		case R.id.ll_recharge_30:
			rechargeAlipayWap(30);
			rechargeAlipayClient(String.valueOf(30));
			rechargeCardPay(String.valueOf(30));
			break;

		case R.id.ll_recharge_50:
			rechargeAlipayWap(50);
			rechargeAlipayClient(String.valueOf(50));
			rechargeCardPay(String.valueOf(50));
			break;

		case R.id.ll_recharge_100:
			rechargeAlipayWap(100);
			rechargeAlipayClient(String.valueOf(100));
			rechargeCardPay(String.valueOf(100));
			break;

		case R.id.ll_recharge_200:
			rechargeAlipayWap(200);
			rechargeAlipayClient(String.valueOf(200));
			rechargeCardPay(String.valueOf(200));
			break;

		case R.id.ll_recharge_300:
			rechargeAlipayWap(300);
			rechargeAlipayClient(String.valueOf(300));
			rechargeCardPay(String.valueOf(300));
			break;

		case R.id.ll_recharge_callda:
			Intent intent = new Intent(this, RechargeMealActivity.class);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 充值卡支付
	 * @param rechargeMoney
	 */
	private void rechargeCardPay(String rechargeMoney) {
		if(rechargeWay == RECHARGE_CARD_CALLDA
				|| rechargeWay == RECHARGE_CARD_YIDONG
				|| rechargeWay == RECHARGE_CARD_LIANTONG
				|| rechargeWay == RECHARGE_CARD_DIANXIN) {
			
			Intent intent = new Intent(this, RechargeCardActivity.class);
			intent.putExtra("cardType", rechargeWay);
			intent.putExtra("rechargeMoney", rechargeMoney);
			startActivity(intent);
		}
	}

	/**
	 * 调用支付宝客户端支付
	 */
	private void rechargeAlipayClient(final String payMoney) {
		if (rechargeWay != RECHARGE_ALIPAY_CLIENT) {
			return;
		}
		
		final MyProgressDialog progressDialog = new MyProgressDialog(this, getString(R.string.rech_hqddh));
		progressDialog.show();
		
		final Handler mHanlder = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				if(msg.what == 0) {
					String orderNo = (String) msg.obj;
					Logger.i(TAG, "Rearge Alipay orderNo. : " + orderNo);
					//调用支付宝客户端
					new AlipayClient(RechargeActivity.this, orderNo, payMoney).prepared2Pay();
				}else if(msg.what == -1) {
					String errorMsg = (String) msg.obj;
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), errorMsg);
				}
			}
		};
		
		new Thread(){
			public void run() {
				
				Map<String, String> params = new HashMap<String, String>();
				params.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
				params.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
				params.put("softType", "android");
				params.put("payMoney", payMoney);
				params.put("payMethod", "0");
				params.put("suiteName", "");
				params.put("lan", lan);
				
				String result = null;
				Message msg = mHanlder.obtainMessage();
				try {
					Logger.i("客户端调用", Interfaces.GET_RECHARGE_TRADENO+params);
					result = HttpUtils.getDataFromHttpPost(Interfaces.GET_RECHARGE_TRADENO, params);
				} catch (Exception e1) {
					e1.printStackTrace();
					msg.what = -1;
					msg.obj = getString(R.string.getserverdata_exception);
				}
				
				try {
					Logger.i("客户端调用", "result:"+result.trim());
					String content[] = result.trim().split("\\|");
					if("0".equals(content[0])) {
						String orderNo = content[1];
						
						msg.what = 0;
						msg.obj = orderNo;
					}else {
						msg.what = -1;
						msg.obj = getString(R.string.rech_hqddsb);
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = getString(R.string.getserverdata_exception);
				}finally {
					mHanlder.sendMessage(msg);
				}
			}
		}.start();
	}

	/**
	 * 调用支付宝网页支付
	 * @param money
	 */
	private void rechargeAlipayWap(int money) {
		if (rechargeWay == RECHARGE_ALIPAY_WAP) {
			String url = Interfaces.AliPayWap + "phoneNumber="
					+ CalldaGlobalConfig.getInstance().getUsername()
					+ "&money=" + money+"&lan"+lan;
			Uri uri = Uri.parse(url);
			Logger.i("普通充值支付宝网页", Intent.ACTION_VIEW+ uri);
		    startActivity(new Intent(Intent.ACTION_VIEW, uri)); 
		}
	}
}
