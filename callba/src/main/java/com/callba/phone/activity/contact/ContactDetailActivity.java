package com.callba.phone.activity.contact;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Intents;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.util.Logger;

/**
 * 联系人详情
 * 
 * @author Administrator
 */
@ActivityFragmentInject(
		contentViewId = R.layout.contact_detail,
		toolbarTitle =R.string.lxrxq,
		navigationId = R.drawable.press_back
)
public class ContactDetailActivity extends BaseActivity implements
		OnClickListener {
	private TextView tv_contactName;
	private TextView tv_location;
	private ListView lv_phoneNums;
	private ContactMutliNumBean bean;
	private CalllogDetailBean logBean;
	private String fromFlag;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			fromFlag = getIntent().getStringExtra("activity");
			bean = (ContactMutliNumBean) getIntent()
					.getSerializableExtra("contact");
			
			if (fromFlag.equals("MainCallActivity")) {
				logBean = (CalllogDetailBean) getIntent().getSerializableExtra(
						"log");
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			finish();
		}

		
		setDatatoAdapter();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		default:
			break;
		}
	}

	/**
	 * 调用系统编辑联系人
	 */
	private void editContact() {
		String lookup_key = bean.get_id();
		
		try {
			if (TextUtils.isEmpty(lookup_key)) {
				String phoneName = bean.getDisplayName();
				Intent intent = new Intent(Intent.ACTION_INSERT);
				intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
				intent.putExtra(Intents.Insert.PHONE, phoneName);
				startActivity(intent);
			} else {
				Intent intent = new Intent(Intent.ACTION_EDIT);
				intent.setData(Uri.parse(ContactsContract.Contacts.CONTENT_LOOKUP_URI + "/"
						+ lookup_key));
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() {

		tv_contactName = (TextView) findViewById(R.id.tv_contactdetail_name);
		tv_location = (TextView) findViewById(R.id.tv_detail_location);
		
		lv_phoneNums = (ListView) findViewById(R.id.lv_phone_nums);

	}

	private void setDatatoAdapter() {
		List<CalldaCalllogBean> calldaCalllogBeans = new ArrayList<CalldaCalllogBean>();

		if (bean != null && fromFlag.equals("ContactActivity")) {
			String name = bean.getDisplayName();
			tv_contactName.setText(bean.getDisplayName());
			List<String> phoneNums = bean.getContactPhones();

			ContactDetailListAdapter adapter = new ContactDetailListAdapter(
					this, phoneNums, name);
			lv_phoneNums.setAdapter(adapter);

		} else if (logBean != null) {
			calldaCalllogBeans = logBean.getCalllogBean();
			String name = calldaCalllogBeans.get(0).getDisplayName();
			String location = calldaCalllogBeans.get(0).getLocation();
			tv_contactName.setText(name);
			tv_location.setText(location);
			Logger.v("详情归属地", location);
			MyContactDetailListAdapter adapter2 = new MyContactDetailListAdapter(
					this, calldaCalllogBeans, name);
			lv_phoneNums.setAdapter(adapter2);
		} else {
			// bean为空时，设置联系人编辑不可用

		}

	}

	@Override
	public void refresh(Object... params) {
	}
}
