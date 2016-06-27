package com.callba.phone.activity.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.bean.Task;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.service.MainService;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.util.PinYinUtil;
import com.callba.phone.view.CalldaToast;
import com.callba.phone.view.MyDialog;
import com.callba.phone.view.MyProgressDialog;

public class NewContactBackupActivity extends BaseActivity implements
		OnClickListener {
	private Button bn_back;
	private TextView tv_name, tv_localCount, tv_netcontact;
	private LinearLayout ll_backup, ll_look, ll_download, ll_laytou_net;

	private MyProgressDialog mProgressDialog;
	private ActivityUtil activityUtil = new ActivityUtil();
	private String lan = "";
	private boolean myDialogFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.contact_backup);
		lan = activityUtil.language(this);
		bn_back = (Button) this.findViewById(R.id.back);
		bn_back.setOnClickListener(this);

		tv_name = (TextView) this.findViewById(R.id.user_name);
		tv_name.setText(CalldaGlobalConfig.getInstance().getUsername());
		tv_localCount = (TextView) this.findViewById(R.id.local_contact);

		if (CalldaGlobalConfig.getInstance().getContactBeans() != null)
			tv_localCount.setText(CalldaGlobalConfig.getInstance()
					.getContactBeans().size()
					+ "");

		tv_netcontact = (TextView) findViewById(R.id.net_contact);
		ll_laytou_net = (LinearLayout) findViewById(R.id.laytou_net);

		ll_backup = (LinearLayout) findViewById(R.id.contact_beifeng);
		ll_look = (LinearLayout) findViewById(R.id.contact_look);
		ll_download = (LinearLayout) findViewById(R.id.contact_download);
		ll_backup.setOnClickListener(this);
		ll_look.setOnClickListener(this);
		ll_download.setOnClickListener(this);

		getRemoteContactCount();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;

		case R.id.contact_beifeng:
			if (!myDialogFlag) {
				myDialogFlag = true;
				MyDialog.showDialog(this, getString(R.string.backup_info),
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								switch (v.getId()) {
								case R.id.bn_ok:
									backupContacts();
									MyDialog.dismissDialog();
									break;
								case R.id.bn_cancel:
									MyDialog.dismissDialog();
									break;
								default:
									break;
								}
								myDialogFlag = false;
							}
						});
			}
			break;

		case R.id.contact_look:
			Intent intent = new Intent(this, RemoteContactsActvity.class);
			startActivity(intent);
			break;

		case R.id.contact_download:
			if (!myDialogFlag) {
				myDialogFlag = true;
				MyDialog.showDialog(this,
						getString(R.string.download_contact_tip),
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								switch (v.getId()) {
								case R.id.bn_ok:
									MyDialog.dismissDialog();
									lookRemoteContacts();
									mProgressDialog = new MyProgressDialog(
											NewContactBackupActivity.this,
											getString(R.string.gettingremotedata));
									mProgressDialog.show();
									break;
								case R.id.bn_cancel:
									MyDialog.dismissDialog();
									break;
								default:
									break;
								}
								myDialogFlag = false;
							}
						});
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 获取云端联系人个数
	 */
	private void getRemoteContactCount() {
		Task task = new Task(Task.TASK_GET_CONTACT_COUNT);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance()
				.getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance()
				.getPassword());
		taskParams.put("softType", "android");
		taskParams.put("lan", lan);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	@Override
	public void refresh(Object... params) {
		Message msg = (Message) params[0];
		if (msg.what == Task.TASK_GET_CONTACT_COUNT) {
			// 获取云端联系人个数
			if (msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					String[] content = result.split("\\|");
					if ("0".equals(content[0])) {
						String count = content[1];
						tv_netcontact.setText(count);
						ll_laytou_net.setVisibility(View.VISIBLE);
					} else {
						// Toast.makeText(this, content[1], 0).show();
					}
				} catch (Exception e) {
					// Toast.makeText(this, "解析服务端数据失败！", 0).show();
					e.printStackTrace();
				}
			}
			// else {
			// Toast.makeText(this, "获取云端联系人个数失败！", 0).show();
			// }
		} else if (msg.what == Task.TASK_BACKUP_CONTACT) {
			// 备份联系人
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					String[] content = result.split("\\|");
					Logger.i("上传手机通讯录结果", result);
					if ("0".equals(content[0])) {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(),
								R.string.backup_success);
					} else {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(),
								content[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(),
							R.string.server_error);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(),
						R.string.backup_failed);
			}
		} else if (msg.what == Task.TASK_LOOK_REMOTE_CONTACT) {
			// 还原
			if (msg.arg1 == Task.TASK_SUCCESS) {
				try {
					String result = (String) msg.obj;
					String[] content = result.split("\\|");
					if ("0".equals(content[0])) {
						String jsonSource = content[1];
						restore2Loacal(jsonSource);
					} else {
						CalldaToast calldaToast = new CalldaToast();
						calldaToast.showToast(getApplicationContext(),
								content[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
					CalldaToast calldaToast = new CalldaToast();
					calldaToast.showToast(getApplicationContext(),
							R.string.server_error);
				}
			} else {
				CalldaToast calldaToast = new CalldaToast();
				calldaToast.showToast(getApplicationContext(),
						R.string.download_remote_failed);
			}
		}
	}

	/**
	 * 处理返回结果 还原至本地
	 * 
	 * @param jsonSource
	 * @throws JSONException
	 */
	private void restore2Loacal(String jsonSource) throws Exception {
		final List<ContactPersonEntity> remoteContacts = parseResult(jsonSource);

		// 增加判断，如果获取到服务器上的联系人为空，则不还原
		if (remoteContacts.isEmpty()) {
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(),
					R.string.restore_failed_empty);

		} else {
			new Thread() {
				public void run() {
					try {
//						BatchAddContact(remoteContacts);
						 restoreContacts(remoteContacts);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	/**
	 * 解析返回的JSON数据
	 * 
	 * @param jsonSource
	 * @throws JSONException
	 */
	private List<ContactPersonEntity> parseResult(String jsonSource)
			throws JSONException {
		JSONArray jsonArray = new JSONArray(jsonSource);
		List<ContactPersonEntity> remoteContacts = new ArrayList<ContactPersonEntity>();
		for (int i = 0; i < jsonArray.length(); i++) {
			ContactPersonEntity bean = new ContactPersonEntity();
			JSONObject json = jsonArray.getJSONObject(i);
			String name = json.getString("name");
			bean.setDisplayName(name);
			bean.setPhoneNumber(json.getString("callee"));

			// 获取首字母英文
			if (name != null && !"".equals(name)) {
				String indexWorld = PinYinUtil.converterToFirstSpell(
						name.trim().charAt(0) + "").toUpperCase();
				bean.setTypeName(indexWorld);
			}

			remoteContacts.add(bean);
		}
		mProgressDialog
				.setProgressMessage(getString(R.string.backcallingcontact));

		return remoteContacts;
	}

	/**
	 * 发送获取云端联系人任务
	 */
	private void lookRemoteContacts() {
		Task task = new Task(Task.TASK_LOOK_REMOTE_CONTACT);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance()
				.getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance()
				.getPassword());
		taskParams.put("type", 1);
		taskParams.put("softType", "android");
		taskParams.put("lan", lan);
		task.setTaskParams(taskParams);

		MainService.newTask(task);
	}

	/**
	 * 备份联系人
	 */
	private void backupContacts() {
		if (CalldaGlobalConfig.getInstance().getContactBeans().size() < 1) {
			CalldaToast calldaToast = new CalldaToast();
			calldaToast.showToast(getApplicationContext(),
					R.string.contactempty);
			return;
		}

		StringBuilder contacts = new StringBuilder();
		for (ContactPersonEntity bean : CalldaGlobalConfig.getInstance()
				.getContactBeans()) {
			if (TextUtils.isEmpty(contacts)) {
				contacts.append(bean.getDisplayName() + ":"
						+ bean.getPhoneNumber());
			} else {
				String disname = bean.getDisplayName();
				String phoneNumber = bean.getPhoneNumber();

				if (!TextUtils.isEmpty(disname)) {
					disname = disname.replaceAll(" ", "");
				}
				if (!TextUtils.isEmpty(phoneNumber)) {
					phoneNumber = phoneNumber.replaceAll(" ", "");
				}
				contacts.append("," + disname + ":" + phoneNumber);
			}
		}

		Task task = new Task(Task.TASK_BACKUP_CONTACT);
		Map<String, Object> taskParams = new HashMap<String, Object>();
		taskParams.put("loginName", CalldaGlobalConfig.getInstance()
				.getUsername());
		taskParams.put("loginPwd", CalldaGlobalConfig.getInstance()
				.getPassword());
		taskParams.put("name_callee", contacts.toString());
		taskParams.put("softType", "android");
		taskParams.put("lan", lan);
		task.setTaskParams(taskParams);

		MainService.newTask(task);

		mProgressDialog = new MyProgressDialog(this,
				getString(R.string.zzbflxr));
		mProgressDialog.show();
	}

	/**
	 * 还原联系人
	 */
	private void restoreContacts(List<ContactPersonEntity> remoteContacts) {
		// 删除本地数据
		NewContactBackupActivity.this.getContentResolver().delete(
				Uri.parse(ContactsContract.RawContacts.CONTENT_URI.toString()
						+ "?" + ContactsContract.CALLER_IS_SYNCADAPTER
						+ "=true"), ContactsContract.RawContacts._ID + ">0",
				null);
		for (int i = 0; i < remoteContacts.size(); i++) {
			ContentValues values = new ContentValues();
			// 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
			Uri rawContactUri = NewContactBackupActivity.this
					.getContentResolver().insert(RawContacts.CONTENT_URI,
							values);
			long rawContactId = ContentUris.parseId(rawContactUri);

			// 往data表入姓名数据
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
			values.put(StructuredName.GIVEN_NAME, remoteContacts.get(i)
					.getDisplayName());
			NewContactBackupActivity.this.getContentResolver().insert(
					android.provider.ContactsContract.Data.CONTENT_URI, values);

			// 往data表入电话数据
			values.clear();
			values.put(
					android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
					rawContactId);
			values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
			values.put(Phone.NUMBER, remoteContacts.get(i).getPhoneNumber());
			values.put(Phone.TYPE, Phone.TYPE_MOBILE);
			NewContactBackupActivity.this.getContentResolver().insert(
					android.provider.ContactsContract.Data.CONTENT_URI, values);
		}
		try {
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 批量添加通讯录
	 * 
	 * @throws OperationApplicationException
	 * @throws RemoteException
	 */
	@SuppressWarnings("unused")
	private void BatchAddContact(List<ContactPersonEntity> list)
			throws RemoteException, OperationApplicationException {
		// GlobalConstants.PrintLog_D("[GlobalVariables->]BatchAddContact begin");
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = 0;
		for (ContactPersonEntity contact : list) {
			rawContactInsertIndex = ops.size(); // 有了它才能给真正的实现批量添加

			ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
					.withValue(RawContacts.ACCOUNT_TYPE, null)
					.withValue(RawContacts.ACCOUNT_NAME, null)
					.withYieldAllowed(true).build());

			// 添加姓名
			ops.add(ContentProviderOperation
					.newInsert(
							android.provider.ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID,
							rawContactInsertIndex)
					.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
					.withValue(StructuredName.DISPLAY_NAME,
							contact.getDisplayName()).withYieldAllowed(true)
					.build());
			// 添加号码
			ops.add(ContentProviderOperation
					.newInsert(
							android.provider.ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID,
							rawContactInsertIndex)
					.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, contact.getPhoneNumber())
					.withValue(Phone.TYPE, Phone.TYPE_MOBILE)
					.withValue(Phone.LABEL, "").withYieldAllowed(true).build());
		}
		if (ops != null) {
			// 真正添加
			// ContentProviderResult[] results = mContext.getContentResolver()
			// .applyBatch(ContactsContract.AUTHORITY, ops);
			ContentProviderResult[] results = this.getContentResolver()
					.applyBatch(ContactsContract.AUTHORITY, ops);
		}
		try {

			mProgressDialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
