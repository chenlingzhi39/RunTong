package com.callba.phone.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.activity.contact.ContactActivity2;
import com.callba.phone.activity.contact.ContactDetailActivity;
import com.callba.phone.activity.contact.ContactDetailActivity2;
import com.callba.phone.activity.contact.ContactMutliNumBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.util.Logger;

/**
 * 通话记录 list 适配器
 * 
 * @author Administrator
 */
public class CalllogListAdapter extends BaseAdapter {
	private Context context;
	private List<? extends Map<String, ?>> data;
	private List<CalllogDetailBean> calldaCalllogBeans;
	/**
	 * 
	 * @param context
	 * @param list  合并解析三种图片的通话记录
	 * @param bean 分组合并后的所有通话记录
	 */
	public CalllogListAdapter(Context context,
			List<? extends Map<String, ?>> list,List<CalllogDetailBean>bean) {
		super();
		this.context = context;
		this.data = list;
		this.calldaCalllogBeans = bean;
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
		View view = View.inflate(context, R.layout.call_log_lv_item, null);

		ImageView iv_type = (ImageView) view.findViewById(R.id.iv_type);
		TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
		TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
		TextView tv_calltime = (TextView) view.findViewById(R.id.tv_calltime);
		TextView tv_location = (TextView) view.findViewById(R.id.tv_location);
		ImageView iv_detail = (ImageView) view
				.findViewById(R.id.iv_contactdetail);

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) data.get(position);
		int occurrencenumber = (Integer) map.get("occurrencenumber");//重复的次数

		iv_type.setImageResource((Integer) map.get("calltype"));
		
		tv_number.setText((String) map.get("phoneNum"));
		tv_calltime.setText((String) map.get("calltime"));
		tv_location.setText((String) map.get("phoneLocation"));
		Logger.v("CalllogListAdapter", (String) map.get("phoneLocation"));
		if (occurrencenumber > 1) {
			tv_name.setText((String) map.get("name") + "  (" + occurrencenumber + ")");
		}else{
			tv_name.setText((String) map.get("name"));
		}
		iv_detail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContactMutliNumBean contactMutliNumBean = new ContactMutliNumBean();
				List<String> contactPhones = new ArrayList<String>();

				String contactName = (String) data.get(position).get("name");
				String contactNumber = (String) data.get(position).get(
						"phoneNum");
				String location = (String) data.get(position).get(
						"phoneLocation");
				contactMutliNumBean.setDisplayName(contactName);
				contactMutliNumBean.setLocation(location);

				if (contactNumber.equals(contactName)
						|| CalldaGlobalConfig.getInstance().getContactBeans() == null) {
					contactPhones.add(contactNumber);
					contactMutliNumBean.setContactPhones(contactPhones);
				} else { // 有姓名
					if (CalldaGlobalConfig.getInstance().getContactBeans() != null) {
						for (ContactPersonEntity bean : CalldaGlobalConfig
								.getInstance().getContactBeans()) {
							if (contactName.equals(bean.getDisplayName())) {
								contactPhones.add(bean.getPhoneNumber());
								contactMutliNumBean.set_id(bean.get_id());
							}
						}
					}
					contactMutliNumBean.setContactPhones(contactPhones);
				}
				
			   CalllogDetailBean currentbean=calldaCalllogBeans.get(position);

				Intent intent = new Intent(context, ContactDetailActivity2.class);
				intent.putExtra("contact", contactMutliNumBean);
//				intent.putParcelableArrayListExtra("", currentbean);
				intent.putExtra("log", currentbean);
				intent.putExtra("activity", "MainCallActivity");
				context.startActivity(intent);
			}
		});

		return view;
	}
}
