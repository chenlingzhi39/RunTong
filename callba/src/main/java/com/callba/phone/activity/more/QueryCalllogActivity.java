package com.callba.phone.activity.more;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.Logger;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;

public class QueryCalllogActivity extends BaseActivity implements
		OnClickListener, OnScrollListener,OnItemSelectedListener {
	private Button bn_back, bn_queryZhibo, bn_queryHuibo;
	private EditText et_year, et_month;
	private ListView lv_calllog;
	private LinearLayout ll_loading;
	private Spinner sp_month, sp_year;

	private int prePageNum = 15; // List每页显示的calllog数量

	private int lastVisibleItem = 0;
	private int fristVisItem = 0;
	private int currentPage = 1;
	private String currentCallogType = ""; // 记录当前查询的通话记录类型

	private String year, month; // 查询的年 月
	private Time time;
	private int nowY;
	private int nowM;
	private int startY = 2010;
	private int i_y_num;
	private ArrayAdapter<String> arrayadapter;
	// 真正的字符串数据将保存在这个list中
	private List<String> all;
	private MyProgressDialog progressDialog;

	// 存储查询到的通话记录
	private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	private SimpleAdapter adapter;

	@Override
	public void init() {
		bn_back = (Button) findViewById(R.id.bn_calllog_back);
		bn_queryZhibo = (Button) findViewById(R.id.bn_calllog_zhibo);
		bn_queryHuibo = (Button) findViewById(R.id.bn_calllog_huibo);
		bn_back.setOnClickListener(this);
		bn_queryZhibo.setOnClickListener(this);
		bn_queryHuibo.setOnClickListener(this);

		time = new Time();
		time.setToNow(); // 取得系统时间。
		nowY = time.year;
		Logger.i("time.year", nowY+"");
		nowM = time.month;
		i_y_num = nowY - startY;
		all = new ArrayList<String>();
		for (int i = 0; i <= i_y_num; i++) {
			all.add(startY + i + "");
		}
		
		// et_year = (EditText) findViewById(R.id.et_calllog_year);
		sp_year = (Spinner) findViewById(R.id.sp_calllog_year);
		arrayadapter = new ArrayAdapter<String>(this, R.layout.item_spinner,
				all);
		arrayadapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_year.setAdapter(arrayadapter);
		sp_year.setSelection(i_y_num);
		sp_year.setOnItemSelectedListener(this);
		// et_month = (EditText) findViewById(R.id.et_calllog_month);
		sp_month = (Spinner) findViewById(R.id.sp_calllog_month);
		sp_month.setSelection(nowM);
		sp_month.setOnItemSelectedListener(this);

		lv_calllog = (ListView) findViewById(R.id.lv_calllog);
		lv_calllog.setOnScrollListener(this);

		adapter = new SimpleAdapter(this, data,
				R.layout.more_query_calllog_lv_item, new String[] { "no",
						"callnum", "startTime", "duration", "fee" }, new int[] {
						R.id.tv_calllog_no, R.id.tv_calllog_phontnum,
						R.id.tv_calllog_time, R.id.tv_calllog_duration,
						R.id.tv_calllog_money });
		lv_calllog.setAdapter(adapter);

		ll_loading = (LinearLayout) findViewById(R.id.ll_load_more);
		year=nowY+"";
		month=nowM+1+"";
		Logger.i("OnCreate", "year:"+year+"-------month"+month);
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];

		ll_loading.setVisibility(View.GONE);

		if (msg.what == Task.TASK_QUERY_CALLLOG) {
			if (progressDialog!=null&&progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			if (msg.arg1 == Task.TASK_SUCCESS) {
				String content = (String) msg.obj;
				Logger.i("查询话单返回", content);
				try {
					String[] result = content.split("\\|");
					
					if ("0".equals(result[0])) {
						// 成功fanhui数据
						Logger.i("查询话单通话记录", content);
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
		}
	}

	/**
	 * 解析返回数据
	 * 
	 * @param result
	 * @throws JSONException
	 * 
	 *             0|[{"callTotalNum":"53"},{"callContent":[{"callee":
	 *             "18851572781"
	 *             ,"startTime":"2013-09-01 16:39:44","hottime":"1",
	 *             "fee":"0.1"},
	 *             {"callee":"18851572781","startTime":"2013-09-01 13:28:51"
	 *             ,"hottime"
	 *             :"1","fee":"0.1"},{"callee":"18851572781","startTime"
	 *             :"2013-09-01 12:43:19","hottime":"39","fee":"0.1"}]}]
	 */
	private void parseData(String[] result) throws JSONException {
		JSONArray jsonArray = new JSONArray(result[1]);
		JSONObject jsonObject = jsonArray.getJSONObject(1);
		JSONArray jArray = jsonObject.getJSONArray("callContent");
		
		if(0>=jArray.length()){
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.no_dail_calllog);
			return;
		}

		for (int i = 0; i < jArray.length(); i++) {
			JSONObject jObject = jArray.getJSONObject(i);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("no", (data.size() + 1) + ".");
			map.put("callnum", jObject.getString("callee"));
			map.put("startTime", jObject.getString("startTime"));
			map.put("duration", jObject.getString("hottime")
					+ getString(R.string.second));
			map.put("fee", jObject.getString("fee") + getString(R.string.money));

			data.add(map);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.more_query_calllog);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// 计算当前最后可见的条目
		lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
		fristVisItem = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& lastVisibleItem == data.size() - 1
				&& data.size() >= prePageNum) { // 如果数据不足一页 则说明已经没有更多数据

			currentPage++;

			// 如果未加载完毕
			// if (!loadCompleted) {
			ll_loading.setVisibility(View.VISIBLE);
			queryCalllog("currentCallogType", currentPage + "");
			// } else {
			// Toast.makeText(QueryCalllogActivity.this, "没有更多的数据！", 0).show();
			// }
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_calllog_back:
			finish();
			break;

		case R.id.bn_calllog_zhibo:
			if (!checkQueryDate())
				break;
			progressDialog = new MyProgressDialog(this,
					getString(R.string.qclog_cxthjl));
			progressDialog.show();
			// 清空之前的查询数据
			data.clear();
			queryCalllog("sip", "1");
			currentCallogType = "sip";

			InputMethodManager imm = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(bn_queryZhibo.getWindowToken(), 0);
			break;

		case R.id.bn_calllog_huibo:
			if (!checkQueryDate())
				break;
			progressDialog = new MyProgressDialog(this,
					getString(R.string.qclog_cxthjl));
			progressDialog.show();
			// 清空之前的查询数据
			data.clear();
			queryCalllog("callback", "1");
			currentCallogType = "callback";

			InputMethodManager imm1 = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm1.hideSoftInputFromWindow(bn_queryHuibo.getWindowToken(), 0);
			break;

		default:
			break;
		}
	}

	/**
	 * 校验输入的年月日期是否合法
	 * 
	 * @return
	 */
	private boolean checkQueryDate() {
//		year = et_year.getText().toString().trim();
//		month = et_month.getText().toString().trim();
		CalldaToast calldaToast = new CalldaToast();
		
		if ("".equals(year) || year.length() < 1) {
			calldaToast.showToast(getApplicationContext(), R.string.qclog_cxnf);
			return false;
		}
		if ("".equals(month) || month.length() < 1) {
			calldaToast.showToast(getApplicationContext(), R.string.qclog_cxyf);
			return false;
		}
		if (year.contains(".") || month.contains(".")) {
			calldaToast.showToast(getApplicationContext(), R.string.qclog_rqbhxs);
			return false;
		}
		try {
			int intYear = Integer.parseInt(year);
			if (intYear < 1 || intYear > 9999) {
				calldaToast.showToast(getApplicationContext(), R.string.qclog_srnf);
				return false;
			}

			int intMonth = Integer.parseInt(month);
			if (intMonth < 1 || intMonth > 12) {
				calldaToast.showToast(getApplicationContext(), R.string.qclog_sryf);
				return false;
			}
			if (intMonth < 10)
				month = "0" + intMonth;
		} catch (Exception e) {
			calldaToast.showToast(getApplicationContext(), R.string.qclog_srzqgs);
			return false;
		}
		return true;
	}

	/**
	 * 查询通话记录
	 * 
	 * @param callType
	 *            直拨、回拨、所有
	 * @param currentPage
	 *            当前页码
	 */
	private void queryCalllog(String callType, String currentPage) {
		Task task = new Task(Task.TASK_QUERY_CALLLOG);
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("softType", "android");
		taskParams.put("year", year);
		taskParams.put("month", month);
		taskParams.put("type", callType);
		taskParams.put("currentPage", currentPage);
		taskParams.put("pageNum", String.valueOf(prePageNum));
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.sp_calllog_year:
			year=all.get(position);
			Logger.i("onItemSelected", "year:"+year+"-----position:"+position);
			break;
		case R.id.sp_calllog_month:
			month=position+1+"";
			Logger.i("onItemSelected", "month:"+month+"-----position:"+position);
			break;

		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
//		year=nowY+"";
//		month=nowM+1+"";
		Logger.i("onNothingSelected", year);
	}
}
