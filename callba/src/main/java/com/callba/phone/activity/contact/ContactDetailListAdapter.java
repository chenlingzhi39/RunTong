package com.callba.phone.activity.contact;

import java.util.List;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.util.CallUtils;

/**
 * 联系人详情 List Adapter
 * 
 * @author Administrator
 * 
 */
public class ContactDetailListAdapter extends BaseAdapter {
	private Context context;
	private List<String> phones;
	private String name;
	public final static String B_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
	private CallUtils callUtils;
	public ContactDetailListAdapter(Context context, List<String> phones,String name) {
		super();
		this.context = context;
		this.phones = phones;
		this.name = name;
	}

	@Override
	public int getCount() {
		return phones.size();
	}

	@Override
	public Object getItem(int position) {
		return phones.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = View
				.inflate(context, R.layout.contact_detail_lv_item, null);
		
		callUtils=new CallUtils();
		ImageView iv_phone = (ImageView) view
				.findViewById(R.id.iv_phonelist_call);
		TextView tv_phoneNum = (TextView) view
				.findViewById(R.id.tv_phonelist_number);
		// ImageView iv_sms = (ImageView)
		// view.findViewById(R.id.iv_phonelist_sms);
		String phoneNum = phones.get(position);
		// 查询到该号码的所有通话记录
		tv_phoneNum.setText(phoneNum);

		tv_phoneNum.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phoneNum = phones.get(position);
				callUtils.judgeCallMode(context, phoneNum,name);
			}

		});
		// iv_sms.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// String phoneNum = phones.get(position);
		// Intent intent = new Intent(context, NewSMSActivity.class);
		// intent.putExtra("phoneNumber", phoneNum);
		// context.startActivity(intent);
		// }
		// });
		return view;
	}
}
