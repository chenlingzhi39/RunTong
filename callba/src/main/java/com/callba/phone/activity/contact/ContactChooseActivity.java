package com.callba.phone.activity.contact;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.activity.contact.ContactChooserAdapter.ViewHolder_Item;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.contact.ContactController;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.ContactSerarchWatcher;
import com.callba.phone.view.QuickSearchBar;

public class ContactChooseActivity extends BaseActivity implements OnClickListener {
	private Button bn_back, bn_ok, bn_selectall;
	private EditText et_search;
	private Button bn_search_del;
	private ListView mListView;
	private QuickSearchBar mQuickSearchBar;
	
	private ContactChooserAdapter contactChooserAdapter;
	private List<ContactEntity> mFilterContactList; // 填充ListView的数据

	private ContactController mContactController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.choose_contact);
		super.onCreate(savedInstanceState);
		
		initView();
	}

	private void initView() {
		bn_back = (Button) findViewById(R.id.bn_choose_back);
		bn_selectall = (Button) findViewById(R.id.bn_choose_all);
		bn_search_del = (Button) findViewById(R.id.bn_search_del);
		bn_ok = (Button) findViewById(R.id.bn_choose_ok);
		bn_back.setOnClickListener(this);
		bn_ok.setOnClickListener(this);
		bn_selectall.setOnClickListener(this);
		bn_search_del.setOnClickListener(this);

		et_search = (EditText) findViewById(R.id.et_search);
		
		mListView = (ListView) findViewById(R.id.lv_choose_contacts);
		mQuickSearchBar = (QuickSearchBar) findViewById(R.id.qsb_choose_contact);
		
		mContactController = new ContactController();
		
		initListView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_choose_back:
			this.finish();
			break;

		case R.id.bn_choose_all:
			if (mFilterContactList != null) {
				if (bn_selectall.getText().equals(
						getString(R.string.cca_checkall))) {
					bn_selectall.setText(getString(R.string.cca_uncheckall));
					for (int i = 0; i < mFilterContactList.size(); i++) {
						contactChooserAdapter.isSelected.put(i, true);
					}
					contactChooserAdapter.notifyDataSetChanged();
					
				} else {
					bn_selectall.setText(getString(R.string.cca_checkall));
					for (int i = 0; i < mFilterContactList.size(); i++) {
						contactChooserAdapter.isSelected.put(i, false);
					}
					contactChooserAdapter.notifyDataSetChanged();
				}
			}
			break;

		case R.id.bn_search_del:
			et_search.setText("");
			break;

		case R.id.bn_choose_ok:
			if (GlobalConfig.getInstance().getContactBeans() != null) {
				getChoosedContact();
				finish();
			}
			break;

		default:
			break;
		}
	}

	private void initListView() {
		// 获取到的联系人数据
		List<ContactPersonEntity> personEntities;
		
		if (GlobalConfig.getInstance().getContactBeans() != null) {
			personEntities = GlobalConfig.getInstance().getContactBeans();
		} else {

			return;
		}

		mFilterContactList = mContactController.getFilterListContactEntities(personEntities);
		
		contactChooserAdapter = new ContactChooserAdapter(this, mFilterContactList);
		mListView.setAdapter(contactChooserAdapter);
		
		mQuickSearchBar.setListView(mListView);
		mQuickSearchBar.setListSearchMap(mContactController.getSearchMap());
		
		ContactSerarchWatcher searchWatcher = new ContactSerarchWatcher(contactChooserAdapter, mFilterContactList, mQuickSearchBar);
		et_search.addTextChangedListener(searchWatcher);
		et_search.setHint(String.format(getString(R.string.cca_contactcount),
				personEntities.size()));
		
		mListView.setOnItemClickListener(new MyItemClickListener());
	}

	/**
	 * 获取已选择的联系人
	 */
	private void getChoosedContact() {
		StringBuilder phoneNumbers = new StringBuilder();
		// 当前未搜索
		for (int i = 0; i < mFilterContactList.size(); i++) {
			// 跳过字母索引
			if (mFilterContactList.get(i).getType() == ContactEntity.CONTACT_TYPE_INDEX)
				continue;
			if (contactChooserAdapter.isSelected.get(i)) {
				ContactPersonEntity contactPersonEntity = (ContactPersonEntity) mFilterContactList.get(i);
				if (phoneNumbers.length() == 0) {
					phoneNumbers.append(contactPersonEntity.getPhoneNumber());
				} else {
					phoneNumbers.append(";"
							+ contactPersonEntity.getPhoneNumber());
				}
			}
		}

		Intent intent = new Intent();
		intent.putExtra("contacts", phoneNumbers.toString());
		this.setResult(RESULT_OK, intent);
	}
	
	class MyItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			ViewHolder_Item holder_Item = (ViewHolder_Item) arg1.getTag();
			CheckBox checkBox = holder_Item.cbSelected;
			// 改变CheckBox的状态
			checkBox.toggle();
			// 将CheckBox的选中状况记录下来
			contactChooserAdapter.isSelected.put(arg2, checkBox.isChecked());
		}
		
	}


	@Override
	public void refresh(Object... params) {
	}
}

