package com.callba.phone.activity.more;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.Logger;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

/**
 * @author zhanghw
 * @version 创建时间：2013-10-8 下午2:41:49
 */
public class QueryMealActivity extends BaseActivity {
	private Button bn_back,bn_refresh;
	private TextView tv_balance;
	private ListView lv_meal;

	private MyProgressDialog progressDialog;
	private String language="";

	@Override
	public void init() {
		Locale locale = getResources().getConfiguration().locale;
		 language = locale.getCountry();
		
		bn_back = (Button) findViewById(R.id.bn_querymeal_back);
		bn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		bn_refresh = (Button) findViewById(R.id.bn_refresh);
		bn_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				queryUserBalance();
				queryMeal();
			}
		});

		tv_balance = (TextView) findViewById(R.id.tv_querymeal_yue);
		tv_balance.setText(CalldaGlobalConfig.getInstance().getAccountBalance());

		lv_meal = (ListView) findViewById(R.id.lv_querymeal);
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];
		
		if (msg.what == Task.TASK_QUERY_MEAL) {
			if (progressDialog!=null&&progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						
						parseData(result);
						
					} else if ("1".equals(result[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getserverdata_failed);
			}
			
		} else if(msg.what == Task.TASK_GET_USER_BALANCE) {
			if (progressDialog!=null&&progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				try {
					String[] result = content.split("\\|");
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						String balance = result[1].trim();
						CalldaGlobalConfig.getInstance().setAccountBalance(balance);
						tv_balance.setText(balance);
						
					} else if ("1".equals(result[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.getserverdata_exception);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.snum_cxsb);
			}
		}
	}

	/**
	 * 解析返回数据
	 * 
	 * @param result
	 */
	private void parseData(String[] result) {
		if(result.length < 2) return;
		
		String content = result[1].replace("\r", "").replace("\n", "").trim();
		if ("".equals(content) || content.length() < 1)
			return;
		Logger.i("返回数据", content);

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		String[] str = content.split("&");
		for (int i = 0; i < str.length; i++) {
			String[] fields = str[i].split(",");

			map = new HashMap<String, Object>();
			if("suite_callback".equals(fields[0])){
				map.put("mealType", "回拨套餐");
			}else if("suite_sip".equals(fields[0])){
				map.put("mealType", "直拨套餐");
			}
//			map.put("mealType", fields[0]);
			map.put("mealRemain", fields[5]);
			map.put("endTime", fields[3]);

			data.add(map);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, data,
				R.layout.more_querymeal_lv_item,
				new String[] {"mealType", "mealRemain", "endTime"},
				new int[] {R.id.tv_meal_name, R.id.tv_meal_syfz, R.id.tv_meal_jssj});
		lv_meal.setAdapter(adapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_querymeal);
		super.onCreate(savedInstanceState);
		
		progressDialog = new MyProgressDialog(this, getString(R.string.qm_cxtc));
		
		queryUserBalance();
		queryMeal();
		
	}

	/**
	 * 查询用户已开通套餐
	 */
	private void queryMeal() {
		progressDialog.show();
		
		Task task = new Task(Task.TASK_QUERY_MEAL);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		
		MainService.newTask(task);
	}
	/**
	 * 查询用户余额
	 */
	private void queryUserBalance() {
		 progressDialog.show();
			
		 Task task = new Task(Task.TASK_GET_USER_BALANCE);
		 Map<String, Object> taskParams = new HashMap<String, Object>();
		 taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		 taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		 taskParams.put("softType", "android");
		 taskParams.put("frompage", "QueryMealActivity");
		 taskParams.put("lan", language);
		 task.setTaskParams(taskParams);
		 
		 MainService.newTask(task);
	}
}
