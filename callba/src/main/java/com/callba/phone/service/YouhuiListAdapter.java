package com.callba.phone.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.activity.more.PreferentialActivity;
import com.callba.phone.activity.more.ShareActivity;
import com.callba.phone.activity.recharge.RechargeWayActivity;

/**
 * @author zhanghw
 * @version 创建时间：2013-9-26 下午2:39:17
 */
public class YouhuiListAdapter extends BaseAdapter {
	private List<String> data;
	private Context context;
	
	public YouhuiListAdapter(Context context) {
		this.context = context;
		data = new ArrayList<String>();
		data.add(context.getString(R.string.yllist_info1));
		data.add(context.getString(R.string.yllist_info2));
		data.add(context.getString(R.string.yllist_info3));
	}
	
	@Override
	public int getCount() {
		return data.size();

	}

	@Override
	public Object getItem(int position) {
		return data.get(position);

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = View.inflate(context, R.layout.dial_lv_youhui_item, null);
		TextView  tv_info = (TextView) view.findViewById(R.id.tv_youhui_info);
		Button bn_look = (Button) view.findViewById(R.id.tv_youhui_chakan);
		tv_info.setText(data.get(position));
		bn_look.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(position == 0) {
					//充值
					Intent intent = new Intent(context, RechargeWayActivity.class);
					intent.putExtra("isTabPage", false);
					context.startActivity(intent);
				}else if(position == 1) {
					//最新优惠
					gotoActivity(PreferentialActivity.class);
				}else if(position == 2) {
					//分享有礼
					gotoActivity(ShareActivity.class);
				}
			}
		});
		
		return view;
	}
	
	private void gotoActivity(Class<?> clazz) {
		Intent intent = new Intent(context, clazz);
		context.startActivity(intent);
	}

}
