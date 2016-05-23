package com.callba.phone.activity.contact;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.util.CallUtils;
import com.callba.phone.util.DataAnalysis;

public class MyContactDetailListAdapter extends BaseAdapter {
	private Context context;
	private List<CalldaCalllogBean> bean;
	private String name;
	public final static String B_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
	private CallUtils callUtils;
	private List<? extends Map<String, ?>> data;
	public MyContactDetailListAdapter(Context context, List<CalldaCalllogBean> bean,String name) {
		super();
		this.context = context;
		this.bean = bean;
		this.name = name;
	}

	@Override
	public int getCount() {
		return bean.size();
	}

	@Override
	public Object getItem(int position) {
		return bean.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
//		View view = View
//				.inflate(context, R.layout.contact_detail_lv_item, null);
		View view = View
				.inflate(context, R.layout.contact_calllogdetail_lv_item, null);
		DataAnalysis dataAnalysis=new DataAnalysis();
		data=dataAnalysis.getData(context,bean);
		callUtils=new CallUtils();
		LinearLayout lv_item=(LinearLayout)view.findViewById(R.id.contactdetail_log_item);
		TextView tv_phoneNum = (TextView) view
				.findViewById(R.id.tv_phonelist_num);
		TextView tv_phoneType = (TextView) view
				.findViewById(R.id.tv_phonelist_type);
		TextView tv_time = (TextView) view
				.findViewById(R.id.tv_phonelist_calltime);
		
//		String type = bean.get(position).getCallLogType()+"";
//		String time = bean.get(position).getCallLogNumber();
		
		 Map<String, Object> map = (Map<String, Object>) data.get(position);
		final String phoneNum = (String)map.get("phoneNum");
		tv_phoneType.setText((String)map.get("calltype"));
		tv_phoneNum.setText((String)map.get("phoneNum"));
		tv_time.setText((String)map.get("calltime"));

//		tv_phoneNum.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String phoneNum = bean.get(position).getCallLogNumber();
//				callUtils.judgeCallMode(context, phoneNum,name);
//			}
//
//		});
		lv_item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callUtils.judgeCallMode(context, phoneNum,name);
			}
			
		});
		return view;
	}

}
