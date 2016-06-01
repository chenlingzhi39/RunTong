package com.callba.phone.activity.contact;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.logic.contact.ContactController;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactListAdapter;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.ContactSerarchWatcher;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.view.QuickSearchBar;
@ActivityFragmentInject(
		contentViewId = R.layout.tab_contact,
		toolbarTitle=R.string.list,
		menuId=R.menu.menu_contact
)
public class ContactActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener,OnItemLongClickListener {
	
	private ImageButton ibn_backup, ibn_add;
	private EditText et_search; // 联系人搜索框
	
	//联系人界面整体
	private LinearLayout ll_tab_contact;
	private ListView mListView; // 联系人列表
	
	private String flagFrom; 
	
	private List<ContactEntity> mContactListData; // 填充ListView的数据
	private QuickSearchBar mQuickSearchBar;	//侧边快速检索控件
	private ContactListAdapter mContactListAdapter;	//联系人适配器
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initContactListView();
	}

	@Override
	public void init() {
		mListView = (ListView) findViewById(R.id.lv_contact_contacts);
		mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
		ll_tab_contact = (LinearLayout) findViewById(R.id.tab_contact_ll);

		ibn_add = (ImageButton) this.findViewById(R.id.ibn_contact_add);
		ibn_backup = (ImageButton) this.findViewById(R.id.ibn_contact_backup);
		ibn_add.setOnClickListener(this);
		ibn_backup.setOnClickListener(this);

		et_search = (EditText) findViewById(R.id.et_contact_search);
		
		mQuickSearchBar = (QuickSearchBar) findViewById(R.id.qsb_contact);
		
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		flagFrom = (String) bundle.get("frompage");
		if ("CallingActivity".equals(flagFrom)) {
			FrameLayout.LayoutParams lp = (LayoutParams) ll_tab_contact
					.getLayoutParams();
			lp.setMargins(0, 0, 0, 0);
		}
	}

	@Override
	public void refresh(Object... params) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibn_contact_add:
			// 调用系统自带应用添加联系人
			Intent intent = new Intent(Intent.ACTION_INSERT);
			intent.setType("vnd.android.cursor.dir/person");
			intent.setType("vnd.android.cursor.dir/contact");
			intent.setType("vnd.android.cursor.dir/raw_contact");
			if(!isIntentAvailable(this, intent)) {
	            return;
	        }else{
	        	startActivity(intent);
	        }
			break;

		case R.id.ibn_contact_backup:
			Intent intent_add = new Intent(this, ContactBackupActivity.class);
			startActivity(intent_add);
			break;

		default:
			break;
		}
	}

	/**
	 * 初始化listview数据
	 */
	private void initContactListView() {
		ContactController contactController = new ContactController();
		List<ContactEntity> allContactEntities = contactController.getFilterListContactEntitiesNoDuplicate();
		if(mContactListData == null) {
			mContactListData = new ArrayList<ContactEntity>();
		}
		mContactListData.clear();
		mContactListData.addAll(allContactEntities);
		
		mContactListAdapter = new ContactListAdapter(this, mContactListData);
		mListView.setAdapter(mContactListAdapter);
		
		mQuickSearchBar.setListView(mListView);
		mQuickSearchBar.setListSearchMap(contactController.getSearchMap());
		
		et_search.addTextChangedListener(new ContactSerarchWatcher(
				mContactListAdapter, mContactListData, mQuickSearchBar));
	}
	
	/**
	 * 重写onkeyDown 捕捉返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ("CallingActivity".equals(flagFrom)) {
				//关闭当前页面
				finish();
				return true;
			}
			//转到后台运行
			ActivityUtil.moveAllActivityToBack();
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
//		ContactEntity contactEntity = mContactListData.get(position);
//		if (contactEntity.getType() == ContactEntity.CONTACT_TYPE_INDEX) {
//			return;
//		}
//		
//		ContactPersonEntity contactPersonEntity = (ContactPersonEntity) contactEntity;
//		String name = contactPersonEntity.getDisplayName();
//		
//		ContactMutliNumBean contactMutliNumBean = new ContactMutliNumBean();
//		List<String> contactPhones = new ArrayList<String>();
//		contactMutliNumBean.set_id(contactPersonEntity.get_id());
//		contactMutliNumBean.setContactName(name);
//		for (ContactEntity bean1 : mContactListData) {
//			if (bean1.getType() == ContactEntity.CONTACT_TYPE_INDEX) {
//				continue;
//			}
//			ContactPersonEntity contactEntity1 = (ContactPersonEntity) bean1;
//			if (name.equals(contactEntity1.getDisplayName())) {
//				contactPhones.add(contactEntity1.getPhoneNumber());
//			}
//		}
//		contactMutliNumBean.setContactPhones(contactPhones);
		
		ContactEntity contactEntity = mContactListData.get(position);
		ContactPersonEntity contactPersonEntity = (ContactPersonEntity)contactEntity;
		ContactMutliNumBean contactMutliNumBean = (ContactMutliNumBean)contactPersonEntity;
		
		Intent intent = new Intent(this, ContactDetailActivity.class);
		intent.putExtra("contact", contactMutliNumBean);
		intent.putExtra("activity", "ContactActivity");
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		Dialog dialog=new AlertDialog.Builder(this).setTitle("是否邀请此好友？").setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContactPersonEntity contactPersonEntity=(ContactPersonEntity)mContactListData.get(position);
				Uri smsToUri = Uri.parse("smsto://" + contactPersonEntity.getPhoneNumber());
				Intent mIntent = new Intent(android.content.Intent.ACTION_SENDTO, smsToUri);
				mIntent.putExtra("sms_body", "我是"+ CalldaGlobalConfig.getInstance().getNickname()+"，我正在使用CALL吧！ CALL吧“0月租”“0漫游”“通话不计分钟”，赶快加入我们吧！");
				startActivity(mIntent);
				dialog.dismiss();
			}
		}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();
		dialog.show();
		return false;
	}
   /*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()){
	    case R.id.add:
			Intent intent = new Intent(Intent.ACTION_INSERT);
			intent.setType("vnd.android.cursor.dir/person");
			intent.setType("vnd.android.cursor.dir/contact");
			intent.setType("vnd.android.cursor.dir/raw_contact");
			if(!isIntentAvailable(this, intent)) {
				return true;
			}else{
				startActivity(intent);
			}
		break;
      }
		return super.onOptionsItemSelected(item);
	}
}
