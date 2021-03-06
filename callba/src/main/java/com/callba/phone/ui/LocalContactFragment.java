package com.callba.phone.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Filter;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.DemoHelper;
import com.callba.phone.MyApplication;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.bean.Friend;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.manager.UserManager;
import com.callba.phone.pinyin.CharacterParser;
import com.callba.phone.pinyin.PinyinComparator;
import com.callba.phone.ui.adapter.ContactAdapter;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersDecoration;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.RxBus;
import com.callba.phone.util.SPUtils;
import com.callba.phone.widget.DividerDecoration;
import com.callba.phone.widget.SideBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by PC-20160514 on 2016/10/14.
 */
@ActivityFragmentInject(contentViewId = R.layout.local_contact)
public class LocalContactFragment extends BaseFragment {
    @BindView(R.id.contact_member)
    RecyclerView mRecyclerView;
    @BindView(R.id.contact_dialog)
    TextView mUserdialog;
    @BindView(R.id.contact_sidebar)
    SideBar mSidebar;
    private boolean first = true;
    private ContactAdapter contactAdapter;
    private ContactBroadcastReceiver broadcastReceiver;
    private PinyinComparator pinyinComparator;
    private Observable<CharSequence> mSearchObservable;
    private MyFilter filter;
    private CharSequence record;
    class ContactBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i("contact", "change");
            //progressBar.setVisibility(View.VISIBLE);
            initContactListView();
        }
    }
    public static LocalContactFragment newInstance() {
        LocalContactFragment localContactFragment = new LocalContactFragment();
        return localContactFragment;
    }
    private Gson gson;

    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.bind(this, fragmentRootView);
        mSidebar.setTextView(mUserdialog);
        pinyinComparator = new PinyinComparator();

        mSearchObservable= RxBus.get().register("search_contact",CharSequence.class);


        mSidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = contactAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    ((LinearLayoutManager)mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position,mRecyclerView.getLayoutManager().getChildCount());
                }

            }
        });
        gson = new Gson();
        subscription = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String string = (String) SPUtils.get(getActivity(), Constant.PACKAGE_NAME, "contacts", "");
                //String string = (String) FileUtils.readObjectFromFile(StorageUtils.getFilesDirectory(getActivity()) + File.separator + "contacts.txt");
                subscriber.onNext(string);

            }

        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()).map(new Func1<String, List<ContactMultiNumBean>>() {
            @Override
            public List<ContactMultiNumBean> call(String s) {

                final List<ContactMultiNumBean> personEntities = gson.fromJson(s, new TypeToken<ArrayList<ContactMultiNumBean>>() {
                }.getType());
                try {
                    Collections.sort(personEntities, pinyinComparator);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return personEntities;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<ContactMultiNumBean>>() {
            @Override
            public void call(List<ContactMultiNumBean> s) {
                contactAdapter = new ContactAdapter(getActivity());
                contactAdapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                        intent.putExtra("contact", contactAdapter.getItem(position));
                        startActivity(intent);
                    }
                });
                contactAdapter.setOnItemLongClickListener(new ContactAdapter.OnItemLongClickListener() {
                    @Override
                    public boolean onItemClick(int position) {
                        showDeleteDialog(contactAdapter.getItem(position));
                        return false;
                    }
                });
                filter=new MyFilter(s);
                contactAdapter.addAll(s);
                mSearchObservable.subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence s) {
                        record=s;
                        filter.filter(s);
                    }
                });
                final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setAdapter(contactAdapter);
                final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(contactAdapter);
                mRecyclerView.addItemDecoration(headersDecor);
                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int mDensity = metrics.densityDpi;
                Logger.i("density",mDensity+"");
                mRecyclerView.addItemDecoration(new DividerDecoration(getActivity(),mDensity*50/160));

                //   setTouchHelper();
                contactAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onChanged() {
                        headersDecor.invalidateHeaders();
                    }
                });
                initContactListView();
            }
        });

        IntentFilter intentFilter = new IntentFilter("com.callba.contact");
        broadcastReceiver = new ContactBroadcastReceiver();
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
        Logger.i("local", "init");
    }
    public class MyFilter extends Filter {
        List<ContactMultiNumBean> mOriginalList;

        public MyFilter(List<ContactMultiNumBean> messages) {
            this.mOriginalList = messages;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (mOriginalList == null) {
                mOriginalList = new ArrayList<>();
            }


            if (prefix == null || prefix.length() == 0) {
                results.values = mOriginalList;
                results.count = mOriginalList.size();
            } else {
                String prefixString = prefix.toString();
                final ArrayList<ContactMultiNumBean> newValues = new ArrayList<>();
                for (ContactMultiNumBean contactMultiNumBean:mOriginalList) {
                    final String string = contactMultiNumBean.getDisplayName();
                    if (string.contains(prefixString)||CharacterParser.getInstance().getSelling(string).contains(prefixString)) {
                        newValues.add(contactMultiNumBean);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactAdapter.clear();
            contactAdapter.addAll((ArrayList<ContactMultiNumBean>) results.values);
        }
    }
    private void initContactListView() {
        gson = new Gson();
        subscription = Observable.create(new Observable.OnSubscribe<List<ContactMultiNumBean>>() {
            @Override
            public void call(Subscriber<? super List<ContactMultiNumBean>> subscriber) {
                List<ContactMultiNumBean> allContactEntities = getContacts();
                if (first) {
                    SPUtils.put(getActivity(), Constant.PACKAGE_NAME, "contacts", gson.toJson(allContactEntities));
                    first = false;
                }
                try {
                    Collections.sort(allContactEntities, pinyinComparator);
                }catch (Exception e){
                    e.printStackTrace();
                }
                subscriber.onNext(allContactEntities);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<ContactMultiNumBean>>() {
            @Override
            public void call(List<ContactMultiNumBean> s) {
                filter = new MyFilter(s);
                if(TextUtils.isEmpty(record))
                {contactAdapter.clear();
                    contactAdapter.addAll(s);}else
                    filter.filter(record);
            }
        });
    }

    private List<ContactMultiNumBean> getContacts() {
        List<ContactPersonEntity> mAllContactPersonEntities = GlobalConfig.getInstance().getContactBeans();
        List<ContactMultiNumBean> personEntities = new ArrayList<>();
        List<String> contactPhones = new ArrayList<>();
        List<Friend> friends = new ArrayList<>();
        Logger.i("contact_size", mAllContactPersonEntities.size() + "");
        for (int i = 0; i < mAllContactPersonEntities.size(); i++) {
            friends.add(new Friend(mAllContactPersonEntities.get(i).getDisplayName().replace("%",""), Pattern.compile("[^0-9]").matcher(mAllContactPersonEntities.get(i).getPhoneNumber()).replaceAll("")));
            if (i == 0) {
                personEntities.add(new ContactMultiNumBean(mAllContactPersonEntities.get(0)));
                contactPhones.add(mAllContactPersonEntities.get(0).getPhoneNumber());
                personEntities.get(0).setContactPhones(contactPhones);
                continue;
            }
            if (!mAllContactPersonEntities.get(i).get_id().equals(mAllContactPersonEntities.get(i - 1).get_id())) {
                contactPhones = new ArrayList<>();
                contactPhones.add(mAllContactPersonEntities.get(i).getPhoneNumber());
                personEntities.add(new ContactMultiNumBean(mAllContactPersonEntities.get(i)));
            } else {
                contactPhones.add(mAllContactPersonEntities.get(i).getPhoneNumber());
            }
            personEntities.get(personEntities.size() - 1).setContactPhones(contactPhones);
        }
        if (friends.size() > 0&&getActivity()!=null)
            addRequestCall(OkHttpUtils
                    .post()
                    .url(Interfaces.ADD_FRIENDS)
                    .addParams("loginName", UserManager.getUsername(getActivity()))
                    .addParams("loginPwd", UserManager.getPassword(getActivity()))
                    .addParams("phoneNumbers", gson.toJson(friends))
                    .build()).execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(String response, int id) {
                    try {
                        Logger.i("add_results", response);
                        String[] result = response.split("\\|");
                        if (result[0].equals("0")) {
                            OkHttpUtils
                                    .post()
                                    .url(Interfaces.GET_FRIENDS)
                                    .addParams("loginName", UserManager.getUsername(getActivity()))
                                    .addParams("loginPwd", UserManager.getPassword(getActivity()))
                                    .build().execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    try {
                                        Logger.i("get_result", response);
                                        String[] result = response.split("\\|");
                                        if (result[0].equals("0")) {
                                            DemoHelper.getInstance().getContactList();
                                            ArrayList<BaseUser> list;
                                            list = gson.fromJson(result[1], new TypeToken<List<BaseUser>>() {
                                            }.getType());
                                            List<EaseUser> mList = new ArrayList<>();
                                            for (BaseUser baseUser : list) {
                                                EaseUser user = new EaseUser(baseUser.getPhoneNumber() + "-callba");
                                                user.setAvatar(baseUser.getUrl_head());
                                                user.setNick(baseUser.getNickname());
                                                user.setSign(baseUser.getSign());
                                                user.setRemark(baseUser.getRemark());
                                                EaseCommonUtils.setUserInitialLetter(user);
                                                mList.add(user);
                                            }
                                            DemoHelper.getInstance().updateContactList(mList);
                                            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(com.callba.phone.Constant.ACTION_CONTACT_CHANAGED));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        return personEntities;
    }
    private void showDeleteDialog(final ContactMultiNumBean entity) {
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mSearchObservable!=null)
        RxBus.get().unregister("search_contact",mSearchObservable);
    }
}
