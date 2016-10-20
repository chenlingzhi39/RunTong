package com.callba.phone.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.db.InviteMessgeDao;
import com.callba.phone.db.UserDao;
import com.callba.phone.ui.adapter.RecyclerArrayAdapter;
import com.callba.phone.ui.adapter.WebContactAdapter;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersDecoration;
import com.callba.phone.ui.base.BaseFragment;
import com.callba.phone.util.ContactsAccessPublic;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.callba.phone.util.RxBus;
import com.callba.phone.util.SimpleHandler;
import com.callba.phone.widget.DividerDecoration;
import com.callba.phone.widget.DividerItemDecoration;
import com.callba.phone.widget.SideBar;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by PC-20160514 on 2016/10/20.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.web_contact2
)
public class WebContactFragment2 extends BaseFragment {
    @BindView(R.id.contact_member)
    RecyclerView contactMember;
    @BindView(R.id.contact_sidebar)
    SideBar contactSidebar;
    @BindView(R.id.contact_dialog)
    TextView contactDialog;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    Map<String, EaseUser> contactsMap;
    private WebContactAdapter webContactAdapter;
    private Observable<CharSequence> mSearchObservable;
    CharSequence record;
    private MyFilter filter;
    public static WebContactFragment2 newInstance() {
        WebContactFragment2 webContactFragment = new WebContactFragment2();
        return webContactFragment;
    }
    @Override
    protected void initView(View fragmentRootView) {
        ButterKnife.bind(this, fragmentRootView);
        contactSidebar.setTextView(contactDialog);
        contactSidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = webContactAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    ((LinearLayoutManager)contactMember.getLayoutManager()).scrollToPositionWithOffset(position,contactMember.getLayoutManager().getChildCount());
                }

            }
        });
        webContactAdapter = new WebContactAdapter(getActivity());
        webContactAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(Constant.EXTRA_USER_ID, webContactAdapter.getItem(position).getUsername());
                startActivity(intent);
            }
        });
        webContactAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(int position) {
                showDialog(webContactAdapter.getItem(position));
                return false;
            }
        });
        mSearchObservable= RxBus.get().register("search_contact",CharSequence.class);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contactMember.setLayoutManager(layoutManager);
        contactMember.setAdapter(webContactAdapter);
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(webContactAdapter);
        contactMember.addItemDecoration(headersDecor);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        contactMember.addItemDecoration(new DividerDecoration(getActivity(),mDensity*50/160));
        webContactAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });
        refreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);
        //下拉刷新
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread() {
                    @Override
                    public void run() {
                        refresh();
                    }
                }.start();
            }
        });
        mSearchObservable.subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence s) {
                record=s;
                filter.filter(s);
            }
        });
    }

    protected void refresh() {
        subscription = Observable.create(new Observable.OnSubscribe<List<EaseUser>>() {
            @Override
            public void call(Subscriber<? super List<EaseUser>> subscriber) {
                subscriber.onNext(getContactList());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<EaseUser>>() {
            @Override
            public void call(List<EaseUser> easeUsers) {
                webContactAdapter.clear();
                Logger.i("ease_size",easeUsers.size()+"");
                webContactAdapter.addAll(easeUsers);
                filter=new MyFilter(easeUsers);
                refreshLayout.setRefreshing(false);
                if (!TextUtils.isEmpty(record))
                    filter.filter(record);
            }
        });
    }

    protected List<EaseUser> getContactList() {
        List<EaseUser> contactList = new ArrayList<>();
        contactsMap= DemoHelper.getInstance().getContactList();
        //获取联系人列表
        if (contactsMap == null) {
            return contactList;
        }
        synchronized (contactsMap) {
            final Iterator<Map.Entry<String, EaseUser>> iterator = contactsMap.entrySet().iterator();
            final List<String> blackList = EMClient.getInstance().contactManager().getBlackListUsernames();

            while (iterator.hasNext()) {
                Map.Entry<String, EaseUser> entry = iterator.next();
                //兼容以前的通讯录里的已有的数据显示，加上此判断，如果是新集成的可以去掉此判断
                if (!entry.getKey().equals("item_new_friends")
                        && !entry.getKey().equals("item_groups")
                        && !entry.getKey().equals("item_chatroom")
                        && !entry.getKey().equals("item_robots")) {
                    if (!blackList.contains(entry.getKey())) {
                        //不显示黑名单中的用户
                        EaseUser user = entry.getValue();
                        EaseCommonUtils.setUserInitialLetter(user);
                        contactList.add(user);
                    }
                }
            }
        }
        // 排序
        Collections.sort(contactList, new Comparator<EaseUser>() {

            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if (lhs.getInitialLetter().equals(rhs.getInitialLetter())) {
                    return lhs.getNick().compareTo(rhs.getNick());
                } else {
                    if ("#".equals(lhs.getInitialLetter())) {
                        return 1;
                    } else if ("#".equals(rhs.getInitialLetter())) {
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }
            }
        });
        return contactList;
    }
    class MyFilter extends Filter {
        List<EaseUser> mOriginalList = null;

        public MyFilter(List<EaseUser> myList) {
            this.mOriginalList = myList;
        }

        @Override
        protected synchronized FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if(mOriginalList==null){
                mOriginalList = new ArrayList<>();
            }
            if(prefix==null || prefix.length()==0){
                results.values = mOriginalList;
                results.count = mOriginalList.size();
            }else{
                String prefixString = prefix.toString();
                final int count = mOriginalList.size();
                final ArrayList<EaseUser> newValues = new ArrayList<EaseUser>();
                for(int i=0;i<count;i++){
                    final EaseUser user = mOriginalList.get(i);
                    String username = user.getUsername();
                    String nick=user.getNick();
                    String remark=user.getRemark();
                    if(nick.contains(prefixString)||remark.contains(prefixString)||username.contains(prefixString)){
                        newValues.add(user);
                    }
                }
                results.values=newValues;
                results.count=newValues.size();
            }
            return results;
        }

        @Override
        protected synchronized void publishResults(CharSequence constraint, FilterResults results) {
            webContactAdapter.clear();
            webContactAdapter.addAll((List<EaseUser>)results.values);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSearchObservable!=null)
            RxBus.get().unregister("search_contact",mSearchObservable);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void showDialog(final EaseUser easeUser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{getString(R.string.delete_contact),getString(R.string.Move_into_the_blacklist_new),getString(R.string.set_remark)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                OkHttpUtils
                                        .post()
                                        .url(Interfaces.DELETE_FRIENDS)
                                        .addParams("loginName", getUsername())
                                        .addParams("loginPwd", getPassword())
                                        .addParams("phoneNumber", easeUser.getUsername().substring(0, 11))
                                        .build().execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e, int id) {
                                        toast("删除失败");
                                    }

                                    @Override
                                    public void onResponse(String response, int id) {
                                        try {
                                            String[] result = response.split("\\|");
                                            Logger.i("delete_result", response);
                                            if (result[0].equals("0")) {
                                                // 删除此联系人
                                                deleteContact(easeUser);
                                                // 删除相关的邀请消息
                                                InviteMessgeDao dao = new InviteMessgeDao(getActivity());
                                                dao.deleteMessage(easeUser.getUsername());
                                            } else {
                                                toast("删除失败");
                                            }
                                        }catch(Exception e){
                                            toast(R.string.getserverdata_exception);
                                        }
                                    }
                                });


                                break;
                            case 1:
                                moveToBlacklist(easeUser.getUsername());
                                break;
                            case 2:
                                Intent intent=new Intent(getActivity(),RemarkActivity.class);
                                intent.putExtra("username",easeUser);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }
    /**
     * 删除联系人
     *
     * @param toDeleteUser
     */
    public void deleteContact(final EaseUser tobeDeleteUser) {
        String st1 = getResources().getString(R.string.deleting);
        final String st2 = getResources().getString(R.string.Delete_failed);
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(tobeDeleteUser.getUsername());
                    // 删除db和内存中此用户的数据
                    UserDao dao = new UserDao(getActivity());
                    dao.deleteContact(tobeDeleteUser.getUsername());
                    DemoHelper.getInstance().getContactList().remove(tobeDeleteUser.getUsername());
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            webContactAdapter.remove(tobeDeleteUser);
                        }
                    });
                } catch (final Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2 + e.getMessage(), 1).show();
                        }
                    });

                }

            }
        }).start();

    }
    /**
     * 把user移入到黑名单
     */
    protected void moveToBlacklist(final String username) {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        String st1 = getResources().getString(R.string.Is_moved_into_blacklist);
        final String st2 = getResources().getString(R.string.Move_into_blacklist_success);
        final String st3 = getResources().getString(R.string.Move_into_blacklist_failure);
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //加入到黑名单
                    EMClient.getInstance().contactManager().addUserToBlackList(username, false);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2, Toast.LENGTH_SHORT).show();
                            refresh();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st3, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

    }
}
