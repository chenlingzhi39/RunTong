package com.callba.phone.activity.recharge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.pay.alipay.AlipayClient;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.HttpUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.CornerListView;
import com.callba.phone.view.MyProgressDialog;

/**
 * 套餐充值，选择充值方式，开始充值
 * 
 * @author zhanghw
 * @version 创建时间：2013-10-8 上午9:57:08
 */
public class RechargeMealWayActivity extends BaseActivity implements
		OnItemClickListener {
	private TextView tv_account, tv_meal;
	private Button bn_back;
	private CornerListView lv_meal_way;

	private String mealMoney;
	private String mealName;
	private String suiteName;
//	private String TAG = "RechargeMealWayActivity";
	private Context context = RechargeMealWayActivity.this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.recharge_meal_way);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void init() {
		tv_account = (TextView) findViewById(R.id.tv_recmealway_account);
		tv_meal = (TextView) findViewById(R.id.tv_recmealway_meal);

		tv_account.setText(CalldaGlobalConfig.getInstance().getUsername());

		mealMoney = getIntent().getStringExtra("mealMoney");
		mealName = getIntent().getStringExtra("mealName");
		suiteName = getIntent().getStringExtra("suiteName");

		tv_meal.setText(mealName);
		
		lv_meal_way = (CornerListView) findViewById(R.id.lv_recmealway_way);
		lv_meal_way.setAdapter(new SimpleAdapter(this, getData(),
				R.layout.recharge_meal_way_lv_item, new String[] { "icon",
						"info" }, new int[] { R.id.iv_icon, R.id.tv_info }));
		lv_meal_way.setOnItemClickListener(this);

		bn_back = (Button) findViewById(R.id.bn_recmealway_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private List<? extends Map<String, ?>> getData() {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		/*map.put("icon", R.drawable.alipay);
		map.put("info", getString(R.string.recmeal_zfbwy));
		data.add(map);*/

		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.alipay);
		map.put("info", getString(R.string.recmeal_zfbkhd));
		data.add(map);

//		map = new HashMap<String, Object>();
//		map.put("icon", R.drawable.callda);
//		map.put("info", getString(R.string.recmeal_kdye));
//		data.add(map);

		return data;
	}

	@Override
	public void refresh(Object... params) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	/*	if (position == 0) {
			// 支付宝网页
			String url = Interfaces.AliPayWap + "phoneNumber="
					+ Constant.USERNAME + "&money=" + mealMoney;
			Uri uri = Uri.parse(url);
			Logger.i("套餐充值支付宝网页", Intent.ACTION_VIEW + uri);
			startActivity(new Intent(Intent.ACTION_VIEW, uri));

		} else */
			if (position ==0) {
			// 支付宝客户端
			rechargeAlipayClient(mealMoney);

		} 
			/*else if (position == 1) {
			// 闰通余额
			Dialog alertDialog = new AlertDialog.Builder(context)
					.setTitle(R.string.sacc_tip)
					.setMessage(R.string.balance_recharge)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									rechargeYuePay();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							}).create();
			alertDialog.show();

		}*/
	}

	/*private void rechargeYuePay() {
		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == Task.TASK_CALLDA_PAY) {
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
								calldaToast.showToast(context, R.string.result_data_error);
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
								calldaToast.showToast(context, R.string.result_data_error);
							}
						}
					} catch (ClassCastException e) {
						// 登陆发生错误，msg.obj 返回的 异常对象
						
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(context, R.string.login_timeout);
						
						Logger.e(TAG, msg.obj.toString());
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
						
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(context, R.string.login_exception);
					}
				}
			}
		};
		new Thread() {
			public void run() {
				Map<String, String> taskParams = new HashMap<String, String>();
				taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
				taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
				taskParams.put("suiteName", suiteName);
				taskParams.put("softType", "android");
				Logger.i(TAG, taskParams.toString());
				Message msg = mHandler.obtainMessage();

				try {
					if (NetworkDetector.detect(context)) {
						String result = HttpUtils.getDataFromHttpPost(
								Interfaces.CALLDA_YUE_PAY, taskParams);
						Logger.i(TAG,
								Interfaces.CALLDA_PAY + taskParams.toString());
						msg.what = Task.TASK_CALLDA_PAY;
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
	}*/

	/**
	 * 调用支付宝客户端支付
	 */
	private void rechargeAlipayClient(final String payMoney) {
		final MyProgressDialog progressDialog = new MyProgressDialog(this,
				getString(R.string.rech_hqddh));
		progressDialog.show();

		final Handler mHanlder = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (progressDialog!=null&&progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

				if (msg.what == 0) {
					String orderNo = (String) msg.obj;

					new AlipayClient(RechargeMealWayActivity.this, orderNo,
							payMoney).prepared2Pay();
				} else if (msg.what == -1) {
					String errorMsg = (String) msg.obj;
					
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(context, errorMsg);
				}
			}
		};

		new Thread() {
			public void run() {
				ActivityUtil activityUtil=new ActivityUtil();
				String lan=activityUtil.language(RechargeMealWayActivity.this);
				Map<String, String> params = new HashMap<String, String>();
				params.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
				params.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
				params.put("softType", "android");
				params.put("payMoney", payMoney);
				params.put("payMethod", "0");
				params.put("suiteName", suiteName);
				params.put("lan", lan);

				String result = null;
				Message msg = mHanlder.obtainMessage();
				try {
					Logger.i("套餐客户端充值", Interfaces.GET_RECHARGE_TRADENO
							+ params);
					result = HttpUtils.getDataFromHttpPost(
							Interfaces.GET_RECHARGE_TRADENO, params);
				} catch (Exception e1) {
					e1.printStackTrace();
					msg.what = -1;
					msg.obj = getString(R.string.getserverdata_exception);
					mHanlder.sendMessage(msg);
					
					return;
				}

				try {
					String content[] = result.trim().split("\\|");
					if ("0".equals(content[0])) {
						String orderNo = content[1];

						msg.what = 0;
						msg.obj = orderNo;
					} else if ("1".equals(content[0])) {
						msg.what = -1;
						msg.obj = content[1];
					} else {
						msg.what = -1;
						msg.obj = getString(R.string.rech_hqddsb);
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = getString(R.string.getserverdata_exception);
				} finally {
					mHanlder.sendMessage(msg);
				}
			}
		}.start();
	}

}
