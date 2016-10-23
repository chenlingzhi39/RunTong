package com.callba.phone.service;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.QuickQueryContactBean;
import com.callba.phone.logic.contact.ContactPersonEntity;

import java.util.List;
/**
 * 拨号界面 号码比配List Adapter
 * @author Zhang
 *
 */
public class DialCallListAdapter extends BaseAdapter {
	private List<QuickQueryContactBean> data;
	private Context context;
	
	public DialCallListAdapter(Context context, List<QuickQueryContactBean> data) {
		this.context = context;
		this.data = data;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactPersonEntity bean = data.get(position);
		
		ViewHolder viewHolder = null;
		if(convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(context, R.layout.phone_filter_lv_item, null);
			viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			viewHolder.tv_phone = (TextView) convertView.findViewById(R.id.tv_number);
			viewHolder.tv_Pinyin = (TextView) convertView.findViewById(R.id.tv_pinyin);
			
			convertView.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) convertView.getTag();
		String showDisplayName = bean.getShowDisplayName();
		if(showDisplayName == null) {
			showDisplayName = bean.getDisplayName();
		}
		viewHolder.tv_name.setText(Html.fromHtml(showDisplayName));
		viewHolder.tv_phone.setText(Html.fromHtml(bean.getShowPhoneNumber()));
		viewHolder.tv_Pinyin.setText(Html.fromHtml(bean.getShowSortPinYin()+""));
		
		return convertView;
	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_phone;
		TextView tv_Pinyin;
	}
}
