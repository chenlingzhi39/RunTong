package com.callba.phone.activity.more;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.FeeBean;
import com.callba.phone.bean.Task;
import com.callba.phone.service.MainService;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

public class FeeQueryActivity extends BaseActivity implements OnClickListener {
	private Button bn_search, bn_back;
	private EditText et_searchText;
	private ListView mSearchedList;
	private LinearLayout ll_commonFee;
	
	private MyProgressDialog progressDialog;

	public void init() {
		bn_search = (Button) findViewById(R.id.bn_fee_search);
		bn_back = (Button) findViewById(R.id.bn_fee_back);
		bn_back.setOnClickListener(this);
		bn_search.setOnClickListener(this);
		
		et_searchText = (EditText) findViewById(R.id.et_fee_search);
		mSearchedList = (ListView) findViewById(R.id.lv_queryed_fee11);
		ll_commonFee = (LinearLayout) findViewById(R.id.ll_commonfee);
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];
		
		if (progressDialog!=null&&progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		if(msg.what == Task.TASK_FEE_QUERY) {
			//获取短信key
			if(msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					String[] content = result.split("\\|");
					if("0".equals(content[0])) {
						List<FeeBean> feeBeans = new ArrayList<FeeBean>();
						String jsonStr = content[1];
						
						JSONArray jsonArray = new JSONArray(jsonStr);
						for (int i = 0; i < jsonArray.length(); i++) {
							FeeBean bean = new FeeBean();
							JSONObject json = jsonArray.getJSONObject(i);
							bean.setCode(json.getString("code"));
							bean.setName_C(json.getString("name_C"));
							bean.setName_E(json.getString("name_E"));
							bean.setPrice(json.getString("price"));
							
							feeBeans.add(bean);
						}
						
						setListAdapter(feeBeans);
						ll_commonFee.setVisibility(View.INVISIBLE);
						mSearchedList.setVisibility(View.VISIBLE);
					}else {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);
					}
				} catch (Exception e) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.server_error);
					e.printStackTrace();
				}
			}else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.fee_query_fail);
			}
		}
	}
	/**
	 * 为查询List设置数据
	 * @param feeBeans
	 */
	private void setListAdapter(List<FeeBean> feeBeans) {
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		Map<String, Object> map;
		
		for(FeeBean bean : feeBeans) {
			map = new HashMap<String, Object>();
			map.put("code", bean.getCode());
			map.put("name_c", bean.getName_C());
			map.put("name_e", bean.getName_E());
			map.put("price", String.format(getString(R.string.fee_money), bean.getPrice()));
			
			data.add(map);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.fee_query_lv_item,
				new String[]{"code", "name_c", "name_e", "price"},
				new int[]{R.id.tv_feelv_code, R.id.tv_feelv_cname, R.id.tv_feelv_ename, R.id.tv_feelv_price});
		mSearchedList.setAdapter(adapter);
//		ListViewUtils.setListViewHeightBasedOnChildren(mSearchedList);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_fee);
		init();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_fee_back:
			finish();
			break;

		case R.id.bn_fee_search:
			progressDialog = new MyProgressDialog(this, getString(R.string.fee_querying));
			progressDialog.show();
			
			InputMethodManager imm = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(bn_search.getWindowToken(), 0);
			doFeeSearch();
			break;
			
		default:
			break;
		}
	}
	
	private void doFeeSearch() {
		String searchText = et_searchText.getText().toString().trim();
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		Task task = new Task(Task.TASK_FEE_QUERY);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("searchText", searchText);
		taskParams.put("softType", "android");
		taskParams.put("lan",language);
		task.setTaskParams(taskParams);
		
		MainService.newTask(task);
	}
}
