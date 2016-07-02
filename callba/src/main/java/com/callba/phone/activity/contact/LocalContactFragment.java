package com.callba.phone.activity.contact;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseFragment;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.db.InviteMessgeDao;
import com.callba.phone.logic.contact.ContactController;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactListAdapter;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.ContactSerarchWatcher;
import com.callba.phone.logic.contact.QueryContacts;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.Logger;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.view.QuickSearchBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/21.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.local_contact
)
public class LocalContactFragment extends BaseFragment implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    @InjectView(R.id.et_contact_search)
    EditText et_search;
    @InjectView(R.id.lv_contact_contacts)
    ListView mListView;
    @InjectView(R.id.qsb_contact)
    QuickSearchBar mQuickSearchBar;
    @InjectView(R.id.tab_contact_ll)
    LinearLayout tabContactLl;
    private ContactPersonEntity contact;
    private List<ContactEntity> mContactListData; // 填充ListView的数据
    private ContactListAdapter mContactListAdapter;	//联系人适配器
    private ContactBroadcastReceiver broadcastReceiver;
    public static LocalContactFragment newInstance() {
        LocalContactFragment localContactFragment = new LocalContactFragment();
        return localContactFragment;
    }
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.inject(this, fragmentRootView);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        initContactListView();
        IntentFilter intentFilter=new IntentFilter("com.callba.contact");
        broadcastReceiver=new ContactBroadcastReceiver();
        getActivity().registerReceiver(broadcastReceiver,intentFilter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
    class ContactBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i("contact","change");
            initContactListView();
        }
    }
    private void initContactListView() {
       final ContactController contactController = new ContactController();
       new Thread(new Runnable() {
           @Override
           public void run() {
               final List<ContactEntity> allContactEntities = contactController.getFilterListContactEntitiesNoDuplicate();
               SimpleHandler.getInstance().post(new Runnable() {
                   @Override
                   public void run() {
                       if(mContactListData == null) {
                           mContactListData = new ArrayList<ContactEntity>();
                       }
                       mContactListData.clear();
                       mContactListData.addAll(allContactEntities);

                       mContactListAdapter = new ContactListAdapter(getActivity(), mContactListData);
                       mListView.setAdapter(mContactListAdapter);

                       mQuickSearchBar.setListView(mListView);
                       mQuickSearchBar.setListSearchMap(contactController.getSearchMap());

                       et_search.addTextChangedListener(new ContactSerarchWatcher(
                               mContactListAdapter, mContactListData, mQuickSearchBar));
                   }
               });
           }
       }).start();


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ContactEntity contactEntity = mContactListData.get(position);
        ContactPersonEntity contactPersonEntity = (ContactPersonEntity)contactEntity;
        ContactMutliNumBean contactMutliNumBean = (ContactMutliNumBean)contactPersonEntity;
        Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
        intent.putExtra("contact", contactMutliNumBean);
        intent.putExtra("activity", "ContactActivity");
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
       showDeleteDialog(getActivity(),(ContactPersonEntity)mContactListData.get(position));
        return true;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
    private void showDeleteDialog(Context context,
                                  final ContactPersonEntity entity) {

        final DialogHelper helper = new DialogHelper(entity);
        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(helper.getView()).create();
        helper.setDialog(dialog);
        dialog.show();
    }
    class DialogHelper implements DialogInterface.OnDismissListener,View.OnClickListener {
        private Dialog mDialog;
        private View view;
        TextView tv_name;
        Button delete;
        ContactPersonEntity entity;
        public DialogHelper(ContactPersonEntity entity) {
            this.entity=entity;
            view=getActivity().getLayoutInflater().inflate(R.layout.dialog_contact,null);
            tv_name=(TextView)view.findViewById(R.id.name);
            delete=(Button)view.findViewById(R.id.delete_contact);
            delete.setOnClickListener(this);
            tv_name.setText(entity.getDisplayName());
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.delete_contact:
                    ContactsAccessPublic.deleteContact(getActivity(),entity.getDisplayName());
                    mDialog.dismiss();
                    break;

            }
        }

        public View getView() {
            return view;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialog=null;
        }
    }


}
