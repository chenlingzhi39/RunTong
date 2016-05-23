package com.callba.phone.activity.recharge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.RechargeMealBean;
import com.callba.phone.bean.RechargeMealSuiteBean;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ListViewUtils;
import com.callba.phone.util.Logger;
import com.callba.phone.view.CalldaToast;

/**
 * 套餐充值，选择套餐
 * @author zhanghw
 * @version 创建时间：2013-9-30 下午2:05:32
 */
public class RechargeMealActivity extends BaseActivity implements OnItemClickListener {
	private Button bn_back;
	private TextView tv_zhiboTitle, tv_huiboTitle;
	private ListView lv_zhibo, lv_huibo;
	private LinearLayout ll_loading;
	
	private List<RechargeMealBean> mealbeans;
	
//	private MyProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.recharge_meal);
		super.onCreate(savedInstanceState);

	}

	@Override
	public void init() {
		getRechargeMeal();
//		mProgressDialog = new MyProgressDialog(this, "获取套餐信息");
//		mProgressDialog.show();
		
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

		bn_back = (Button) findViewById(R.id.bn_rechargemeal_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RechargeMealActivity.this.finish();
			}
		});

		tv_zhiboTitle = (TextView) findViewById(R.id.tv_meal_zhibo_title);
		tv_huiboTitle = (TextView) findViewById(R.id.tv_meal_huibo_title);

		lv_zhibo = (ListView) findViewById(R.id.lv_recmeal_zhibo);
		lv_huibo = (ListView) findViewById(R.id.lv_recmeal_huibo);
		lv_zhibo.setOnItemClickListener(this);
		lv_huibo.setOnItemClickListener(this);
	}

	private void getRechargeMeal() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		Task task = new Task(Task.TASK_GET_RECHARGE_MEAL);
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
				Logger.i("获取的套餐内容", result);
				String[] content = result.split("\\|");
				if ("1".equals(content[0])) {
					// fail
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(this, content[1]);
				} else if ("0".equals(content[0])) {
					// ok
					mealbeans = new ArrayList<RechargeMealBean>();
					JSONArray jsonArray = new JSONArray(content[1]);
					for (int i = 0; i < jsonArray.length(); i++) {
						RechargeMealBean mealBean = new RechargeMealBean();
						List<RechargeMealSuiteBean> mealSuiteBeans = new ArrayList<RechargeMealSuiteBean>();
						JSONObject json = jsonArray.getJSONObject(i);
						mealBean.setSuite_name(json.getString("suite_name"));
						
						JSONArray jArray = (JSONArray) json.get("suite");
						for(int j=0; j< jArray.length(); j++) {
							RechargeMealSuiteBean bean = new RechargeMealSuiteBean();
							JSONObject jObject = jArray.getJSONObject(j);
							bean.setShowSuiteName(jObject.getString("showSuiteName"));
							bean.setSuiteName(jObject.getString("suiteName"));
							bean.setMoney(jObject.getString("money"));
							bean.setDiscount(jObject.getString("discount"));
							
							mealSuiteBeans.add(bean);
						}
						mealBean.setSuiteBeans(mealSuiteBeans);
						mealbeans.add(mealBean);
					}
					
					//解析完毕
					setListData(mealbeans);
					ll_loading.setVisibility(View.GONE);
				} else {
					ll_loading.setVisibility(View.GONE);

					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(this, R.string.recmeal_hqtcsb);
				}
			} catch (Exception e) {
				ll_loading.setVisibility(View.GONE);
				
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(this, R.string.getserverdata_exception);
				
				e.printStackTrace();
			}
		} else {
			ll_loading.setVisibility(View.GONE);
			
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(this, R.string.sin_hqsjsb);
		}
	}

	private void setListData(List<RechargeMealBean> mealbeans) {
		RechargeMealBean mealBean1 = mealbeans.get(0);
		tv_zhiboTitle.setText(mealBean1.getSuite_name());
		RechargeMealAdapter adapter1 = new RechargeMealAdapter(1, this, mealBean1.getSuiteBeans());
		lv_zhibo.setAdapter(adapter1);
		
		RechargeMealBean mealBean2 = mealbeans.get(1);
		tv_huiboTitle.setText(mealBean2.getSuite_name());
		RechargeMealAdapter adapter2 = new RechargeMealAdapter(2, this, mealBean2.getSuiteBeans());
		lv_huibo.setAdapter(adapter2);
		
		ListViewUtils.setListViewHeightBasedOnChildren(lv_zhibo);
		ListViewUtils.setListViewHeightBasedOnChildren(lv_huibo);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		String mealName = "";
		String money = "";
		String suiteName = "";
		if(parent == lv_zhibo) {
			RechargeMealBean mealBean = mealbeans.get(0);
			mealName = mealBean.getSuiteBeans().get(position).getShowSuiteName();
			suiteName = mealBean.getSuiteBeans().get(position).getSuiteName();
			money = mealBean.getSuiteBeans().get(position).getMoney();
		}else if(parent == lv_huibo) {
			RechargeMealBean mealBean = mealbeans.get(1);
			mealName = mealBean.getSuiteBeans().get(position).getShowSuiteName();
			suiteName = mealBean.getSuiteBeans().get(position).getSuiteName();
			money = mealBean.getSuiteBeans().get(position).getMoney();
		}
		
		Intent intent = new Intent(this, RechargeMealWayActivity.class);
		intent.putExtra("mealName", mealName);
		intent.putExtra("suiteName", suiteName);
		intent.putExtra("mealMoney", money);
		Logger.i("套餐选择", "mealName:"+mealName+"\nsuiteName:"+suiteName+"\nmoney:"+money);
		startActivity(intent);
	}
}
