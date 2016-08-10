package com.callba.phone.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.callba.R;
import com.callba.phone.bean.ContactMutliNumBean;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.logic.contact.ContactController;
import com.callba.phone.logic.contact.ContactEntity;
import com.callba.phone.logic.contact.ContactListAdapter;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.logic.contact.ContactSerarchWatcher;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.FileUtils;
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
public class LocalContactFragment extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    @InjectView(R.id.et_contact_search)
    EditText et_search;
    @InjectView(R.id.lv_contact_contacts)
    ListView mListView;
    @InjectView(R.id.qsb_contact)
    QuickSearchBar mQuickSearchBar;
    @InjectView(R.id.tab_contact_ll)
    LinearLayout tabContactLl;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    private ContactPersonEntity contact;
    private List<ContactEntity> mContactListData; // 填充ListView的数据
    private ContactListAdapter mContactListAdapter;    //联系人适配器
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
        IntentFilter intentFilter = new IntentFilter("com.callba.contact");
        broadcastReceiver = new ContactBroadcastReceiver();
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void lazyLoad() {
        initContactListView();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    class ContactBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i("contact", "change");
            progressBar.setVisibility(View.VISIBLE);
            initContactListView();
        }
    }

    private void  initContactListView() {
        if(progressBar!=null)
        progressBar.setVisibility(View.VISIBLE);
        final ContactController contactController = new ContactController();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<ContactEntity> allContactEntities = contactController.getFilterListContactEntitiesNoDuplicate();
                FileUtils.writeObjectToFile(allContactEntities);
                SimpleHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mContactListData == null) {
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
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ContactEntity contactEntity = mContactListData.get(position);
        ContactPersonEntity contactPersonEntity = (ContactPersonEntity) contactEntity;
        ContactMutliNumBean contactMutliNumBean = (ContactMutliNumBean) contactPersonEntity;
        Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
        contactMutliNumBean.setAvatar(null);
        intent.putExtra("contact", contactMutliNumBean);
      /*  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {try {
            ActivityOptions options=  ActivityOptions
                    .makeSceneTransitionAnimation(getActivity(),
                          view.findViewById(R.id.avatar), "photo");
            getActivity().startActivity(intent, options.toBundle());
        }catch (Exception e){
            e.printStackTrace();
        }

        } else {
            //让新的Activity从一个小的范围扩大到全屏
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeScaleUpAnimation(view, view.getWidth() / 2,
                            view.getHeight() / 2, 0, 0);
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        }*/
       startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showDeleteDialog((ContactPersonEntity) mContactListData.get(position));
        return true;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void showDeleteDialog(final ContactPersonEntity entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(entity.getDisplayName());
        builder.setItems(new String[]{getString(R.string.delete_contact)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                ContactsAccessPublic.deleteContact(getActivity(), entity.get_id());
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

}
