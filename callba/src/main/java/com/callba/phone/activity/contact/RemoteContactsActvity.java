package com.callba.phone.activity.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.logic.contact.ContactController;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.ContactSerarchWatcher;
import com.callba.phone.service.MainService;
import com.callba.phone.util.PinYinUtil;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyProgressDialog;
import com.callba.phone.view.QuickSearchBar;

public class RemoteContactsActvity extends BaseActivity implements
		OnClickListener {
	private Button bn_back, bn_refresh;
	private EditText et_search;
	private Button bn_clear;
	private ListView contactListView;
	private QuickSearchBar mQuickSearchBar;
	
	private RemoteContactsListAdapter mContactsListAdapter;
	private ContactController mContactController;
	private ContactSerarchWatcher contactSerarchWatcher;

	private List<ContactPersonEntity> remoteContacts;// 服务端返回的联系人数据
	private List<ContactEntity> mContactList; // 格式化后的联系人数据


	private MyProgressDialog mProgressDialog;

	/**
	 * 获取远程联系人
	 */
	private void getRemoteContacts() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		Task task = new Task(Task.TASK_LOOK_REMOTE_CONTACT);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance().getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance().getPassword());
		taskParams.put("type", 1);
		taskParams.put("softType", "android");
		taskParams.put("lan", language);
		task.setTaskParams(taskParams);
		
		MainService.newTask(task);
		
		mProgressDialog = new MyProgressDialog(this, getString(R.string.getting_data));
		mProgressDialog.show();
	}
	
	@Override
	public void refresh(Object... params) {
		if(mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

		Message msg = (Message) params[0];
		if (msg.what == Task.TASK_LOOK_REMOTE_CONTACT) {
			// 获取云端联系人
			if (msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					String[] content = result.split("\\|");
					if ("0".equals(content[0])) {
						String jsonSource = content[1];
						parseResult(jsonSource);
					} else {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(), content[1]);
					}
				} catch (Exception e) {
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(), R.string.parseremotedatafaild);
					e.printStackTrace();
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(), R.string.getremotecount);
			}
		}
	}

	private void initListSearch() {
		mQuickSearchBar.setListView(contactListView);
		mQuickSearchBar.setListSearchMap(mContactController.getSearchMap());
	}
	
	/**
	 * 解析返回的JSON数据
	 * 
	 * @param jsonSource
	 * @throws JSONException
	 */
	private void parseResult(String jsonSource) throws JSONException {
		if(remoteContacts == null) {
			remoteContacts = new ArrayList<ContactPersonEntity>();
		}
		remoteContacts.clear();
		mContactList.clear();
		
		JSONArray jsonArray = new JSONArray(jsonSource);
		for (int i = 0; i < jsonArray.length(); i++) {
			ContactPersonEntity bean = new ContactPersonEntity();
			JSONObject json = jsonArray.getJSONObject(i);
			String name = json.getString("name");
			bean.setDisplayName(name);
			bean.setPhoneNumber(json.getString("callee"));

			// 获取首字母英文
			if (name != null && !"".equals(name)) {
				String indexWorld = "#";
				try {
					indexWorld = PinYinUtil.converterToFirstSpell(
							name.trim().charAt(0) + "").substring(0, 1).toUpperCase();
				} catch (Exception e) {
					e.printStackTrace();
				}
				bean.setTypeName(indexWorld);
			} else {
				bean.setTypeName("#");
			}
			
			remoteContacts.add(bean);
		}
		
		List<ContactEntity> contactEntities = mContactController.getFilterListContactEntities(remoteContacts);
		mContactList.addAll(contactEntities);
		
		initListSearch();
		
		et_search.setHint(String.format(getString(R.string.contactcount), remoteContacts.size()));
		contactListView.setAdapter(mContactsListAdapter);
		contactListView.setOnItemClickListener(new MyitemClickListener());

		contactSerarchWatcher = new ContactSerarchWatcher(mContactsListAdapter, mContactList, mQuickSearchBar);
		et_search.addTextChangedListener(contactSerarchWatcher);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.look_remote_contact);
		bn_back = (Button) findViewById(R.id.bn_remotecontact_back);
		bn_refresh = (Button) findViewById(R.id.bn_remotecontact_refresh);
		bn_clear = (Button) findViewById(R.id.bn_search_del);
		bn_back.setOnClickListener(this);
		bn_refresh.setOnClickListener(this);
		bn_clear.setOnClickListener(this);

		mQuickSearchBar = (QuickSearchBar) findViewById(R.id.qsb_remote_contact);

		et_search = (EditText) findViewById(R.id.et_search);
		contactListView = (ListView) findViewById(R.id.lv_remotecontact_contact);

		mContactController = new ContactController();

		mContactList = new ArrayList<ContactEntity>();
		mContactsListAdapter = new RemoteContactsListAdapter(this, mContactList);

		getRemoteContacts();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_search_del:
			et_search.setText("");
			break;

		case R.id.bn_remotecontact_back:
			finish();
			break;

		case R.id.bn_remotecontact_refresh:
			//刷新远程联系人
			getRemoteContacts();
			et_search.setText("");
			break;

		default:
			break;
		}
	}


	/**
	 * ListView onclick监听
	 */
	class MyitemClickListener implements OnItemClickListener {
		String[] str1 = new String[] { getString(R.string.makecall), getString(R.string.sendsms), getString(R.string.addsuperbackcall)};

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
//			ContactEntity bean = mContactList.get(position);
//			if (bean.getType() == ContactPersonEntity.CONTACT_TYPE_CONTACT) {
//				AlertDialog dialog = new AlertDialog.Builder(
//						RemoteContactsActvity.this).setSingleChoiceItems(str1,
//						0, new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								//TODO ...
//								Toast.makeText(getApplicationContext(),
//										str1[which], 0).show();
//							}
//						}).create();
//				
//				dialog.show();
//			}
		}
	}
}
