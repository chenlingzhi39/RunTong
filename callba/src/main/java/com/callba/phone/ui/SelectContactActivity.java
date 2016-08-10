package com.callba.phone.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.callba.R;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.bean.ContactMutliNumBean;
import com.callba.phone.ui.adapter.NumberAdapter;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.logic.contact.ContactController;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactListAdapter;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.ContactSerarchWatcher;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.view.QuickSearchBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PC-20160514 on 2016/6/20.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.tab_contact,
        toolbarTitle=R.string.select_contact,
        navigationId = R.drawable.press_back
)
public class SelectContactActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    ;
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
        init();
        initContactListView();
    }


    public void init() {
        mListView = (ListView) findViewById(R.id.lv_contact_contacts);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        ll_tab_contact = (LinearLayout) findViewById(R.id.tab_contact_ll);


        et_search = (EditText) findViewById(R.id.et_contact_search);

        mQuickSearchBar = (QuickSearchBar) findViewById(R.id.qsb_contact);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        flagFrom = (String) bundle.get("frompage");
        if ("CallingActivity".equals(flagFrom)) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) ll_tab_contact
                    .getLayoutParams();
            lp.setMargins(0, 0, 0, 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
	/*	case R.id.ibn_contact_add:
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
*/
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
     if(contactMutliNumBean.getContactPhones().size()==1){
         Intent intent=new Intent();
         intent.putExtra("number",contactMutliNumBean.getContactPhones().get(0));
         setResult(RESULT_OK,intent);
     }
        else{
         showDialog((ArrayList<String>)contactMutliNumBean.getContactPhones());
     }
    }
    public class DialogHelper implements DialogInterface.OnDismissListener {
        private Dialog mDialog;
        private View mView;
        private RecyclerView mealList;
        private NumberAdapter numberAdapter;

        public DialogHelper(ArrayList<String> numbers) {
            mView = getLayoutInflater().inflate(R.layout.dialog_meal, null);
            mealList = (RecyclerView) mView.findViewById(R.id.meal_list);
            mealList.setLayoutManager(new LinearLayoutManager(SelectContactActivity.this));
            numberAdapter = new NumberAdapter(SelectContactActivity.this);
            numberAdapter.addAll(numbers);
            numberAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Intent intent=new Intent();
                    ContactEntity contactEntity = mContactListData.get(position);
                    ContactPersonEntity contactPersonEntity = (ContactPersonEntity)contactEntity;
                    ContactMutliNumBean contactMutliNumBean = (ContactMutliNumBean)contactPersonEntity;
                    intent.putExtra("number",contactMutliNumBean.getContactPhones().get(position));
                    setResult(RESULT_OK,intent);
                }
            });
            mealList.setAdapter(numberAdapter);
        }


        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            mDialog = null;
        }

        public void setDialog(Dialog mDialog) {
            this.mDialog = mDialog;
        }

        public View getView() {
            return mView;
        }
    }

    public void showDialog(ArrayList<String> numbers) {
        final DialogHelper helper = new DialogHelper(numbers);
        Dialog dialog = new AlertDialog.Builder(this)
                .setView(helper.getView()).setTitle("选择号码")
                .setOnDismissListener(helper).create();
        helper.setDialog(dialog);
        dialog.show();
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        return false;
    }
}
