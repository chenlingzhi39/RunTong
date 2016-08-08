package com.callba.phone.activity.more;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.service.MainService;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;
import okhttp3.Request;

@ActivityFragmentInject(
		contentViewId = R.layout.more_query_calllog,
		toolbarTitle = R.string.calllog_search,
		navigationId = R.drawable.press_back
)
public class QueryCalllogActivity extends BaseActivity implements
		OnClickListener,OnDateSetListener,SwipeRefreshLayout.OnRefreshListener {
	private ListView lv_calllog;
	private ArrayAdapter<String> arrayadapter;
	// 真正的字符串数据将保存在这个list中
	private List<String> all;

	// 存储查询到的通话记录
	private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	private SimpleAdapter adapter;
    ProgressDialog progressDialog;
	private Button bt_date;
	TimePickerDialog mDialogYearMonthDay;
	private ProgressBar progressBar;
	private SwipeRefreshLayout refreshLayout;
	String time;
	private boolean first=true;
	public void init() {
        bt_date=(Button)findViewById(R.id.date);
        bt_date.setOnClickListener(this);
		lv_calllog = (ListView) findViewById(R.id.lv_calllog);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		refreshLayout=(SwipeRefreshLayout) findViewById(R.id.refresh);
		adapter = new SimpleAdapter(this, data,
				R.layout.more_query_calllog_lv_item, new String[] { "no",
						"callnum", "startTime", "duration", "fee" }, new int[] {
						R.id.tv_calllog_no, R.id.tv_calllog_phontnum,
						R.id.tv_calllog_time, R.id.tv_calllog_duration,
						R.id.tv_calllog_money });
		lv_calllog.setAdapter(adapter);
		Date date=new Date();
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		time=format.format(date);
		bt_date.setText(time);
        refreshLayout.setOnRefreshListener(this);
		refreshLayout.setColorSchemeResources(R.color.orange);
		mDialogYearMonthDay = new TimePickerDialog.Builder()
				.setType(Type.YEAR_MONTH_DAY)
				.setCallBack(this)
				.build();
		queryCalllog(time);

	}

	@Override
	public void onRefresh() {

		queryCalllog(time);
	}

	@Override
	public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
		time=simpleDateFormat.format(new Date(millseconds));
		bt_date.setText(time);
		data.clear();
		queryCalllog(time);

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
			toast(R.string.no_dail_calllog);
			/*CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(), R.string.no_dail_calllog);*/
			return;
		}
        data.clear();
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
		super.onCreate(savedInstanceState);
		init();
	}

	/*@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// 计算当前最后可见的条目
		lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
		fristVisItem = firstVisibleItem;
	}*/

/*	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& lastVisibleItem == data.size() - 1
				&& data.size() >= prePageNum) { // 如果数据不足一页 则说明已经没有更多数据

			currentPage++;

			// 如果未加载完毕
			// if (!loadCompleted) {
			ll_loading.setVisibility(View.VISIBLE);
			/queryCalllog("currentCallogType", currentPage + "");
			// } else {
			// Toast.makeText(QueryCalllogActivity.this, "没有更多的数据！", 0).show();
			// }
		}
	}*/

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.date:
				mDialogYearMonthDay.show(getSupportFragmentManager(), "year_month_day");
				break;
		}
	}


	/**
	 * 查询通话记录
	 */
	private void queryCalllog(String date) {
		if(first){
        progressBar.setVisibility(View.VISIBLE);
		first=false;}else refreshLayout.setRefreshing(true);
		OkHttpUtils.post().url(Interfaces.QUERY_CALLLOG)
				.addParams("loginName", getUsername())
				.addParams("loginPwd", getPassword())
				.addParams("date",date)
				.build().execute(new StringCallback() {
			@Override
			public void onAfter(int id) {
				progressBar.setVisibility(View.GONE);
				refreshLayout.setVisibility(View.VISIBLE);
				refreshLayout.setRefreshing(false);
			}

			@Override
			public void onBefore(Request request, int id) {

			}

			@Override
			public void onError(Call call, Exception e, int id) {
				toast(R.string.getserverdata_failed);
			}

			@Override
			public void onResponse(String response, int id) {
				try {
					Logger.i("callog_result",response);
					String[] result = response.split("\\|");

					if ("0".equals(result[0])) {
						// 成功fanhui数据
						Logger.i("查询话单通话记录", response);
						parseData(result);
					} else if ("1".equals(result[0])) {
						toast(result[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					toast(R.string.getserverdata_exception);
				}
			}
		});
	}


}
